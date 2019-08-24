/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.control;

import io.github.oasis.game.Main;
import io.github.oasis.game.persist.OasisSink;
import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import io.github.oasis.model.defs.OasisGameDef;
import io.github.oasis.model.events.JsonEvent;
import io.github.oasis.services.DataCache;
import io.github.oasis.services.configs.OasisConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Properties;

/**
 * @author iweerarathna
 */
class LocalRunner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LocalRunner.class);

    private final long gameId;
    private OasisSink oasisSink;
    private DataCache dataCache;
    private Sources sources;
    private OasisConfigurations oasisConfigurations;

    LocalRunner(OasisConfigurations configurations,
                long gameId,
                DataCache dataCache,
                Sources sources,
                OasisSink oasisSink) {
        this.oasisConfigurations = configurations;
        this.gameId = gameId;
        this.oasisSink = oasisSink;
        this.dataCache = dataCache;
        this.sources = sources;
    }

    @Override
    public void run() {
        // setup explicit configs
        Map<String, Object> localRunProps = oasisConfigurations.getLocalRun();

        // @TODO convert OasisConfiguration object to Map and pass it here
        Configs configs = Configs.from(new Properties());
        if (localRunProps != null) {
            for (Map.Entry<String, Object> entry : localRunProps.entrySet()) {
                configs.append(entry.getKey(), entry.getValue());
            }
        }

        QueueSource queueSource = new QueueSource(gameId);

        configs.append(ConfigKeys.KEY_LOCAL_REF_SOURCE, queueSource);
        configs.append(ConfigKeys.KEY_LOCAL_REF_OUTPUT, oasisSink);

        try {
            OasisGameDef gameDef = dataCache.loadGameDefs(gameId);
            Main.startGame(configs, gameDef);
        } catch (Throwable e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    void stop() throws InterruptedException {
        sources.finish(gameId);
    }

    void submitEvent(Map<String, Object> event) throws Exception {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.putAll(event);
        sources.poll(gameId).put(jsonEvent);
    }

}

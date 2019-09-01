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

package io.github.oasis.game.persist.rabbit;

import io.github.oasis.model.configs.ConfigKeys;
import io.github.oasis.model.configs.Configs;
import io.github.oasis.model.configs.EnvKeys;
import io.github.oasis.model.utils.OasisUtils;
import org.apache.flink.streaming.connectors.rabbitmq.common.RMQConnectionConfig;

/**
 * @author iweerarathna
 */
public class RabbitUtils {

    public static RMQConnectionConfig createRabbitSourceConfig(Configs gameProps) {
        return new RMQConnectionConfig.Builder()
                .setHost(OasisUtils.getEnvOr(EnvKeys.OASIS_RABBIT_HOST,
                        gameProps.getStr(ConfigKeys.KEY_RABBIT_HOST, "localhost")))
                .setPort(gameProps.getInt(ConfigKeys.KEY_RABBIT_PORT, ConfigKeys.DEF_RABBIT_PORT))
                .setVirtualHost(gameProps.getStr(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST, ConfigKeys.DEF_RABBIT_VIRTUAL_HOST))
                .setUserName(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_GSRC_USERNAME))
                .setPassword(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_GSRC_PASSWORD))
                .build();
    }


    public static RMQConnectionConfig createRabbitSinkConfig(Configs gameProps) {
        return new RMQConnectionConfig.Builder()
                .setHost(OasisUtils.getEnvOr(EnvKeys.OASIS_RABBIT_HOST,
                                gameProps.getStr(ConfigKeys.KEY_RABBIT_HOST, "localhost")))
                .setPort(gameProps.getInt(ConfigKeys.KEY_RABBIT_PORT, ConfigKeys.DEF_RABBIT_PORT))
                .setVirtualHost(gameProps.getStr(ConfigKeys.KEY_RABBIT_VIRTUAL_HOST, ConfigKeys.DEF_RABBIT_VIRTUAL_HOST))
                .setUserName(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_GSNK_USERNAME))
                .setPassword(gameProps.getStrReq(ConfigKeys.KEY_RABBIT_GSNK_PASSWORD))
                .build();
    }

}

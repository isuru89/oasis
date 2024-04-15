/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.events;

import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.services.events.dispatcher.DispatcherFactory;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static io.github.oasis.services.events.Constants.ENV_CONFIG_FILE;
import static io.github.oasis.services.events.Constants.SYS_CONFIG_FILE;

/**
 * @author Isuru Weerarathna
 */
public class Main {

    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        var configs = resolveConfigs();
        var jsonObject = new JsonObject(configs.getAll());
        var vertxOptions = new ConfigStoreOptions()
                .setType("json")
                .setConfig(jsonObject);

        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions().addStore(vertxOptions);

        deploy(retrieverOptions);
    }

    private static OasisConfigs resolveConfigs() {
        String confPath = System.getenv(ENV_CONFIG_FILE);
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            return new OasisConfigs.Builder().buildFromYamlFile(confPath);
        }
        confPath = System.getProperty(SYS_CONFIG_FILE);
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            return new OasisConfigs.Builder().buildFromYamlFile(confPath);
        }
        return new OasisConfigs.Builder().buildFromYamlResource("defaults.yml");
    }

    public static void deploy(ConfigRetrieverOptions configOptions) {
        Vertx vertx = Vertx.vertx();
        ConfigRetriever retriever = ConfigRetriever.create(vertx, configOptions);

        retriever.getConfig(configs -> {
            vertx.registerVerticleFactory(new DispatcherFactory());

            DeploymentOptions options = new DeploymentOptions().setConfig(configs.result());
            vertx.deployVerticle(new EventsApi(), options, res -> {
                if (!res.succeeded()) {
                    LOG.warn("Shutting down Events API. Try again later!");
                    LOG.error("Error:", res.cause());
                    vertx.close();
                }
            });
        });

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                vertx.close();
            }
        });
    }

}

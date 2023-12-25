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
        ConfigStoreOptions envConfigs = new ConfigStoreOptions().setType("env");
        ConfigStoreOptions jvmConfigs = new ConfigStoreOptions().setType("sys");
        ConfigStoreOptions fileConfigs = new ConfigStoreOptions()
                .setType("file")
                .setFormat("hocon")
                .setConfig(new JsonObject().put("path", resolveConfigFileLocation()));

        ConfigRetrieverOptions retrieverOptions = new ConfigRetrieverOptions()
                .addStore(jvmConfigs)
                .addStore(envConfigs)
                .addStore(fileConfigs);

        deploy(retrieverOptions);
    }

    private static String resolveConfigFileLocation() {
        String confPath = System.getenv(ENV_CONFIG_FILE);
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            return confPath;
        }
        confPath = System.getProperty(SYS_CONFIG_FILE);
        if (Objects.nonNull(confPath) && !confPath.isEmpty()) {
            return confPath;
        }
        return "application.conf";
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

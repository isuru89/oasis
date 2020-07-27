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

package io.github.oasis.ext.rabbitstream;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.RecoveryDelayHandler;
import com.typesafe.config.Config;

import javax.net.ssl.SSLSocketFactory;
import java.util.Map;

import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_HOST;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_PASSWORD;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_PORT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_RETRY_DELAY;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_USER;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.CONFIG_VIRTUAL_HOST;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_HOST;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_PORT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_RETRY_COUNT;
import static io.github.oasis.ext.rabbitstream.RabbitConstants.DEFAULT_RETRY_DELAY;

/**
 * Creates rabbit mq factory instances from various config inputs.
 *
 * @author Isuru Weerarathna
 */
class FactoryInitializer {


    /**
     * Creates rabbit connection factory using given configuration map.
     * @param configs configuration map
     * @return a new rabbit connection factory.
     */
    static ConnectionFactory createFrom(Map<String, Object> configs) {
        ConnectionFactory factory = new ConnectionFactory();

        factory.setHost((String) configs.getOrDefault(CONFIG_HOST, DEFAULT_HOST));
        factory.setPort((int) configs.getOrDefault(CONFIG_PORT, DEFAULT_PORT));

        int maxRetries = (int) configs.getOrDefault(CONFIG_RETRY_COUNT, DEFAULT_RETRY_COUNT);
        int delay = (int) configs.getOrDefault(CONFIG_RETRY_DELAY, DEFAULT_RETRY_DELAY);
        factory.setNetworkRecoveryInterval(maxRetries);
        factory.setRecoveryDelayHandler(new RecoveryDelayHandler.DefaultRecoveryDelayHandler(delay));
        factory.setAutomaticRecoveryEnabled(true);
        if (configs.containsKey(CONFIG_VIRTUAL_HOST)) {
            factory.setVirtualHost((String) configs.get(CONFIG_VIRTUAL_HOST));
        }
        if (configs.containsKey(CONFIG_USER)) {
            factory.setUsername((String) configs.get(CONFIG_USER));
            factory.setPassword((String) configs.get(CONFIG_PASSWORD));
        }
        return factory;
    }

    static ConnectionFactory createFrom(Config configs) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(getStringOrDefault(configs, CONFIG_HOST, DEFAULT_HOST));
        factory.setPort(getIntOrDefault(configs, CONFIG_PORT, DEFAULT_PORT));
        factory.setAutomaticRecoveryEnabled(true);

        if (configs.hasPath(CONFIG_VIRTUAL_HOST)) {
            factory.setVirtualHost(configs.getString(CONFIG_VIRTUAL_HOST));
        }

        if (configs.hasPath(CONFIG_USER)) {
            factory.setUsername(configs.getString(CONFIG_USER));
            factory.setPassword(configs.getString(CONFIG_PASSWORD));
        }

        factory.setNetworkRecoveryInterval(getIntOrDefault(configs, RabbitConstants.CONFIG_RETRY_DELAY, 5000));

        setupSSL(factory, configs);
        return factory;
    }

    private static void setupSSL(ConnectionFactory factory, Config config) throws Exception {
        if (config.hasPath(RabbitConstants.CONFIG_SSL)) {
            Config sslConfigs = config.getObject(RabbitConstants.CONFIG_SSL).toConfig();
            if (sslConfigs.getBoolean(RabbitConstants.CONFIG_SSL_ENABLED)) {
                String protocol = getStringOrDefault(sslConfigs, RabbitConstants.CONFIG_SSL_PROTOCOL, "TLSv1.2");
                boolean trustAll = getBoolOrDefault(sslConfigs, RabbitConstants.CONFIG_SSL_TRUSTALL, false);
                if (trustAll) {
                    factory.useSslProtocol(protocol);
                    factory.setSocketFactory(SSLSocketFactory.getDefault());
                } else {

                    // @TODO create ssl context

                }
            }
        }
    }

    private static String getStringOrDefault(Config configs, String key, String defValue) {
        if (configs.hasPath(key)) {
            return configs.getString(key);
        }
        return defValue;
    }

    private static int getIntOrDefault(Config configs, String key, int defValue) {
        if (configs.hasPath(key)) {
            return configs.getInt(key);
        }
        return defValue;
    }

    private static boolean getBoolOrDefault(Config configs, String key, boolean defValue) {
        if (configs.hasPath(key)) {
            return configs.getBoolean(key);
        }
        return defValue;
    }
}

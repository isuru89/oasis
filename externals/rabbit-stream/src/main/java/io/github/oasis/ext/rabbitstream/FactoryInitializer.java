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
import io.github.oasis.core.configs.OasisConfigs;

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

    static ConnectionFactory createFrom(OasisConfigs configs) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(getStringOrDefault(configs, CONFIG_HOST, DEFAULT_HOST));
        factory.setPort(getIntOrDefault(configs, CONFIG_PORT, DEFAULT_PORT));
        factory.setAutomaticRecoveryEnabled(true);

        if (configs.hasProperty(CONFIG_VIRTUAL_HOST)) {
            factory.setVirtualHost(configs.get(CONFIG_VIRTUAL_HOST, null));
        }

        if (configs.hasProperty(CONFIG_USER)) {
            factory.setUsername(configs.get(CONFIG_USER, null));
            factory.setPassword(configs.get(CONFIG_PASSWORD, null));
        }

        factory.setNetworkRecoveryInterval(getIntOrDefault(configs, RabbitConstants.CONFIG_RETRY_DELAY, 5000));

        setupSSL(factory, configs);
        return factory;
    }

    private static void setupSSL(ConnectionFactory factory, OasisConfigs config) throws Exception {
        if (config.hasProperty(RabbitConstants.CONFIG_SSL) && config.getBoolean("ssl.enabled", false)) {
            String protocol = config.get("ssl.protocol", "TLSv1.2");
            boolean trustAll = config.getBoolean("ssl.trustAll", false);
            if (trustAll) {
                factory.useSslProtocol(protocol);
                factory.setSocketFactory(SSLSocketFactory.getDefault());
            } else {

                // @TODO create ssl context

            }
        }
    }

    private static String getStringOrDefault(OasisConfigs configs, String key, String defValue) {
        return configs.get(key, defValue);
    }

    private static int getIntOrDefault(OasisConfigs configs, String key, int defValue) {
       return configs.getInt(key, defValue);
    }

    private static boolean getBoolOrDefault(OasisConfigs configs, String key, boolean defValue) {
        return configs.getBoolean(key, defValue);
    }
}

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

/**
 * @author Isuru Weerarathna
 */
class RabbitConstants {

    static final int RETRY_SEED = 0;

    static final String GAME_EXCHANGE = "oasis.game.exchange";
    static final String ANNOUNCEMENT_EXCHANGE = "oasis.announcements";

    static final String ANNOUNCEMENT_EXCHANGE_TYPE = "fanout";
    static final String GAME_EXCHANGE_TYPE = "direct";

    static final String CONFIG_HOST = "host";
    static final String CONFIG_PORT = "port";
    static final String CONFIG_VIRTUAL_HOST = "virtualHost";
    static final String CONFIG_USER = "user";
    static final String CONFIG_PASSWORD = "password";
    static final String CONFIG_SSL = "ssl";
    static final String CONFIG_SSL_ENABLED = "enabled";
    static final String CONFIG_SSL_PROTOCOL = "protocol";
    static final String CONFIG_SSL_TRUSTALL = "trustAll";

    static final String CONFIG_RETRY_COUNT = "connectionRetries";
    static final String CONFIG_RETRY_DELAY = "connectionRetryDelay";

    static final int DEFAULT_PORT = 5672;
    static final String DEFAULT_HOST = "localhost";
    static final int DEFAULT_RETRY_COUNT = 1;
    static final int DEFAULT_RETRY_DELAY = 5000;

}

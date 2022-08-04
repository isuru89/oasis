/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.ext.kafkastream;

/**
 * @author Isuru Weerarathna
 */
final class KafkaConstants {

    static final String GAME_EVENT_CONSUMER_GROUP = "oasis-game-event-consumer-%d";

    static final String TOPIC_GAME_EVENTS = "oasis.game.events";
    static final String TOPIC_GAME_ANNOUNCEMENTS = "oasis.game.announcements";
    static final String TOPIC_ENGINE_RELATED_EVENTS = "oasis.engine.events";
    static final String TOPIC_FEEDS = "oasis.feeds";

    static final String TOPIC_DELIMITER = ".";

    static final int DEFAULT_MAX_CONSUMER_POOL_THREADS = 10;

    static final String EARLIEST = "earliest";

    static final String DEFAULT_ENGINE_EVENT_CONSUMER_GROUP = "oasis-admin-api-consumer";

    static final class ConsumerConstants {

        static final String DEFAULT_ANNOUNCEMENT_AUTO_COMMIT = "false";
        static final String DEFAULT_FEED_AUTO_COMMIT = "false";
        static final String DEFAULT_ANNOUNCEMENT_FETCH_COUNT = "1";

    }
}

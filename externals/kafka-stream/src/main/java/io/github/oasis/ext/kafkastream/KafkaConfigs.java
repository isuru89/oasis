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

import lombok.Data;
import lombok.ToString;

import java.util.Map;

/**
 * Represents the same accepted kafka configurations structure as in config file.
 *
 * @author Isuru Weerarathna
 */
@Data
@ToString
class KafkaConfigs {

    private String brokerUrls;
    private Integer maxConsumerThreadPoolSize = KafkaConstants.DEFAULT_MAX_CONSUMER_POOL_THREADS;

    private GameEventConsumerConfigs gameEventsConsumer;
    private AnnounceConsumerConfigs broadcastConsumer;
    private DispatcherConfigs dispatcherConfigs;
    private EngineStatusTopicConsumerConfigs engineEventConsumer;
    private FeedStreamConfigs feedStreamConsumer;

    @Data
    static class DispatcherConfigs {
        private String clientId;

        private Map<String, String> props;
    }

    @Data
    static class GameEventConsumerConfigs {
        private String groupId;

        private Map<String, String> props;
    }

    @Data
    static class FeedStreamConfigs {
        private String groupId;
        private String instanceId;

        private Map<String, String> props;
    }

    @Data
    static class AnnounceConsumerConfigs {
        private String groupId;
        private String instanceId;

        private Map<String, String> props;
    }

    @Data
    static class EngineStatusTopicConsumerConfigs {
        private String groupId;

        private Map<String, String> props;
    }
}

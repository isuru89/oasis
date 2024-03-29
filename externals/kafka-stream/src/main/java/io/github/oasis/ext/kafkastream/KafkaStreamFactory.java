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

import io.github.oasis.core.external.*;

/**
 * Kafka support for all streams.
 *
 * @author Isuru Weerarathna
 */
public class KafkaStreamFactory implements EventStreamFactory {

    private final KafkaDispatcher kafkaDispatcher = new KafkaDispatcher();
    private final KafkaStreamConsumer kafkaStreamConsumer = new KafkaStreamConsumer();
    private final KafkaEngineManagerSubscription engineManagerSubscription = new KafkaEngineManagerSubscription();
    private final FeedHandler feedHandler = new KafkaFeedHandler();


    @Override
    public SourceStreamProvider getEngineEventSource() {
        return kafkaStreamConsumer;
    }

    @Override
    public EventDispatcher getDispatcher() {
        return kafkaDispatcher;
    }

    @Override
    public EngineManagerSubscription getEngineManagerSubscription() {
        return engineManagerSubscription;
    }

    @Override
    public FeedHandler getFeedHandler() {
        return feedHandler;
    }

    private static class KafkaFeedHandler implements FeedHandler {
            @Override
            public FeedConsumer getFeedConsumer() {
                return new KafkaFeedConsumer();
            }

            @Override
            public FeedPublisher getFeedPublisher() {
                return new KafkaFeedPublisher();
            }
    }
}

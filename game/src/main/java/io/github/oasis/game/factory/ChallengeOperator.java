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

package io.github.oasis.game.factory;

import io.github.oasis.game.process.ChallengeProcess;
import io.github.oasis.game.process.OasisIDs;
import io.github.oasis.model.DefinitionUpdateEvent;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.handlers.PointNotification;
import org.apache.flink.streaming.api.datastream.BroadcastStream;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

public class ChallengeOperator {


    public static ChallengePipelineResponse createChallengePipeline(DataStream<Event> eventDataStream,
                                                                    BroadcastStream<DefinitionUpdateEvent> definitionUpdateBroadcastStream,
                                                                    OutputTag<PointNotification> pointOutputTag) {
        SingleOutputStreamOperator<ChallengeEvent> challengeStream = eventDataStream
                .connect(definitionUpdateBroadcastStream)
                .process(new ChallengeProcess())
                .uid(OasisIDs.CHALLENGE_PROCESSOR_ID);

        DataStream<PointNotification> pointOutput = challengeStream.getSideOutput(pointOutputTag);
        return new ChallengePipelineResponse(pointOutput, challengeStream);
    }

    public static class ChallengePipelineResponse {
        private DataStream<PointNotification> pointNotificationStream;
        private DataStream<ChallengeEvent> challengeEventDataStream;

        ChallengePipelineResponse(DataStream<PointNotification> pointNotificationStream,
                                  DataStream<ChallengeEvent> challengeEventDataStream) {
            this.pointNotificationStream = pointNotificationStream;
            this.challengeEventDataStream = challengeEventDataStream;
        }

        public DataStream<PointNotification> getPointNotificationStream() {
            return pointNotificationStream;
        }

        public DataStream<ChallengeEvent> getChallengeEventDataStream() {
            return challengeEventDataStream;
        }
    }

}

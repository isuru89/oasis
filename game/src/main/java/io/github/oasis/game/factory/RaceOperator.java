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

import io.github.oasis.game.process.RaceProcess;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.EventNames;
import io.github.oasis.model.events.RaceEvent;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.util.OutputTag;

public class RaceOperator {

    public static RacePipelineResult createRacePipeline(DataStream<Event> eventDataStream,
                                                           OutputTag<PointNotification> pointOutput,
                                                           PointRule pointRule) {
        SingleOutputStreamOperator<RaceEvent> raceStream = eventDataStream
                .filter(new FilterFunction<Event>() {
                    @Override
                    public boolean filter(Event event) {
                        return Utils.eventEquals(event, EventNames.OASIS_EVENT_RACE_AWARD);
                    }
                }).process(new RaceProcess(pointRule, pointOutput)).uid("oasis-race-process");

        return new RacePipelineResult(raceStream, raceStream.getSideOutput(pointOutput));
    }

    public static class RacePipelineResult {
        private DataStream<RaceEvent> raceStream;
        private DataStream<PointNotification> pointStream;

        RacePipelineResult(DataStream<RaceEvent> raceStream, DataStream<PointNotification> pointStream) {
            this.raceStream = raceStream;
            this.pointStream = pointStream;
        }

        public DataStream<RaceEvent> getRaceStream() {
            return raceStream;
        }

        public DataStream<PointNotification> getPointStream() {
            return pointStream;
        }
    }

}

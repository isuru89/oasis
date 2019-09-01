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

import io.github.oasis.game.Oasis;
import io.github.oasis.game.process.EventUserSelector;
import io.github.oasis.game.process.RatingProcess;
import io.github.oasis.game.utils.Utils;
import io.github.oasis.model.Event;
import io.github.oasis.model.Rating;
import io.github.oasis.model.events.RatingEvent;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;

public class RatingsOperator {

    public static DataStream<RatingEvent> createRatingStream(Rating rating,
                                                             DataStream<Event> eventDataStream,
                                                             Oasis oasis) {
        FilterFunction<Event> filter;
        if (rating.getCondition() != null) {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) throws Exception {
                    return Utils.eventEquals(value, rating.getEvent())
                            && Utils.evaluateCondition(rating.getCondition(), value.getAllFieldValues());
                }
            };
        } else {
            filter = new FilterFunction<Event>() {
                @Override
                public boolean filter(Event value) {
                    return Utils.eventEquals(value, rating.getEvent());
                }
            };
        }

        return eventDataStream.filter(filter)
                .keyBy(new EventUserSelector<>())
                .process(new RatingProcess(rating))
                .uid(String.format("oasis-%s-states-processor-%d", oasis.getId(), rating.getId()));
    }

}

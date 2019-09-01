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

import io.github.oasis.model.collect.Pair;
import io.github.oasis.model.events.PointEvent;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Collections;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointsNotifier implements FlatMapFunction<PointEvent, PointNotification> {

    @Override
    public void flatMap(PointEvent event, Collector<PointNotification> out) {
        for (Map.Entry<String, Pair<Double, PointRule>> entry : event.getReceivedPoints().entrySet()) {
            out.collect(new PointNotification(
                    event.getUser(),
                    Collections.singletonList(event),
                    entry.getValue().getValue1(),
                    entry.getValue().getValue0()));
        }
    }
}
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

package io.github.oasis.game.process;

import io.github.oasis.model.Constants;
import io.github.oasis.model.Milestone;
import io.github.oasis.model.handlers.MilestoneNotification;
import io.github.oasis.model.handlers.PointNotification;
import io.github.oasis.model.rules.PointRule;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
public class PointsFromMilestoneMapper implements FlatMapFunction<MilestoneNotification, PointNotification> {

    private final PointRule associatedRule;

    public PointsFromMilestoneMapper(PointRule associatedRule) {
        this.associatedRule = associatedRule;
    }

    @Override
    public void flatMap(MilestoneNotification milestoneNotification, Collector<PointNotification> out) {
        Milestone milestone = milestoneNotification.getMilestone();
        Milestone.Level level = milestone.getLevel(milestoneNotification.getLevel());
        if (level != null
                && level.getAwardPoints() != null) {
            PointNotification pointNotification = new PointNotification(
                    milestoneNotification.getUserId(),
                    Collections.singletonList(milestoneNotification.getEvent()),
                    associatedRule,   // reserved type of point rule
                    level.getAwardPoints()
            );
            pointNotification.setTag(Constants.POINTS_FROM_MILESTONE_TAG);
            out.collect(pointNotification);
        }
    }
}

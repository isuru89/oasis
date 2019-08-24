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

package io.github.oasis.game.persist.mappers;

import io.github.oasis.model.Event;
import io.github.oasis.model.handlers.MilestoneNotification;
import io.github.oasis.model.handlers.output.MilestoneModel;

/**
 * @author iweerarathna
 */
public class MilestoneNotificationMapper extends BaseNotificationMapper<MilestoneNotification, MilestoneModel> {

    @Override
    MilestoneModel create(MilestoneNotification notification) {
        MilestoneModel model = new MilestoneModel();
        Event event = notification.getEvent();

        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setUserId(notification.getUserId());
        model.setEventType(event.getEventType());
        model.setEvent(extractRawEvents(notification.getEvent()));
        model.setLevel(notification.getLevel());
        model.setMilestoneId(notification.getMilestone().getId());
        model.setMaximumLevel(notification.getMilestone().getLevels().size());
        model.setTs(event.getTimestamp());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());
        return model;
    }
}

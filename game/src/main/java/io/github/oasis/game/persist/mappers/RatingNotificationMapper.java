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
import io.github.oasis.model.Rating;
import io.github.oasis.model.handlers.RatingNotification;
import io.github.oasis.model.handlers.output.RatingModel;

public class RatingNotificationMapper extends BaseNotificationMapper<RatingNotification, RatingModel> {

    @Override
    RatingModel create(RatingNotification notification) {
        RatingModel model = new RatingModel();
        Event event = notification.getEvent();

        model.setUserId(notification.getUserId());
        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setRatingId(notification.getRatingRef().getId());
        model.setCurrentState(notification.getState().getId());
        model.setCurrentStateName(notification.getState().getName());
        model.setCurrentValue(notification.getCurrentValue());
        model.setCurrentPoints(notification.getState().getPoints());
        model.setCurrency(notification.getRatingRef().isCurrency());
        model.setEvent(extractRawEvents(event));
        model.setTs(event.getTimestamp());
        model.setExtId(event.getExternalId());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());
        model.setPreviousState(notification.getPreviousState());
        model.setPrevStateChangedAt(notification.getPreviousChangeAt());
        model.setPreviousStateName(notification.getRatingRef().getStates().stream()
                .filter(s -> s.getId().equals(notification.getPreviousState()))
                .map(Rating.RatingState::getName)
                .findFirst().orElse(""));
        return model;
    }
}

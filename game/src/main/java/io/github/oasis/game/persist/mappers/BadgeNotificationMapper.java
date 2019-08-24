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

import io.github.oasis.model.Badge;
import io.github.oasis.model.Event;
import io.github.oasis.model.events.BadgeEvent;
import io.github.oasis.model.handlers.BadgeNotification;
import io.github.oasis.model.handlers.output.BadgeModel;

import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author iweerarathna
 */
public class BadgeNotificationMapper extends BaseNotificationMapper<BadgeNotification, BadgeModel> {

    @Override
    BadgeModel create(BadgeNotification notification) {
        BadgeModel model = new BadgeModel();
        Event event = notification.getEvents().get(notification.getEvents().size() - 1);
        model.setTeamId(event.getTeam());
        model.setTeamScopeId(event.getTeamScope());
        model.setUserId(notification.getUserId());
        model.setEventType(event.getEventType());
        model.setSourceId(event.getSource());
        model.setGameId(event.getGameId());

        if (event instanceof BadgeEvent) {
            model.setEvents(((BadgeEvent) event).getEvents().stream()
                    .map(super::extractRawEvents).filter(Objects::nonNull)
                    .collect(Collectors.toList()));
        } else {
            model.setEvents(Collections.singletonList(extractRawEvents(event)));
        }
        model.setTag(notification.getTag());

        Badge badge = notification.getBadge();
        Long badgeId = badge.getParent() == null ? badge.getId() : badge.getParent().getId();
        String subBadgeId = badge.getParent() == null ? null : badge.getName();

        model.setBadgeId(badgeId);
        model.setSubBadgeId(subBadgeId);
        //model.setRuleId(notification.getRule().getId());
        model.setTs(event.getTimestamp());
        return model;
    }

}

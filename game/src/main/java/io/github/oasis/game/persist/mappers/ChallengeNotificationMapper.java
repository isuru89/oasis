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

import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.handlers.output.ChallengeModel;

/**
 * @author iweerarathna
 */
public class ChallengeNotificationMapper extends BaseNotificationMapper<ChallengeEvent, ChallengeModel> {

    @Override
    ChallengeModel create(ChallengeEvent notification) {
        ChallengeModel model = new ChallengeModel();
        model.setTeamId(notification.getTeam());
        model.setTeamScopeId(notification.getTeamScope());
        model.setUserId(notification.getUser());
        model.setWonAt(notification.getTimestamp());
        model.setChallengeId(notification.getChallengeId());
        model.setPoints(notification.getPoints());
        model.setEventExtId(notification.getExternalId());
        model.setTs(notification.getTimestamp());
        model.setSourceId(notification.getSource());
        model.setGameId(notification.getGameId());
        model.setWinNo(notification.getWinNo());
        return model;
    }
}

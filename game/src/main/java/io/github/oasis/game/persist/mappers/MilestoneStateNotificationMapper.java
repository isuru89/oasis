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

import io.github.oasis.model.events.MilestoneStateEvent;
import io.github.oasis.model.handlers.output.MilestoneStateModel;

/**
 * @author iweerarathna
 */
public class MilestoneStateNotificationMapper extends BaseNotificationMapper<MilestoneStateEvent, MilestoneStateModel> {

    @Override
    MilestoneStateModel create(MilestoneStateEvent notification) throws Exception {
        MilestoneStateModel model = new MilestoneStateModel();
        model.setUserId(notification.getUserId());
        model.setValue(notification.getValue());
        model.setValueInt(notification.getValueInt());
        model.setCurrBaseValue(notification.getCurrBaseValue());
        model.setCurrBaseValueInt(notification.getCurrBaseValueInt());
        model.setNextValue(notification.getNextValue());
        model.setNextValueInt(notification.getNextValueInt());
        model.setLossUpdate(notification.isLossUpdate());
        model.setLossValue(notification.getLossValue());
        model.setLossValueInt(notification.getLossValueInt());
        model.setMilestoneId(notification.getMilestone().getId());
        model.setGameId(notification.getGameId());
        return model;
    }
}

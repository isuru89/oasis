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

package io.github.oasis.services.services.injector.consumers;

import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.model.handlers.output.ChallengeModel;
import io.github.oasis.services.services.injector.ConsumerContext;
import io.github.oasis.services.services.injector.MsgAcknowledger;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ChallengeConsumer extends BaseConsumer<ChallengeModel> {

    private static final String GAME_ADD_CHALLENGE_WINNER = "game/batch/addChallengeWinner";

    public ChallengeConsumer(IOasisDao dao, ConsumerContext contextInfo, MsgAcknowledger acknowledger) {
        super(dao, ChallengeModel.class, contextInfo, acknowledger);
    }

    @Override
    public Map<String, Object> handle(ChallengeModel msg) {
        return ConsumerUtils.toChallengeDaoData(msg);
    }

    @Override
    public String getInsertScriptName() {
        return GAME_ADD_CHALLENGE_WINNER;
    }
}
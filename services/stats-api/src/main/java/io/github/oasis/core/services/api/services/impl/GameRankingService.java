/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.services.impl;

import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.services.annotations.AdminDbRepository;
import io.github.oasis.core.services.api.services.IGameRankingService;
import io.github.oasis.core.services.api.to.RankCreationRequest;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Service
public class GameRankingService extends AbstractOasisService implements IGameRankingService {

    public GameRankingService(@AdminDbRepository OasisRepository backendRepository) {
        super(backendRepository);
    }

    @Override
    public RankInfo addRank(int gameId, RankCreationRequest request) {
        RankInfo rankInfo = RankInfo.builder()
                .name(request.getName())
                .colorCode(request.getColorCode())
                .priority(request.getPriority())
                .build();

        return backendRepository.addRank(gameId, rankInfo);
    }

    @Override
    public List<RankInfo> listRanks(int gameId) {
        return backendRepository.listAllRanks(gameId);
    }

}

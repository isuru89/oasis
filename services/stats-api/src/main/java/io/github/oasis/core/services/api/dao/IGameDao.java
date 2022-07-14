/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.dao;

import io.github.oasis.core.Game;
import io.github.oasis.core.services.api.dao.configs.UseOasisSqlLocator;
import io.github.oasis.core.services.api.dao.dto.GameStatusDto;
import io.github.oasis.core.services.api.dao.dto.GameUpdatePart;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@UseOasisSqlLocator("io/github/oasis/db/scripts/game")
@RegisterBeanMapper(Game.class)
@RegisterBeanMapper(GameStatusDto.class)
public interface IGameDao {

    @SqlUpdate
    @GetGeneratedKeys("id")
    int insertGame(@BindBean Game game,
                   @Bind("ts") long timestamp);

    default int insertGame(Game game) {
        return insertGame(game, System.currentTimeMillis());
    }

    @SqlQuery
    Game readGame(@Bind("id") int gameId);

    @SqlUpdate
    void updateGame(@Bind("id") int gameId,
                    @BindBean GameUpdatePart gameNew,
                    @Bind("ts") long ts);

    @SqlUpdate
    void deleteGame(@Bind("id") int gameId);

    @SqlQuery
    List<Game> listGames(@Bind("offset") int pageOffset,
                         @Bind("pageSize") int pageSize);

    @SqlQuery
    Game readGameByName(@Bind("name") String name);

    @SqlUpdate
    void updateGameStatus(@Bind("id") int gameId,
                          @Bind("newGameStatus") String newGameStatus,
                          @Bind("ts") long statusChangedTs);

    @SqlQuery
    GameStatusDto readGameStatus(@Bind("id") int gameId);

    @SqlQuery
    List<GameStatusDto> listGameStatusHistory(@Bind("id") int gameId,
                                              @Bind("startTime") long startTime,
                                              @Bind("endTime") long endTime);
}

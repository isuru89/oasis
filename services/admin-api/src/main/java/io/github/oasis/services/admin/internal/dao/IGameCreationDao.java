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

package io.github.oasis.services.admin.internal.dao;

import io.github.oasis.services.admin.internal.dto.NewGameDto;
import io.github.oasis.services.admin.json.game.GameJson;
import io.github.oasis.services.common.annotations.UseOasisSqlLocator;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

/**
 * Game creation and update related dao.
 *
 * @author Isuru Weerarathna
 */
@UseOasisSqlLocator("/admin/game")
public interface IGameCreationDao {

    @SqlUpdate("INSERT INTO OA_GAME_DEF (name, description, current_state)" +
            " VALUES" +
            " (:name, :description, :currentState)")
    @GetGeneratedKeys("game_id")
    int insertGame(@BindBean NewGameDto game);

    @SqlQuery
    @RegisterBeanMapper(GameJson.class)
    List<GameJson> readAllGames();

}

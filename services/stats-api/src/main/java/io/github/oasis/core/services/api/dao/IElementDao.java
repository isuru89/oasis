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

import io.github.oasis.core.elements.RankInfo;
import io.github.oasis.core.services.api.dao.configs.UseOasisSqlLocator;
import io.github.oasis.core.services.api.dao.dto.ElementDto;
import io.github.oasis.core.services.api.dao.dto.ElementUpdateDto;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;
import org.jdbi.v3.sqlobject.transaction.Transaction;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@UseOasisSqlLocator("io/github/oasis/db/scripts/elements")
@RegisterBeanMapper(ElementDto.class)
@RegisterBeanMapper(RankInfo.class)
public interface IElementDao {

    @SqlUpdate
    @GetGeneratedKeys("id")
    int insertNewElement(@BindBean ElementDto elementDef, @Bind("ts") long timestamp);

    @SqlUpdate
    void insertNewElementData(@Bind("id") int id, @BindBean ElementDto elementDef);

    @Transaction
    default void insertNewElement(ElementDto elementDef) {
        int newId = insertNewElement(elementDef, System.currentTimeMillis());
        insertNewElementData(newId, elementDef);
    }

    @SqlQuery
    ElementDto readElement(@Bind("defId") String elementId);

    @SqlQuery
    ElementDto readElementWithData(@Bind("defId") String elementId);

    @SqlQuery
    List<ElementDto> readElementsByType(@Bind("gameId") int gameId, @Bind("type") String type);

    @SqlQuery
    List<ElementDto> readElementsByGameId(@Bind("gameId") int gameId);

    @SqlUpdate
    void updateElement(@Bind("defId") String elementId, @BindBean ElementUpdateDto update, @Bind("ts") long updatedAt);

    @SqlUpdate
    void deleteElementById(@Bind("id") int id);
    @SqlUpdate
    void deleteElementDataById(@Bind("id") int id);

    @Transaction
    default void deleteElement(int id) {
        deleteElementById(id);
        deleteElementDataById(id);
    }


    @SqlUpdate
    @GetGeneratedKeys("id")
    int insertRank(@Bind("gameId") int gameId, @BindBean RankInfo newAttr);

    @SqlQuery
    RankInfo readRank(@Bind("gameId") int gameId, @Bind("id") int id);

    @SqlQuery
    List<RankInfo> readAllRanks(@Bind("gameId") int gameId);

}

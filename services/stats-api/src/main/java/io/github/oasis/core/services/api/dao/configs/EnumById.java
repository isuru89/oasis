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

package io.github.oasis.core.services.api.dao.configs;

import io.github.oasis.core.model.EnumIdSupport;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.result.UnableToProduceResultException;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;

/**
 * @author Isuru Weerarathna
 */
public class EnumById<E extends Enum<E>> implements ColumnMapper<E>, Argument {

//    private static final JdbiCache<Class<? extends Enum<?>>, JdbiCache<Integer, Enum<?>>> BY_ID_CACHE =
//            JdbiCaches.declare(e -> JdbiCaches.declare(
//                    id -> e.cast(getValueById(e, id))));

    private final Class<E> enumClass;

    public EnumById(Class<E> enumClass) {
        this.enumClass = enumClass;
    }

    public static <E extends Enum<E>> ColumnMapper<E> byId(Class<E> type) {
        return new EnumById<>(type);
    }

    @Override
    public E map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        int id = r.getInt(columnNumber);

        return enumClass.cast(getValueById(enumClass, id));
    }

    @Override
    public E map(ResultSet r, String columnLabel, StatementContext ctx) throws SQLException {
        int id = r.getInt(columnLabel);

        return enumClass.cast(getValueById(enumClass, id));
    }

    private static Object getValueById(Class<? extends Enum<?>> enumClass, int id) {
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(anEnum -> ((EnumIdSupport)anEnum).getId() == id)
                .findFirst()
                .orElseThrow(() -> new UnableToProduceResultException(
                        String.format("no %s value could be matched to the id %s", enumClass.getSimpleName(), id)));
    }

    @Override
    public void apply(int position, PreparedStatement statement, StatementContext ctx) throws SQLException {

    }
}

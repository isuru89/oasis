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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.QualifiedColumnMapperFactory;
import org.jdbi.v3.core.qualifier.QualifiedType;
import org.jdbi.v3.core.statement.StatementContext;

import java.io.IOException;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class OasisMapColumnFactory implements QualifiedColumnMapperFactory {

    private final MapColumnMapper mapColumnMapper;


    public OasisMapColumnFactory(ObjectMapper mapper) {
        mapColumnMapper = new MapColumnMapper(mapper);
    }


    @Override
    public Optional<ColumnMapper<?>> build(QualifiedType<?> type, ConfigRegistry config) {
        return Optional.of(type.getType())
                .filter(c -> Map.class.isAssignableFrom(c.getClass()))
                .map(clz -> mapColumnMapper);
    }

    public static class MapColumnMapper implements ColumnMapper<Map<String, Object>> {

        private final ObjectMapper mapper;
        private final TypeReference<HashMap<String, Object>> typeRef = new TypeReference<>() {
        };

        private MapColumnMapper(ObjectMapper mapper) {
            this.mapper = mapper;
        }

        @Override
        public Map<String, Object> map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
            Clob clob = r.getClob(columnNumber);
            try {
                return mapper.readValue(clob.getCharacterStream(), typeRef);
            } catch (IOException e) {
                throw new SQLException("Unable to parse clob to json!", e);
            }
        }
    }
}

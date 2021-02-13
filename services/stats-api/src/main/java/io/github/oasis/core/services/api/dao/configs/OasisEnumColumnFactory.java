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
import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.generic.GenericTypes;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.QualifiedColumnMapperFactory;
import org.jdbi.v3.core.qualifier.QualifiedType;

import java.util.Optional;

/**
 * @author Isuru Weerarathna
 */
public class OasisEnumColumnFactory implements QualifiedColumnMapperFactory {

    @SuppressWarnings("unchecked")
    @Override
    public Optional<ColumnMapper<?>> build(QualifiedType<?> givenType, ConfigRegistry config) {
        return Optional.of(givenType.getType())
                .map(GenericTypes::getErasedType)
                .filter(c -> Enum.class.isAssignableFrom(c) && EnumIdSupport.class.isAssignableFrom(c))
                .map(clazz -> EnumById.byId((Class<Enum>) clazz));
    }
}

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
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.argument.ArgumentFactory;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Types;

/**
 * @author Isuru Weerarathna
 */
public class OasisEnumArgTypeFactory extends AbstractArgumentFactory<EnumIdSupport> {

    /**
     * Constructs an {@link ArgumentFactory} for type {@code T}.
     */
    public OasisEnumArgTypeFactory() {
        super(Types.INTEGER);
    }

    @Override
    protected Argument build(EnumIdSupport value, ConfigRegistry config) {
        return ((position, statement, ctx) -> statement.setInt(position, value.getId()));
    }
}

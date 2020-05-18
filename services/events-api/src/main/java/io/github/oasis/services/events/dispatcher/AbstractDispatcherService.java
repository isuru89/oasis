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

package io.github.oasis.services.events.dispatcher;

import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.services.events.model.EventProxy;
import io.vertx.core.json.JsonObject;

import static io.github.oasis.core.external.messages.PersistedDef.FIELD_DATA;
import static io.github.oasis.core.external.messages.PersistedDef.FIELD_IMPL;
import static io.github.oasis.core.external.messages.PersistedDef.FIELD_TYPE;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractDispatcherService implements EventDispatcherService {

    private static final JsonObject EMPTY = new JsonObject();

    PersistedDef toPersistDef(EventProxy event) {
        return PersistedDef.fromEvent(event);
    }

    PersistedDef toPersistDef(JsonObject message) {
        PersistedDef def = new PersistedDef();
        def.setType(message.getString(FIELD_TYPE));
        def.setImpl(message.getString(FIELD_IMPL));
        def.setScope(null);
        def.setData(message.getJsonObject(FIELD_DATA, EMPTY).getMap());
        return def;
    }
}

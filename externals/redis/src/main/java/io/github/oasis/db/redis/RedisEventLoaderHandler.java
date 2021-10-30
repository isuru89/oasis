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

package io.github.oasis.db.redis;

import io.github.oasis.core.Event;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.EventReadWriteHandler;
import io.github.oasis.core.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RedisEventLoaderHandler implements EventReadWriteHandler {

    private final Db dbPool;
    private final OasisConfigs configs;

    public RedisEventLoaderHandler(Db dbPool, OasisConfigs configs) {
        this.dbPool = dbPool;
        this.configs = configs;
    }

    @Override
    public Optional<Event> read(String contextRef, String eventId) {
        try (DbContext db = dbPool.createContext()) {
            String data = db.getValueFromMap(contextRef, eventId);
            if (Objects.nonNull(data)) {
                return Optional.ofNullable(Utils.fromSerializedContent(Base64.getDecoder().decode(data)));
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Event> bulkRead(String contextRef, String... eventIds) {
        try (DbContext db = dbPool.createContext()) {
            List<String> data = db.getValuesFromMap(contextRef, eventIds);
            if (Objects.nonNull(data)) {
                return data.stream()
                        .map(raw -> (Event) Utils.fromSerializedContent(Base64.getDecoder().decode(raw)))
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public boolean write(String contextRef, Event event) {
        try (DbContext db = dbPool.createContext()) {
            db.setValueInMap(contextRef, event.getExternalId(), Base64.getEncoder().encodeToString(Utils.toSerializableContent(event)));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public boolean remove(String contextRef, String... eventIds) {
        try (DbContext db = dbPool.createContext()) {
            return db.removeKeyFromMap(contextRef, eventIds);
        } catch (IOException e) {
            return false;
        }
    }
}

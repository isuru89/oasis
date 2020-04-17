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

package io.github.oasis.engine.external.redis;

import io.github.oasis.engine.OasisConfigs;
import io.github.oasis.engine.external.Db;
import io.github.oasis.engine.external.DbContext;
import io.github.oasis.engine.external.EventReadWrite;
import io.github.oasis.engine.utils.Utils;
import io.github.oasis.model.Event;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RedisEventLoader implements EventReadWrite {

    private final Db dbPool;
    private final OasisConfigs configs;

    @Inject
    public RedisEventLoader(Db dbPool, OasisConfigs configs) {
        this.dbPool = dbPool;
        this.configs = configs;
    }

    @Override
    public Optional<Event> read(String contextRef, String eventId) {
        try (DbContext db = dbPool.createContext()) {
            byte[] data = db.getValueFromMap(contextRef, eventId.getBytes(StandardCharsets.US_ASCII));
            if (Objects.nonNull(data)) {
                return Optional.ofNullable(Utils.fromSerializedContent(data));
            }
            return Optional.empty();
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Event> bulkRead(String contextRef, String... eventIds) {
        try (DbContext db = dbPool.createContext()) {
            List<byte[]> data = db.getRawValuesFromMap(contextRef, eventIds);
            if (Objects.nonNull(data)) {
                return data.stream()
                        .map(raw -> (Event) Utils.fromSerializedContent(raw))
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
            db.setRawValueInMap(contextRef, event.getExternalId(), Utils.toSerializableContent(event));
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

/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.core.services.api.handlers;

import io.github.oasis.core.ID;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.services.annotations.OasisCache;
import io.github.oasis.core.services.api.handlers.events.BaseEventSourceChangedEvent;
import io.github.oasis.core.services.api.handlers.events.BasePlayerRelatedEvent;
import io.github.oasis.core.services.api.handlers.events.EntityChangeType;
import io.github.oasis.core.utils.CacheUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Clears relevant cache entries when some event is occurred which
 * should be notified to event-api.
 *
 * @author Isuru Weerarathna
 */
@Component
public class CacheClearanceListener {

    private final Db cache;

    public CacheClearanceListener(@OasisCache Db cache) {
        this.cache = cache;
    }

    @EventListener
    public void handleEventSourceUpdateEvent(BaseEventSourceChangedEvent eventSourceChangedEvent) {
        if (eventSourceChangedEvent.getChangeType() != EntityChangeType.ADDED) {
            try (DbContext db = cache.createContext()) {
                db.removeKey(CacheUtils.getSourceCacheKey(eventSourceChangedEvent.getToken()));

            } catch (IOException e) {
                // silently ignore cache clear failures
            }
        }
    }

    @EventListener
    public void handlePlayerUpdateEvent(BasePlayerRelatedEvent playerRelatedEvent) {
        if (playerRelatedEvent.getChangeType() != EntityChangeType.ADDED) {
            try (DbContext db = cache.createContext()) {
                db.MAP(ID.EVENT_API_CACHE_USERS_KEY).remove(playerRelatedEvent.getEmail());

            } catch (IOException e) {
                // silently ignore cache clear failures
            }
        }
    }
}

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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.services.api.to.EventSourceCreateRequest;
import io.github.oasis.core.services.api.to.EventSourceKeysResponse;

import java.util.List;

/**
 * All event source related operations.
 *
 * @author Isuru Weerarathna
 */
public interface IEventSourceService {

    EventSource registerEventSource(EventSourceCreateRequest request) throws OasisException;

    EventSource readEventSource(int eventSourceId);

    EventSource readEventSourceByToken(String token);

    void deleteEventSource(int eventSourceId);

    EventSourceKeysResponse downloadEventSourceKeys(int eventSourceId);

    void assignEventSourceToGame(int eventSource, int gameId);

    void removeEventSourceFromGame(int eventSource, int gameId);

    List<EventSource> listAllEventSources();

    List<EventSource> listAllEventSourcesOfGame(int gameId);

}

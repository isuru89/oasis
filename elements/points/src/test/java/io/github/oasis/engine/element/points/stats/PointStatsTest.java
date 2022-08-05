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
 */

package io.github.oasis.engine.element.points.stats;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.services.EngineDataReader;
import io.github.oasis.core.services.exceptions.ErrorCodes;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.engine.element.points.stats.to.UserPointsRequest;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableCallable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PointStatsTest {

    private ObjectMapper mapper = new ObjectMapper();
    private final TypeReference<HashMap<String, String>> MAP_TYPE_REFERENCE = new TypeReference<>() {
    };
    private final TypeReference<HashMap<String, Object>> ROOT_MAP_TYPE_REFERENCE = new TypeReference<>() {
    };

    private OasisMetadataSupport metadataSupport;
    private PointStats stats;
    private Db db;
    private DbContext context;

    @BeforeEach
    void beforeEach() {
        db = Mockito.mock(Db.class);
        context = Mockito.mock(DbContext.class);
        Mockito.when(db.createContext()).thenReturn(context);
        EngineDataReader engineDataReader = new EngineDataReader(db);
        metadataSupport = Mockito.mock(OasisMetadataSupport.class);

        stats = new PointStats(engineDataReader, metadataSupport);
    }

    @Test
    void getUserPointsValidations() {
        var request = new UserPointsRequest();
        assertApiValidationError(() -> stats.getUserPoints(request));

        request.setGameId(1);
        assertApiValidationError(() -> stats.getUserPoints(request));

        request.setUserId(1L);
        assertApiValidationError(() -> stats.getUserPoints(request));

        request.setFilters(List.of());
        assertApiValidationError(() -> stats.getUserPoints(request));

        request.setFilters(List.of(new UserPointsRequest.PointsFilterScope()));
        assertApiValidationError(() -> stats.getUserPoints(request));
    }

    @Test
    void getUserPoints() throws OasisApiException {
//        var request = new UserPointsRequest();
//        request.setGameId(1);
//        request.setUserId(1L);
//        var filter = new UserPointsRequest.PointsFilterScope();
//        filter.setType(UserPointsRequest.ScopedTypes.TEAM);
//        filter.setValues(List.of("1", "2", "3"));
//        request.setFilters(List.of(filter));
//
//        var memoryMap = new MemoryMappedInternal(readFromJson("stats.json", "pointSummary"));
//        Mockito.when(context.MAP(Mockito.eq(PointIDs.getGameUserPointsSummary(1, 1)))).thenReturn(memoryMap);
//
//        System.out.println(stats.getUserPoints(request));
    }

    @Test
    void getLeaderboard() {
    }

    @Test
    void getUserRankings() {
    }

    @Test
    void appendToSummary() {
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readFromJson(String file, String key) {
        try {
            String content = IOUtils.resourceToString(file, StandardCharsets.UTF_8, Thread.currentThread().getContextClassLoader());
            Map<String, Object> root = mapper.readValue(content, ROOT_MAP_TYPE_REFERENCE);
            return (Map<String, String>) root.get(key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private <T> void assertApiValidationError(FailableCallable<T, OasisApiException> executable) {
        try {
            executable.call();
            Assertions.fail("Should not expected to success!");
        } catch (OasisApiException e) {
            // ignored
            Assertions.assertEquals(ErrorCodes.GENERIC_ELEMENT_QUERY_FAILURE, e.getErrorCode());
        }
    }

}
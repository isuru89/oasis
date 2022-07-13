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

package io.github.oasis.services.feeds.services;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.Game;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static io.github.oasis.services.feeds.Constants.DEF_CONNECT_TIMEOUT;
import static io.github.oasis.services.feeds.Constants.DEF_REQUEST_TIMEOUT;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class  ApiDataService implements DataService {

    private final ObjectMapper mapper = new ObjectMapper();

    private HttpClient client;

    private String adminApiBaseUrl;
    private String adminApiAppId;
    private String adminApiAppSecret;

    private String urlPlayerGet;
    private String urlTeamGet;
    private String urlGameGet;
    private String urlEventSourceGet;

    private int requestTimeout = DEF_REQUEST_TIMEOUT;

    public void init(OasisConfigs configs) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.adminApiBaseUrl = configs.get("oasis.adminApi.baseUrl", EMPTY);
        this.adminApiAppId = configs.get("oasis.adminApi.apiKey", EMPTY);
        this.adminApiAppSecret = configs.get("oasis.adminApi.secretKey", EMPTY);

        this.urlEventSourceGet = configs.get("oasis.adminApi.eventSourceGet", EMPTY);
        this.urlGameGet = configs.get("oasis.adminApi.gameGet", EMPTY);
        this.urlPlayerGet = configs.get("oasis.adminApi.playerGet", EMPTY);
        this.urlTeamGet = configs.get("oasis.adminApi.teamGet", EMPTY);

        this.requestTimeout = configs.getInt("oasis.adminApi.requestTimeout", DEF_REQUEST_TIMEOUT);

        int connectTimeout = configs.getInt("oasis.adminApi.connectTimeout", DEF_CONNECT_TIMEOUT);
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();
    }

    @Override
    public PlayerObject getPlayer(long playerId) {
        return asEntity(adminApiGetReq(urlPlayerGet + playerId), PlayerObject.class);
    }

    @Override
    public TeamObject getTeam(long teamId) {
        return asEntity(adminApiGetReq(urlTeamGet + teamId), TeamObject.class);
    }

    @Override
    public Game getGame(int gameId) {
        return asEntity(adminApiGetReq(urlGameGet + gameId), Game.class);
    }

    @Override
    public EventSource getEventSource(int eventSourceId) {
        return asEntity(adminApiGetReq(urlEventSourceGet + eventSourceId), EventSource.class);
    }

    private <T> T asEntity(HttpRequest request, Class<T> clz) {
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new OasisRuntimeException("Unable to read data from api! [uri: " + request.uri() + ", Status: " + response.statusCode() + "]");
            }
            return mapper.readValue(response.body(), clz);

        } catch (IOException | InterruptedException e) {
            throw new OasisRuntimeException("Unable to read data!", e);
        }
    }

    private HttpRequest adminApiGetReq(String subUrl) {
        return adminApiReq(subUrl).GET().build();
    }

    private HttpRequest.Builder adminApiReq(String subUrl) {
        return HttpRequest.newBuilder()
                .uri(URI.create(adminApiBaseUrl + subUrl))
                .timeout(Duration.ofSeconds(requestTimeout))
                .header("Content-Type", "application/json")
                .header("X-APP-ID", adminApiAppId)
                .header("X-APP-KEY", adminApiAppSecret);
    }
}

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

package io.github.oasis.services.events.db;

import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;

import java.util.ArrayList;

/**
 * @author Isuru Weerarathna
 */
public class AdminApiClient implements DataService {

    private static final JsonObject EMPTY = new JsonObject();

    private WebClient webClient;
    private JsonObject configs;

    private String getPlayerInfoUrl;
    private String getPlayerTeamsUrl;
    private String getEventSourceInfoUrl;

    private String apiKey;
    private String secretKey;

    public AdminApiClient(WebClient webClient, JsonObject configs, Handler<AsyncResult<DataService>> resultHandler) {
        this.webClient = webClient;
        this.configs = configs;
        resultHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public DataService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler) {
        webClient.get(getPlayerInfoUrl)
                .addQueryParam("email", email)
                .addQueryParam("verbose", "true")
                .putHeader("X-APP-ID", apiKey)
                .putHeader("X-APP-KEY", secretKey)
                .putHeader("accept", "application/json")
                .send()
                .onSuccess(res -> {
                    PlayerWithTeams playerObject = res.bodyAsJson(PlayerWithTeams.class);
                    JsonObject jsonObject = new JsonObject()
                            .put(UserInfo.ID, playerObject.getId())
                            .put(UserInfo.EMAIL, playerObject.getEmail());
                    if (Utils.isNotEmpty(playerObject.getTeams())) {
                        JsonObject gamesObject = new JsonObject();
                        for (TeamObject team : playerObject.getTeams()) {
                            gamesObject.put(String.valueOf(team.getGameId()), new JsonObject().put(UserInfo.TEAM, team.getId()));
                        }
                        jsonObject.put(UserInfo.GAMES, gamesObject);
                    } else {
                        jsonObject.put(UserInfo.GAMES, EMPTY);
                    }

                    UserInfo userInfo = new UserInfo(jsonObject);
                    resultHandler.handle(Future.succeededFuture(userInfo));

                }).onFailure(err -> resultHandler.handle(Future.failedFuture(err)));
        return this;
    }

    @Override
    public DataService readSourceInfo(String sourceId, Handler<AsyncResult<EventSource>> resultHandler) {
        webClient.get(getEventSourceInfoUrl)
                .addQueryParam("token", sourceId)
                .addQueryParam("withKey", "true")
                .putHeader("X-APP-ID", apiKey)
                .putHeader("X-APP-KEY", secretKey)
                .putHeader("accept", "application/json")
                .send()
                .onSuccess(res -> {
                    var eventSource = res.bodyAsJson(io.github.oasis.core.model.EventSource.class);
                    JsonObject jsonObject = new JsonObject()
                            .put(EventSource.ID, eventSource.getId())
                            .put(EventSource.GAMES, new JsonArray(new ArrayList<>(eventSource.getGames())));
                    if (eventSource.getSecrets() != null && Texts.isNotEmpty(eventSource.getSecrets().getPublicKey())) {
                        jsonObject.put(EventSource.KEY, eventSource.getSecrets().getPublicKey());
                        EventSource info = new EventSource(jsonObject);
                        resultHandler.handle(Future.succeededFuture(info));
                    } else {
                        resultHandler.handle(Future.failedFuture(new OasisRuntimeException("The public key not received for source " + sourceId)));
                    }
                }).onFailure(err -> resultHandler.handle(Future.failedFuture(err)));
        return this;
    }
}

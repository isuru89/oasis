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

package io.github.oasis.services.events.client;

import io.github.oasis.core.Game;
import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.model.PlayerWithTeams;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.services.events.db.DataService;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.GameInfo;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

import static io.github.oasis.services.events.client.AdminConstants.HEADER_ACCEPT;
import static io.github.oasis.services.events.client.AdminConstants.HEADER_APP_ID;
import static io.github.oasis.services.events.client.AdminConstants.HEADER_APP_KEY;
import static io.github.oasis.services.events.client.AdminConstants.MEDIA_TYPE_JSON;
import static io.github.oasis.services.events.client.AdminConstants.QUERY_PARAM_EMAIL;
import static io.github.oasis.services.events.client.AdminConstants.QUERY_PARAM_TOKEN;
import static io.github.oasis.services.events.client.AdminConstants.QUERY_PARAM_VERBOSE;
import static io.github.oasis.services.events.client.AdminConstants.QUERY_PARAM_WITH_KEY;
import static io.github.oasis.services.events.client.AdminConstants.STATUS_NOT_FOUND;
import static io.github.oasis.services.events.client.AdminConstants.STATUS_SUCCESS;
import static io.github.oasis.services.events.client.AdminConstants.TRUE;
import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

/**
 * @author Isuru Weerarathna
 */
public class AdminApiClient implements DataService {

    private static final Logger LOG = LoggerFactory.getLogger(AdminApiClient.class);

    private static final JsonObject EMPTY = new JsonObject();

    private final WebClient webClient;

    private final String getPlayerInfoUrl;
    private final String getEventSourceInfoUrl;
    private final String getGameInfoUrl;

    private final String apiKey;
    private final String secretKey;

    public AdminApiClient(WebClient webClient, JsonObject configs) {
        this.webClient = webClient;

        JsonObject adminApiConf = configs.getJsonObject("adminApi", new JsonObject());

        String baseUrl = adminApiConf.getString("baseUrl");
        LOG.info("Admin api base url: {}", baseUrl);

        getPlayerInfoUrl = baseUrl + adminApiConf.getString("playerGet");
        getEventSourceInfoUrl = baseUrl + adminApiConf.getString("eventSourceGet");
        getGameInfoUrl = baseUrl + adminApiConf.getString("gameGet", "/games");

        apiKey = adminApiConf.getString("apiKey");
        secretKey = adminApiConf.getString("secretKey");
    }

    @Override
    public DataService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler) {
        webClient.getAbs(getPlayerInfoUrl)
                .addQueryParam(QUERY_PARAM_EMAIL, email)
                .addQueryParam(QUERY_PARAM_VERBOSE, TRUE)
                .putHeader(HEADER_APP_ID, apiKey)
                .putHeader(HEADER_APP_KEY, secretKey)
                .putHeader(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .send()
                .onSuccess(res -> {
                    if (res.statusCode() == STATUS_NOT_FOUND) {
                        // no user exists
                        resultHandler.handle(failedFuture("No user exists by given email " + email));
                        return;
                    } else if (res.statusCode() != STATUS_SUCCESS) {
                        // service down
                        resultHandler.handle(failedFuture("Unable to connect to admin api!"));
                        return;
                    }

                    PlayerWithTeams playerObject = res.bodyAsJson(PlayerWithTeams.class);
                    LOG.debug("User received for email {}:  {}", email, playerObject);
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

                    UserInfo userInfo = UserInfo.create(email, jsonObject);
                    resultHandler.handle(succeededFuture(userInfo));

                }).onFailure(err -> resultHandler.handle(failedFuture(err)));
        return this;
    }

    @Override
    public DataService readSourceInfo(String token, Handler<AsyncResult<EventSource>> resultHandler) {
        webClient.getAbs(getEventSourceInfoUrl)
                .addQueryParam(QUERY_PARAM_TOKEN, token)
                .addQueryParam(QUERY_PARAM_WITH_KEY, TRUE)
                .putHeader(HEADER_APP_ID, apiKey)
                .putHeader(HEADER_APP_KEY, secretKey)
                .putHeader(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .send()
                .onSuccess(res -> {
                    if (res.statusCode() == STATUS_NOT_FOUND) {
                        // no user exists
                        resultHandler.handle(failedFuture("No event source exists by given token " + token));
                        return;
                    } else if (res.statusCode() != STATUS_SUCCESS) {
                        // service down
                        resultHandler.handle(failedFuture("Unable to connect to admin api!"));
                        return;
                    }

                    var eventSource = res.bodyAsJson(io.github.oasis.core.model.EventSource.class);
                    JsonObject jsonObject = new JsonObject()
                            .put(EventSource.ID, eventSource.getId())
                            .put(EventSource.TOKEN, token)
                            .put(EventSource.GAMES, new JsonArray(new ArrayList<>(eventSource.getGames())));
                    if (eventSource.getSecrets() != null && Texts.isNotEmpty(eventSource.getSecrets().getPublicKey())) {
                        jsonObject.put(EventSource.KEY, eventSource.getSecrets().getPublicKey());
                        EventSource info = EventSource.create(token, jsonObject);
                        resultHandler.handle(succeededFuture(info));
                    } else {
                        resultHandler.handle(failedFuture(new OasisRuntimeException("The public key not received for source " + token)));
                    }
                }).onFailure(err -> resultHandler.handle(failedFuture(err)));
        return this;
    }

    @Override
    public DataService readGameInfo(int gameId, Handler<AsyncResult<GameInfo>> resultHandler) {
        webClient.getAbs(getGameInfoUrl + "/" + gameId)
                .putHeader(HEADER_APP_ID, apiKey)
                .putHeader(HEADER_APP_KEY, secretKey)
                .putHeader(HEADER_ACCEPT, MEDIA_TYPE_JSON)
                .send()
                .onSuccess(res -> {
                    if (res.statusCode() == STATUS_NOT_FOUND) {
                        // no user exists
                        resultHandler.handle(failedFuture("No game exists by given game id " + gameId));
                        return;
                    } else if (res.statusCode() != STATUS_SUCCESS) {
                        // service down
                        resultHandler.handle(failedFuture("Unable to connect to admin api to get game info!"));
                        return;
                    }

                    var game = res.bodyAsJson(Game.class);
                    JsonObject jsonObject = new JsonObject()
                            .put(GameInfo.ID, game.getId())
                            .put(GameInfo.START_TIME, ObjectUtils.firstNonNull(game.getStartTime(), game.getCreatedAt()))
                            .put(GameInfo.END_TIME, ObjectUtils.firstNonNull(game.getEndTime(), Long.MAX_VALUE - 1));
                    GameInfo gameInfo = GameInfo.create(game.getId(), jsonObject);
                    resultHandler.handle(succeededFuture(gameInfo));
                }).onFailure(err -> resultHandler.handle(failedFuture(err)));
        return this;
    }
}

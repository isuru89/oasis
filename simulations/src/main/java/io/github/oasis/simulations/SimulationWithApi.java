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

package io.github.oasis.simulations;

import io.github.oasis.core.Game;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.EventSourceSecrets;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserObject;
import io.github.oasis.simulations.model.Team;
import io.github.oasis.simulations.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public class SimulationWithApi extends Simulation {

    static final int PORT = 8050;

    private HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    protected int createEventSourceToken() throws Exception {
        sourceKeyPair = generateKeyPair();
        EventSource source = new EventSource();
        source.setName(SOURCE_NAME);
        source.setToken(SOURCE_TOKEN);
        source.setGames(Set.of(GAME_ID));
        EventSourceSecrets sourceSecrets = new EventSourceSecrets();
        sourceSecrets.setPublicKey(Base64.getEncoder().encodeToString(sourceKeyPair.getPublic().getEncoded()));
        sourceSecrets.setPrivateKey(Base64.getEncoder().encodeToString(sourceKeyPair.getPrivate().getEncoded()));
        source.setSecrets(sourceSecrets);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(context.getAdminApiUrl() + "/admin/event-sources"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(source)))
                .build();

        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() > 200) {
            System.out.println(result.statusCode());
            throw new IllegalStateException("Cannot add new event source!");
        }
        EventSource eventSource = gson.fromJson(result.body(), EventSource.class);
        return eventSource.getId();
    }

    @Override
    protected int createGame() throws Exception {
        Game game = new Game();
        game.setName("Stackoverflow-Game");
        game.setMotto("Keep Help!");
        game.setDescription("This game simulated stackoverflow reputation system");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(context.getAdminApiUrl() + "/admin/games"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(game)))
                .build();

        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() > 200) {
            throw new IllegalStateException("Cannot add new event source!");
        }
        return gson.fromJson(result.body(), Game.class).getId();
    }

    @Override
    protected Map<String, Integer> persistTeams(List<Team> teams) throws IOException {
        try {
            Map<String, Integer> teamMap = new HashMap<>();
            for (Team team : teams) {
                TeamObject teamObject = new TeamObject();
                teamObject.setGameId(GAME_ID);
                teamObject.setName(team.getName());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(context.getAdminApiUrl() + "/admin/teams"))
                        .timeout(Duration.ofSeconds(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(teamObject)))
                        .build();
                HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add team " + team);
                }
                TeamObject dbTeam = gson.fromJson(result.body(), TeamObject.class);
                System.out.println("Added team " + dbTeam);
                teamMap.put(dbTeam.getName(), dbTeam.getTeamId());
            }
            return teamMap;
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected void persistUsers(List<User> users) throws IOException {
        try {
            for (User user : users) {
                System.out.println(user);
                UserObject userObject = new UserObject();
                userObject.setEmail(user.getEmail());
                userObject.setDisplayName(user.getName());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(context.getAdminApiUrl() + "/admin/users"))
                        .timeout(Duration.ofSeconds(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(userObject)))
                        .build();
                HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add user " + user);
                }
                UserObject dbUser = gson.fromJson(result.body(), UserObject.class);
                System.out.println("Added user " + dbUser);

                long teamId = user.getGames().get(String.valueOf(GAME_ID)).getTeam();
                Map<String, Object> dataReq = Map.of("userId", dbUser.getUserId(),
                        "gameId", GAME_ID,
                        "teamId", teamId);

                request = HttpRequest.newBuilder()
                        .uri(URI.create(context.getAdminApiUrl() + "/admin/users/" + dbUser.getUserId() + "/teams"))
                        .timeout(Duration.ofSeconds(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataReq)))
                        .build();
                HttpResponse<Void> resultTeamAdd = client.send(request, HttpResponse.BodyHandlers.discarding());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add user to team " + user);
                }
                System.out.println("Added user to team " + dbUser.getUserId() + " to team " + teamId);
            }
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected void announceGame(PersistedDef def) throws Exception {
        super.announceGame(def);
    }

    @Override
    protected void announceRule(PersistedDef def) throws Exception {
        super.announceRule(def);
    }

    @Override
    protected void sendEvent(PersistedDef def) throws Exception {
        System.out.println(">>> sending event " + def);
        Map<String, Object> body = Map.of("data", def.getData());
        String msg = gson.toJson(body);
        String signature = signPayload(msg);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(context.getApiUrl() + "/api/event"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SOURCE_TOKEN + ":" + signature)
                .PUT(HttpRequest.BodyPublishers.ofString(msg))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .exceptionally(new Function<Throwable, Void>() {
                    @Override
                    public Void apply(Throwable throwable) {
                        throwable.printStackTrace();
                        return null;
                    }
                });

    }

    private String signPayload(String msg) throws Exception {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        Signature sha1withRSA = Signature.getInstance("SHA1withRSA");
        sha1withRSA.initSign(sourceKeyPair.getPrivate());
        sha1withRSA.update(data);
        return Base64.getEncoder().encodeToString(sha1withRSA.sign());
    }
}

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
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.simulations.model.Team;
import io.github.oasis.simulations.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.*;
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
        EventSource source = new EventSource();
        source.setName(SOURCE_NAME);
        source.setGames(Set.of(GAME_ID));

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

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(eventSource.getSecrets().getPublicKey())));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(eventSource.getSecrets().getPrivateKey())));
        sourceKeyPair = new KeyPair(publicKey, privateKey);
        SOURCE_TOKEN = eventSource.getToken();
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
                teamMap.put(dbTeam.getName(), dbTeam.getId());
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
                PlayerObject playerObject = new PlayerObject();
                playerObject.setEmail(user.getEmail());
                playerObject.setDisplayName(user.getName());

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(context.getAdminApiUrl() + "/admin/users"))
                        .timeout(Duration.ofSeconds(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(playerObject)))
                        .build();
                HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add user " + user);
                }
                PlayerObject dbUser = gson.fromJson(result.body(), PlayerObject.class);
                System.out.println("Added user " + dbUser);

                long teamId = user.getGames().get(String.valueOf(GAME_ID)).getTeam();
                Map<String, Object> dataReq = Map.of("userId", dbUser.getId(),
                        "gameId", GAME_ID,
                        "teamId", teamId);

                request = HttpRequest.newBuilder()
                        .uri(URI.create(context.getAdminApiUrl() + "/admin/users/" + dbUser.getId() + "/teams"))
                        .timeout(Duration.ofSeconds(2))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(dataReq)))
                        .build();
                HttpResponse<Void> resultTeamAdd = client.send(request, HttpResponse.BodyHandlers.discarding());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add user to team " + user);
                }
                System.out.println("Added user to team " + dbUser.getId() + " to team " + teamId);
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

    @Override
    protected void cleanUpAllResources() {
//        try {
//            {
//                System.out.println("Deleting event source...");
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(context.getAdminApiUrl() + "/admin/event-sources/" + SOURCE_ID))
//                        .timeout(Duration.ofSeconds(2))
//                        .header("Content-Type", "application/json")
//                        .DELETE()
//                        .build();
//
//                client.send(request, HttpResponse.BodyHandlers.discarding());
//            }
//
//            {
//                System.out.println("Deleting game...");
//                HttpRequest request = HttpRequest.newBuilder()
//                        .uri(URI.create(context.getAdminApiUrl() + "/admin/games/" + GAME_ID))
//                        .timeout(Duration.ofSeconds(2))
//                        .header("Content-Type", "application/json")
//                        .DELETE()
//                        .build();
//
//                client.send(request, HttpResponse.BodyHandlers.discarding());
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}

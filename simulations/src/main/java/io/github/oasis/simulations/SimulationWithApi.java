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
import io.github.oasis.core.elements.AttributeInfo;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.model.UserGender;
import io.github.oasis.core.parser.GameParserYaml;
import io.github.oasis.simulations.model.Team;
import io.github.oasis.simulations.model.User;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;

/**
 * @author Isuru Weerarathna
 */
public class SimulationWithApi extends Simulation {

    private HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    private HttpRequest.Builder adminApiReq(String subUrl) {
        return HttpRequest.newBuilder()
                .uri(URI.create(context.getAdminApiUrl() + subUrl))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .header("X-APP-ID", context.getAdminApiAppId())
                .header("X-APP-KEY", context.getAdminApiSecret());
    }

    @Override
    protected int createEventSourceToken() throws Exception {
        EventSource source = new EventSource();
        source.setName(SOURCE_NAME);

        HttpRequest request = adminApiReq("/admin/event-sources")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(source)))
                .build();

        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() >= 300) {
            System.out.println(result.statusCode());
            System.out.println(result.body());
            throw new IllegalStateException("Cannot add new event source! ");
        }

        EventSource eventSource = mapper.readValue(result.body(), EventSource.class);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(eventSource.getSecrets().getPublicKey())));
        PrivateKey privateKey = keyFactory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(eventSource.getSecrets().getPrivateKey())));
        sourceKeyPair = new KeyPair(publicKey, privateKey);
        SOURCE_TOKEN = eventSource.getToken();


        result = client.send(adminApiReq("/admin/games/" + GAME_ID + "/event-sources/" + eventSource.getId())
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build(), HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() >= 300) {
            System.out.println(result.body());
            throw new IllegalStateException("Unable to associate game with event-source!");
        }

        return eventSource.getId();
    }

    @Override
    protected int createGame() throws Exception {
        Game game = new Game();
        game.setName("Stackoverflow-Game");
        game.setMotto("Keep Help!");
        game.setDescription("This game simulated stackoverflow reputation system");

        HttpRequest request = adminApiReq("/games")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(game)))
                .build();

        HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (result.statusCode() >= 300) {
            System.out.println(result.statusCode());
            System.out.println(result.body());
            throw new IllegalStateException("Cannot add new game!");
        }

        int gameId = mapper.readValue(result.body(), Game.class).getId();
        System.out.println("Game added: " + gameId);

        AttributeInfo gold = AttributeInfo.builder().id(1).name("Gold").priority(1).build();
        AttributeInfo silver = AttributeInfo.builder().id(2).name("Silver").priority(2).build();
        AttributeInfo bronze = AttributeInfo.builder().id(3).name("Bronze").priority(3).build();

        client.send(adminApiReq("/games/" + gameId + "/attributes")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(gold)))
                .build(), HttpResponse.BodyHandlers.discarding());
        client.send(adminApiReq("/games/" + gameId + "/attributes")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(silver)))
                .build(), HttpResponse.BodyHandlers.discarding());
        client.send(adminApiReq("/games/" + gameId + "/attributes")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(bronze)))
                .build(), HttpResponse.BodyHandlers.discarding());

        System.out.println("Game attributes added!");
        defineGameRules(gameId);

        return gameId;
    }

    private void defineGameRules(int gameId) throws IOException, InterruptedException {
        File rules = context.getGameDataDir().toPath().resolve("rules").resolve("rules.yml").toFile();
        GameDef gameDef = GameParserYaml.fromFile(rules);
        for (EngineMessage ruleDefinition : gameDef.getRuleDefinitions()) {
            String id = ruleDefinition.getData().get("id").toString();
            String name = ruleDefinition.getData().get("name").toString();
            String description = ruleDefinition.getData().get("description").toString();
            ElementDef elementDef = ElementDef.builder()
                    .type(ruleDefinition.getType())
                    .impl(ruleDefinition.getImpl())
                    .gameId(gameId)
                    .data(ruleDefinition.getData())
                    .elementId(id)
                    .metadata(new SimpleElementDefinition(id, name, description))
                    .build();

            System.out.println("Adding rule: " + elementDef);

            HttpResponse<String> result = client.send(adminApiReq("/games/" + gameId + "/elements")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(elementDef)))
                    .build(), HttpResponse.BodyHandlers.ofString());
            System.out.println("Add result: " + result.body());
        }
    }

    @Override
    protected Map<String, Integer> persistTeams(List<Team> teams) throws IOException {
        try {
            Map<String, Integer> teamMap = new HashMap<>();
            for (Team team : teams) {
                TeamObject teamObject = new TeamObject();
                teamObject.setGameId(GAME_ID);
                teamObject.setName(team.getName());

                System.out.println("Adding team: " + teamObject);

                HttpRequest request = adminApiReq("/teams")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(teamObject)))
                        .build();
                HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add team " + team);
                }
                TeamObject dbTeam = mapper.readValue(result.body(), TeamObject.class);
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
            Random random = new Random(1L);
            for (User user : users) {
                System.out.println(user);
                PlayerObject playerObject = new PlayerObject();
                playerObject.setEmail(user.getEmail());
                playerObject.setDisplayName(user.getName());
                playerObject.setTimeZone("UTC");
                playerObject.setGender(random.nextInt(2) % 2 == 0 ? UserGender.MALE : UserGender.FEMALE);

                HttpRequest request = adminApiReq("/players")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(playerObject)))
                        .build();
                HttpResponse<String> result = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (result.statusCode() >= 400) {
                    throw new IOException("Unable to add user " + user);
                }
                PlayerObject dbUser = mapper.readValue(result.body(), PlayerObject.class);
                System.out.println("Added user " + dbUser);

                long teamId = user.getGames().get(String.valueOf(GAME_ID)).getTeam();
                Map<String, Object> dataReq = Map.of("userId", dbUser.getId(),
                        "gameId", GAME_ID,
                        "teamId", teamId);

                request = adminApiReq("/players/" + dbUser.getId() + "/teams")
                        .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(dataReq)))
                        .build();
                HttpResponse<Void> resultTeamAdd = client.send(request, HttpResponse.BodyHandlers.discarding());
                if (resultTeamAdd.statusCode() >= 400) {
                    throw new IOException("Unable to add user to team " + user);
                }
                System.out.println("Added user to team " + dbUser.getId() + " to team " + teamId);
            }
        } catch (InterruptedException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    @Override
    protected void dispatchGameStart() throws Exception {
        System.out.println("Announcing game start...");

        HttpRequest startRequest = adminApiReq("/games/" + GAME_ID + "/start")
                .PUT(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> startResponse = client.send(startRequest, HttpResponse.BodyHandlers.ofString());
        if (startResponse.statusCode() >= 300) {
            System.out.println(startResponse.body());
            throw new IllegalStateException("Game start failed!");
        }

        System.out.println("Game started successfully!");
    }

    @Override
    protected void dispatchRules() {
        // do nothing
    }

    @Override
    protected void defineAllGameRules() {

    }

    @Override
    protected void announceGame(EngineMessage def) throws Exception {

    }

    @Override
    protected void announceRule(EngineMessage def) throws Exception {
        super.announceRule(def);
    }

    @Override
    protected void sendEvent(EngineMessage def) throws Exception {
        System.out.println(">>> sending event " + def);
        Map<String, Object> body = Map.of("data", def.getData());
        String msg = mapper.writeValueAsString(body);
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

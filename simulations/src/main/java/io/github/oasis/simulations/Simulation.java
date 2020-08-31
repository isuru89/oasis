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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.github.oasis.core.Event;
import io.github.oasis.core.Game;
import io.github.oasis.core.external.EventDispatchSupport;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.engine.element.points.PointDef;
import io.github.oasis.simulations.model.Team;
import io.github.oasis.simulations.model.User;
import org.yaml.snakeyaml.Yaml;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class Simulation implements Closeable {

    static final String SOURCE_NAME = "oasis.simulation.internal";
    static final String SOURCE_TOKEN = UUID.randomUUID().toString().replace("-", "");
    static int SOURCE_ID = 1;
    static int GAME_ID = 1001;
    static final int TIME_RESOLUTION = 84000 * 5;

    protected final Gson gson = new Gson();

    private JedisPool dbPool;
    protected KeyPair sourceKeyPair;

    private File gameRootDir;
    protected SimulationContext context;
    private EventDispatchSupport dispatcher;

    private List<User> users = new ArrayList<>();
    private List<String> acceptedEvents = new ArrayList<>();
    Map<String, Integer> teamMapping;

    public void run(SimulationContext context) {
        try {
            this.context = context;
            this.dispatcher = context.getDispatcher();
            gameRootDir = context.getGameDataDir();

            bootstrapDb();

            GAME_ID = createGame();

            SOURCE_ID = createEventSourceToken();

            teamMapping = createTeams();

            createUsers();

            dbPool.close();
            Thread.sleep(3000);

            dispatchGameStart();
            dispatchRules();
            dispatchEvents();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            dispatchGameEnd();
        }
    }

    private void bootstrapDb() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(1);
        String host = "localhost";
        int port = 6379;
        dbPool = new JedisPool(config, host, port);
    }

    protected int createEventSourceToken() throws Exception {
        try (Jedis jedis = dbPool.getResource()) {
            sourceKeyPair = generateKeyPair();
            Map<String, Object> sourceMap = new HashMap<>();
            String key = Base64.getEncoder().encodeToString(sourceKeyPair.getPublic().getEncoded());
            System.out.println(key);
            sourceMap.put("token", SOURCE_TOKEN);
            sourceMap.put("key", key);
            sourceMap.put("games", Collections.singletonList(GAME_ID));
            sourceMap.put("id", SOURCE_ID);

            jedis.hset("oasis.sources", SOURCE_TOKEN, gson.toJson(sourceMap));
        }
        return SOURCE_ID;
    }

    protected int createGame() throws Exception {
        Game game = new Game();
        game.setId(GAME_ID);

        try (Jedis jedis = dbPool.getResource()) {
            jedis.hset("oasis.games", String.valueOf(game.getId()), gson.toJson(game));
        }
        return GAME_ID;
    }

    private void createUsers() throws IOException {
        List<User> users = Files.lines(Paths.get(gameRootDir.getAbsolutePath()).resolve("users.csv"))
                .map(l -> {
                    String[] parts = l.split("[,]");
                    User user = new User();
                    user.setId(Long.parseLong(parts[0]));
                    user.setName(parts[1]);
                    user.setEmail(parts[2]);
                    Integer teamId = teamMapping.get(parts[3]);
                    user.setGames(Map.of(String.valueOf(GAME_ID), new User.UserGame(teamId)));
                    return user;
                }).collect(Collectors.toList());
        this.users = users;

        persistUsers(users);
    }

    protected void persistUsers(List<User> users) throws IOException {
        try (Jedis jedis = dbPool.getResource()) {

            for (User user : users) {
                jedis.hset("oasis.users", user.getEmail(), gson.toJson(user));
            }
        }
    }

    private Map<String, Integer> createTeams() throws IOException {
        List<Team> teams = Files.lines(Paths.get(gameRootDir.getAbsolutePath()).resolve("teams.csv"))
                .map(l -> {
                    String[] parts = l.split("[,]");
                    return new Team(Integer.parseInt(parts[0]), parts[1]);
                }).collect(Collectors.toList());

        return persistTeams(teams);
    }

    protected Map<String, Integer> persistTeams(List<Team> teams) throws IOException {
        Map<String, Integer> teamMap = new HashMap<>();
        try (Jedis jedis = dbPool.getResource()) {
            for (Team team : teams) {
                jedis.hset("oasis.teams", String.valueOf(team.getId()), gson.toJson(team));
                teamMap.put(team.getName(), Integer.parseInt(String.valueOf(team.getId())));
            }
        }
        return teamMap;
    }

    protected KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
        return pairGenerator.generateKeyPair();
    }

    protected void announceGame(PersistedDef def) throws Exception {
        this.dispatcher.broadcast(def);
    }

    protected void announceRule(PersistedDef def) throws Exception {
        this.dispatcher.push(def);
    }

    protected void sendEvent(PersistedDef def) throws Exception {
        this.dispatcher.push(def);
    }

    private void dispatchGameStart() throws Exception {
        PersistedDef.Scope scope = new PersistedDef.Scope(GAME_ID);
        PersistedDef gameCreatedDef = new PersistedDef();
        gameCreatedDef.setType(PersistedDef.GAME_CREATED);
        gameCreatedDef.setScope(scope);
        announceGame(gameCreatedDef);

        PersistedDef gameStartCmd = new PersistedDef();
        gameStartCmd.setType(PersistedDef.GAME_STARTED);
        gameStartCmd.setScope(scope);
        announceGame(gameStartCmd);
    }

    private void dispatchGameEnd() {
        PersistedDef.Scope scope = new PersistedDef.Scope(GAME_ID);
        PersistedDef gameRemoved = new PersistedDef();
        gameRemoved.setType(PersistedDef.GAME_REMOVED);
        gameRemoved.setScope(scope);
        try {
            Thread.sleep(2000);
            announceGame(gameRemoved);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchRules() throws Exception {
        Yaml yaml = new Yaml();

        PersistedDef.Scope scope = new PersistedDef.Scope(GAME_ID);
        try (FileInputStream inputStream = new FileInputStream(new File(gameRootDir, "rules/points.yml"))) {
            Map<String, Object> loadedDef = yaml.load(inputStream);
            List<Map<String, Object>> pointDefs = (List<Map<String, Object>>) loadedDef.get("points");
            for (Map<String, Object> def : pointDefs) {
                PersistedDef persistedDef = new PersistedDef();
                persistedDef.setType(PersistedDef.GAME_RULE_ADDED);
                persistedDef.setImpl(PointDef.class.getName());
                persistedDef.setData(def);
                persistedDef.setScope(scope);
                acceptedEvents.add((String) def.get("event"));

                announceRule(persistedDef);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchEvents() throws Exception {
        PersistedDef.Scope scope = new PersistedDef.Scope(GAME_ID);
        long startTime = LocalDate.of(2019, Month.JUNE, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = LocalDate.of(2020, Month.APRIL, 30).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String eventDistribution = Files.readString(Paths.get(gameRootDir.getAbsolutePath()).resolve("eventtypes.json"));
        Map<String, Object> eventDist = gson.fromJson(eventDistribution, new TypeToken<Map<String, Object>>() {
        }.getType());
        int distribution = ((Number) eventDist.get("distribution")).intValue();
        Map<String, Double> items = ((List<List<Object>>) eventDist.get("events")).stream()
                .collect(Collectors.toMap(s -> (String) s.get(0), objects -> (Double) objects.get(1)));
        NavigableMap<Integer, String> distMap = new TreeMap<>();
        int curr = 0;
        for (Map.Entry<String, Double> entry : items.entrySet()) {
            int p = (int) Math.ceil(entry.getValue() * distribution);
            distMap.put(curr, entry.getKey());
            curr += p;
        }
        distMap.put(curr, "");

        Random random = new Random(23);
        long currTime = startTime;
        int events = 0;
        while (currTime < endTime) {
            int increment = random.nextInt(TIME_RESOLUTION * 1000);
            currTime += increment;

            int userIdx = random.nextInt(users.size());
            User userRef = users.get(userIdx);

            Map<String, Object> event = new HashMap<>();
            event.put(Event.GAME_ID, GAME_ID);
            event.put(Event.SOURCE_ID, SOURCE_ID);
            event.put(Event.ID, UUID.randomUUID().toString());
            event.put(Event.TIMESTAMP, currTime);
            event.put(Event.USER_ID, userRef.getId());
            event.put(Event.USER_NAME, userRef.getEmail());
            event.put(Event.TEAM_ID, userRef.getGames().get(String.valueOf(GAME_ID)).getTeam());

            int eventIdx = random.nextInt(distribution);
            String eventType = distMap.floorEntry(eventIdx).getValue();
            if (eventType.isEmpty()) {
                // currTime -= increment;
                continue;
            }
            event.put(Event.EVENT_TYPE, eventType);

            PersistedDef def = new PersistedDef();
            def.setType(PersistedDef.GAME_EVENT);
            def.setScope(scope);
            def.setData(event);
            sendEvent(def);
            events++;
        }

        System.out.println(">>> Event Count " + events);
    }

    @Override
    public void close() {
        if (dbPool != null) {
            dbPool.close();
        }
    }
}

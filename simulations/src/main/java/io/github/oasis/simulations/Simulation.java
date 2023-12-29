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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.core.Event;
import io.github.oasis.core.Game;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.engine.element.points.PointDef;
import io.github.oasis.simulations.model.Team;
import io.github.oasis.simulations.model.User;
import org.redisson.Redisson;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.yaml.snakeyaml.Yaml;

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

    static final String SOURCE_NAME = "oasis_simulation_internal";
    static String SOURCE_TOKEN;
    static int SOURCE_ID = 1;
    static int GAME_ID = 1001;
    static final int TIME_RESOLUTION = 84000 * 5;

    protected final ObjectMapper mapper = new ObjectMapper();

    private RedissonClient dbPool;
    protected KeyPair sourceKeyPair;

    private File gameRootDir;
    protected SimulationContext context;
    private EventDispatcher dispatcher;

    private List<User> users = new ArrayList<>();
    private List<String> acceptedEvents = new ArrayList<>();
    Map<String, Integer> teamMapping;

    public void run(SimulationContext context) {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            this.context = context;
            this.dispatcher = context.getDispatcher();
            gameRootDir = context.getGameDataDir();

            bootstrapDb();

            GAME_ID = createGame();

            SOURCE_ID = createEventSourceToken();

            teamMapping = createTeams();

            createUsers();

            defineAllGameRules();

            dbPool.shutdown();
            Thread.sleep(3000);

            dispatchGameStart();
            dispatchRules();

            System.out.println("Waiting for 5 seconds before event publishing");
            // wait some time until all rules are published
            Thread.sleep(5000);

            dispatchEvents();

        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        } finally {
            dispatchGameEnd();
            cleanUpAllResources();
        }
    }

    protected void cleanUpAllResources() {

    }

    protected void defineAllGameRules() {

    }

    private void bootstrapDb() {
//        JedisPoolConfig config = new JedisPoolConfig();
//        config.setMaxTotal(1);
        String host = "localhost";
        int port = 6379;
        dbPool = Redisson.create();
    }

    protected int createEventSourceToken() throws Exception {
//        try (Jedis jedis = dbPool.getResource()) {
            sourceKeyPair = generateKeyPair();
            Map<String, Object> sourceMap = new HashMap<>();
            String key = Base64.getEncoder().encodeToString(sourceKeyPair.getPublic().getEncoded());
            System.out.println(key);
            sourceMap.put("token", SOURCE_TOKEN);
            sourceMap.put("key", key);
            sourceMap.put("games", Collections.singletonList(GAME_ID));
            sourceMap.put("id", SOURCE_ID);

            RMap<String, String> map = dbPool.getMap("oasis.sources");
            map.fastPut(SOURCE_TOKEN, mapper.writeValueAsString(sourceMap));
//        }
        return SOURCE_ID;
    }

    protected int createGame() throws Exception {
        Game game = new Game();
        game.setId(GAME_ID);

        RMap<String, String> map = dbPool.getMap("oasis.games");
        map.fastPut(String.valueOf(game.getId()), mapper.writeValueAsString(game));
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

        RMap<String, String> map = dbPool.getMap("oasis.users");
            for (User user : users) {
                map.put(user.getEmail(), mapper.writeValueAsString(user));
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

        RMap<String, String> map = dbPool.getMap("oasis.teams");
            for (Team team : teams) {
                map.put(String.valueOf(team.getId()), mapper.writeValueAsString(team));
                teamMap.put(team.getName(), Integer.parseInt(String.valueOf(team.getId())));
            }
        return teamMap;
    }

    protected KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
        return pairGenerator.generateKeyPair();
    }

    protected void announceGame(EngineMessage def) throws Exception {
        this.dispatcher.broadcast(def);
    }

    protected void announceRule(EngineMessage def) throws Exception {
        this.dispatcher.push(def);
    }

    protected void sendEvent(EngineMessage def) throws Exception {
        this.dispatcher.push(def);
    }

    protected void dispatchGameStart() throws Exception {
        EngineMessage.Scope scope = new EngineMessage.Scope(GAME_ID);
        EngineMessage gameCreatedDef = new EngineMessage();
        gameCreatedDef.setType(EngineMessage.GAME_CREATED);
        gameCreatedDef.setScope(scope);
        announceGame(gameCreatedDef);

        EngineMessage gameStartCmd = new EngineMessage();
        gameStartCmd.setType(EngineMessage.GAME_STARTED);
        gameStartCmd.setScope(scope);
        announceGame(gameStartCmd);
    }

    private void dispatchGameEnd() {
        EngineMessage.Scope scope = new EngineMessage.Scope(GAME_ID);
        EngineMessage gameRemoved = new EngineMessage();
        gameRemoved.setType(EngineMessage.GAME_REMOVED);
        gameRemoved.setScope(scope);
        try {
            Thread.sleep(2000);
            announceGame(gameRemoved);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    protected void dispatchRules() throws Exception {
        Yaml yaml = new Yaml();

        EngineMessage.Scope scope = new EngineMessage.Scope(GAME_ID);
        try (FileInputStream inputStream = new FileInputStream(new File(gameRootDir, "rules/points.yml"))) {
            Map<String, Object> loadedDef = yaml.load(inputStream);
            List<Map<String, Object>> pointDefs = (List<Map<String, Object>>) loadedDef.get("points");
            for (Map<String, Object> def : pointDefs) {
                EngineMessage engineMessage = new EngineMessage();
                engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
                engineMessage.setImpl(PointDef.class.getName());
                engineMessage.setData(def);
                engineMessage.setScope(scope);
                acceptedEvents.add((String) def.get("event"));

                announceRule(engineMessage);
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private void dispatchEvents() throws Exception {
        EngineMessage.Scope scope = new EngineMessage.Scope(GAME_ID);
        long startTime = LocalDate.of(2019, Month.JUNE, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = LocalDate.of(2020, Month.APRIL, 30).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String eventDistribution = Files.readString(Paths.get(gameRootDir.getAbsolutePath()).resolve("eventtypes.json"));
        Map<String, Object> eventDist = mapper.readValue(eventDistribution, HashMap.class);
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

            EngineMessage def = new EngineMessage();
            def.setType(EngineMessage.GAME_EVENT);
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
            dbPool.shutdown();
        }
    }
}

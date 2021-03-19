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
import com.typesafe.config.Config;
import com.typesafe.config.ConfigObject;
import io.github.oasis.core.Event;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.external.messages.GameState;
import io.github.oasis.core.parser.GameParserYaml;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.engine.EngineContext;
import io.github.oasis.engine.OasisEngine;
import io.github.oasis.engine.element.points.PointsModuleFactory;
import io.github.oasis.ext.rabbitstream.RabbitStreamFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.ZoneId;
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
public class Main {

    private static int[] U_IDS = new int[]{1050301,1059853,1061102,1098316,1514544,1588583,1614668,1617801,1989475,
            2148133,2183595,2810754,2894896,3065424,3156130,3165645,3229716,3285789,3286999,3353371,3355195,3466863,
            3522956,3647460,3929807,4007478,4131805,4198991,4332695,4392625,4407969,4430262,4538001,4611817,4674526,
            4691905,4791525,4942852,4968346,5135073,5208322,5612973,5729324,5762293,5785803,5811542,5838425,5845172,
            6072217,6125919,6209267,6323348,6343546,6423187,6542187,6646899,6688779,7023250,7105641,7117953,7273838,
            7341127,7430088,7495521,7502169,7503758,7548005,7582360,7820933,7872441,8004722,8037093,8160577,8222827,
            8234645,8314397,8326561,8388034,8451333,8534613,8755304,8868531,8887080,8896026,9012993,9089385,9090365,
            9143533,9208860,9285959,9367045,9465759,9543690,9741445,9782408,9808481,9830524,9923983,9944043,9972772};

    static final int TIME_RESOLUTION = 84000 * 5;

    protected static final ObjectMapper mapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "api".equals(args[0])) {
            runToEventsAPI();
        } else {
            runToEngine();
        }
    }

    private static void bareSetup() throws Exception {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        OasisConfigs configs = OasisConfigs.defaultConfigs();

        // initialize dispatcher first
        EventDispatcher dispatcher = initializeDispatcher(configs.getConfigRef());

        SimulationContext simulationContext = new SimulationContext();
        simulationContext.setGameDataDir(new File("./simulations/stackoverflow"));
        simulationContext.setDispatcher(dispatcher);
        simulationContext.setApiUrl("http://localhost:8050");
        simulationContext.setAdminApiUrl("http://localhost:8081/api");
        simulationContext.setAdminApiAppId("root");
        simulationContext.setAdminApiSecret("root");

        defineGameRules(1, simulationContext);

        Thread.sleep(5000);

        dispatchEvents(1, simulationContext);

        dispatcher.close();
    }


    @SuppressWarnings("unchecked")
    private static void dispatchEvents(int gameId, SimulationContext context) throws Exception {
        EngineMessage.Scope scope = new EngineMessage.Scope(gameId);
        long startTime = LocalDate.of(2020, Month.JULY, 1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long endTime = LocalDate.of(2021, Month.MARCH, 15).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        String eventDistribution = Files.readString(Paths.get(context.getGameDataDir().getAbsolutePath()).resolve("eventtypes.json"));
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

        Random random = new Random(223);
        Map<Integer, Integer> userTeams = new HashMap<>();
        for (Integer uid : U_IDS) {
            userTeams.put(uid, random.nextInt(8) + 1);
        }

        long currTime = startTime;
        int events = 0;
        while (currTime < endTime) {
            int increment = random.nextInt(TIME_RESOLUTION * 1000);
            currTime += increment;

            int userIdx = random.nextInt(U_IDS.length);
            int userId = U_IDS[userIdx];

            Map<String, Object> event = new HashMap<>();
            event.put(Event.GAME_ID, gameId);
            event.put(Event.SOURCE_ID, 1);
            event.put(Event.ID, UUID.randomUUID().toString());
            event.put(Event.TIMESTAMP, currTime);
            event.put(Event.USER_ID, userId);
            event.put(Event.TEAM_ID, userTeams.get(userId));

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

            System.out.println(">>>>> publishing event " + def);
            context.getDispatcher().push(def);

            events++;
        }

        System.out.println(">>> Event Count " + events);
    }

    private static void defineGameRules(int gameId, SimulationContext context) throws Exception {
        context.getDispatcher().broadcast(EngineMessage.createGameLifecycleEvent(gameId, GameState.CREATED));

        File rules = context.getGameDataDir().toPath().resolve("rules").resolve("rules.yml").toFile();
        GameDef gameDef = GameParserYaml.fromFile(rules);
        for (EngineMessage ruleDefinition : gameDef.getRuleDefinitions()) {
            EngineMessage engineMessage = new EngineMessage();
            engineMessage.setData(ruleDefinition.getData());
            engineMessage.setType(EngineMessage.GAME_RULE_ADDED);
            engineMessage.setImpl(ruleDefinition.getImpl());
            engineMessage.setScope(new EngineMessage.Scope(gameId));

            System.out.println("Adding rule: " + engineMessage);

            context.getDispatcher().broadcast(engineMessage);
        }

        context.getDispatcher().broadcast(EngineMessage.createGameLifecycleEvent(gameId, GameState.STARTED));
    }

    private static void runToEventsAPI() throws Exception {
        OasisConfigs configs = OasisConfigs.defaultConfigs();

        // initialize dispatcher first
        EventDispatcher dispatcher = initializeDispatcher(configs.getConfigRef());

        SimulationContext simulationContext = new SimulationContext();
        simulationContext.setGameDataDir(new File("./simulations/stackoverflow"));
        simulationContext.setDispatcher(dispatcher);
        simulationContext.setApiUrl("http://localhost:8050");
        simulationContext.setAdminApiUrl("http://localhost:8081/api");
        simulationContext.setAdminApiAppId("root");
        simulationContext.setAdminApiSecret("root");
        Simulation simulation = new SimulationWithApi();
        simulation.run(simulationContext);

        dispatcher.close();
    }

    private static void runToEngine() throws Exception {
        OasisConfigs configs = OasisConfigs.defaultConfigs();
        EngineContext.Builder builder = EngineContext.builder()
                .withConfigs(configs)
                .installModule(PointsModuleFactory.class);
        Db dbPool = RedisDb.create(configs);
        dbPool.init();
        builder.withDb(dbPool);

        // initialize dispatcher first
        EventDispatcher dispatcher = initializeDispatcher(configs.getConfigRef());

        OasisEngine engine = new OasisEngine(builder.build());
        engine.start();

        SimulationContext simulationContext = new SimulationContext();
        simulationContext.setGameDataDir(new File("./simulations/stackoverflow"));
        simulationContext.setDispatcher(dispatcher);
        Simulation simulation = new Simulation();
        simulation.run(simulationContext);
    }

    private static EventDispatcher initializeDispatcher(Config configs) throws Exception {
        RabbitStreamFactory streamFactory = new RabbitStreamFactory();
        EventDispatcher dispatcher = streamFactory.getDispatcher();
        ConfigObject dispatcherConfigs = configs.getObject("oasis.dispatcher.configs");
        Map<String, Object> conf = dispatcherConfigs.unwrapped();
        dispatcher.init(() -> conf);
        return dispatcher;
    }

}

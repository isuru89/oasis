/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.engine;

import akka.actor.ActorRef;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.elements.SimpleElementDefinition;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.OasisRepository;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;
import io.github.oasis.core.parser.GameParserYaml;
import io.github.oasis.core.services.api.beans.JsonSerializer;
import io.github.oasis.core.services.api.beans.RedisRepository;
import io.github.oasis.core.services.api.beans.jdbc.JdbcMetadataProvider;
import io.github.oasis.core.services.helpers.OasisMetadataSupport;
import io.github.oasis.db.redis.RedisDb;
import io.github.oasis.db.redis.RedisEventLoaderHandler;
import io.github.oasis.elements.badges.BadgesModuleFactory;
import io.github.oasis.elements.challenges.ChallengesModuleFactory;
import io.github.oasis.elements.milestones.MilestonesModuleFactory;
import io.github.oasis.elements.ratings.RatingsModuleFactory;
import io.github.oasis.engine.actors.cmds.Messages;
import io.github.oasis.engine.element.points.PointsModuleFactory;
import io.github.oasis.engine.model.TEvent;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class OasisEngineTest {

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    protected final ObjectMapper mapper = new ObjectMapper();

    private static final String TEST_SYSTEM = "test-oasis-system";

    protected static final String EVT_A = "event.a";
    protected static final String EVT_B = "event.b";
    private static final double AMOUNT_10 = 10.0;
    private static final double AMOUNT_50 = 50.0;

    static final long U1 = 1;
    static final long U2 = 2;
    static final long U3 = 3;
    static final long U4 = 4;
    static final long U5 = 5;

    protected OasisEngine engine;

    protected Db dbPool;
    protected OasisMetadataSupport metadataSupport;
    protected OasisRepository oasisRepository;

    @BeforeEach
    public void setup() throws IOException, OasisException {
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        var jsonSerializer = new JsonSerializer(mapper);

        EngineContext.Builder builder = EngineContext.builder();
        OasisConfigs oasisConfigs = new OasisConfigs.Builder().buildFromYamlResource("test-defaults.yml");
        dbPool = RedisDb.create(oasisConfigs, "oasis.redis");
        dbPool.init();

        RedisRepository redisRepository = new RedisRepository(dbPool, jsonSerializer);
        oasisRepository = redisRepository;
        var metadataProvider = new JdbcMetadataProvider(oasisRepository);
        metadataProvider.setSelf(metadataProvider);
        metadataSupport = metadataProvider;

        EngineContext context = builder.withConfigs(oasisConfigs)
                .havingId("test-engine")
                .withDb(dbPool)
                .withEventStore(new RedisEventLoaderHandler(dbPool, oasisConfigs))
                .installModule(RatingsModuleFactory.class)
                .installModule(PointsModuleFactory.class)
                .installModule(MilestonesModuleFactory.class)
                .installModule(ChallengesModuleFactory.class)
                .installModule(BadgesModuleFactory.class)
                .build();
        engine = new OasisEngine(context);
        engine.start();

        try (DbContext db = dbPool.createContext()) {
            db.allKeys("*").forEach(db::removeKey);

            redisRepository.addPlayer(new PlayerObject(1, "Jakob Floyd", "jakob@oasis.io"));
            redisRepository.addPlayer(new PlayerObject(2, "Thierry Hines", "thierry@oasis.io"));
            redisRepository.addPlayer(new PlayerObject(3, "Ray Glenn", "ray@oasis.io"));
            redisRepository.addPlayer(new PlayerObject(4, "Lilia Stewart", "lilia@oasis.io"));
            redisRepository.addPlayer(new PlayerObject(5, "Archer Roberts", "archer@oasis.io"));

            redisRepository.addTeam(TeamObject.builder().id(1).gameId(1).name("Warriors").build());

            setupDbBefore(db);
        }
    }

    public void setupDbBefore(DbContext db) throws IOException {

    }

    @AfterEach
    public void shutdown() {
        engine.stopGame(TEvent.GAME_ID);
    }

    protected void awaitTerminated() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected long TSZ(String timeStr, String tz) {
        return LocalDateTime.parse(timeStr, FORMATTER).atZone(ZoneId.of(tz)).toInstant().toEpochMilli();
    }

    protected long TS(String timeStr) {
        return LocalDateTime.parse(timeStr, FORMATTER).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    protected void submit(ActorRef actorRef, TEvent... events) {
        for (TEvent event : events) {
            actorRef.tell(event, actorRef);
        }
    }

    protected AbstractRule findRuleByName(List<AbstractRule> rules, String name) {
        return rules.stream().filter(rule -> rule.getName().equals(name)).findFirst().orElseThrow();
    }

    protected void addRulesToMetadata(int gameId, List<AbstractRule> rules) {
        addRulesToMetadata(gameId, rules, null);
    }

    protected void addRulesToMetadata(int gameId, List<AbstractRule> rules, GameDef gameDef) {
        for (AbstractRule rule : rules) {
            ElementDef.ElementDefBuilder elementDef = ElementDef.builder()
                    .elementId(rule.getId())
                    .gameId(gameId)
                    .metadata(new SimpleElementDefinition(rule.getId(), rule.getName(), rule.getDescription()));

            if (gameDef != null) {
                EngineMessage message = gameDef.getRuleDefinitions().stream()
                        .filter(r -> r.getData().get("id").equals(rule.getId())).findFirst()
                        .orElseThrow();
                elementDef.data(message.getData());
            }

            oasisRepository.addNewElement(gameId, elementDef.build());
        }
    }

    protected List<AbstractRule> submitRules(OasisEngine engine, int gameId, GameDef gameDef) {
        List<EngineMessage> ruleDefinitions = gameDef.getRuleDefinitions();
        List<AbstractRule> rules = new ArrayList<>();
        for (EngineMessage def : ruleDefinitions) {
            AbstractRule rule = engine.getContext().getParsers().parseToRule(def);
            ElementDef elementDef = ElementDef.builder()
                    .elementId(rule.getId())
                    .type(def.getType())
                    .data(def.getData())
                    .gameId(gameId)
                    .metadata(new SimpleElementDefinition(rule.getId(), rule.getName(), rule.getDescription()))
                    .build();
            oasisRepository.addNewElement(gameId, elementDef);
            engine.submit(Messages.createRuleAddMessage(gameId, rule, null));
            rules.add(rule);
        }
        return rules;
    }

    protected GameDef loadRulesFromResource(String location) {
        try {
            return GameParserYaml.fromClasspath(location, Thread.currentThread().getContextClassLoader());
        } catch (OasisParseException e) {
            throw new IllegalArgumentException("Unable to parse classpath resource! " + location, e);
        }
    }

    protected <T, R> void compareStatReqRes(String reqJsonFile, Class<T> reqClz,
                                            String resJsonFile, Class<R> resClz,
                                         TestExecutorFunction<T, R> executable) {
        ClassLoader clzLoader = Thread.currentThread().getContextClassLoader();
        try {
            String reqStr = IOUtils.resourceToString(reqJsonFile, StandardCharsets.UTF_8, clzLoader);
            String resStr = IOUtils.resourceToString(resJsonFile, StandardCharsets.UTF_8, clzLoader);

            T input = mapper.readValue(reqStr, reqClz);
            System.out.println("Request: " + mapper.writeValueAsString(input));
            R result = executable.run(input);
            String resJsonStr = mapper.writeValueAsString(result);
            System.out.println("Expected Response: " + mapper.writeValueAsString(mapper.readValue(resStr, resClz)));
            System.out.println("Actual Response: " + resJsonStr);
            Assertions.assertEquals(mapper.readTree(resStr), mapper.readTree(resJsonStr));

        } catch (Exception e) {
            Assertions.fail("Should not fail when comparing req/res jsons!", e);
        }
    }

    @FunctionalInterface
    public interface TestExecutorFunction<T, R> {

        R run(T input) throws Exception;

    }

}

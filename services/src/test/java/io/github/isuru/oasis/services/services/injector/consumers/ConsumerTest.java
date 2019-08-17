package io.github.isuru.oasis.services.services.injector.consumers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Envelope;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;
import io.github.isuru.oasis.model.handlers.output.ChallengeModel;
import io.github.isuru.oasis.model.handlers.output.MilestoneModel;
import io.github.isuru.oasis.model.handlers.output.MilestoneStateModel;
import io.github.isuru.oasis.model.handlers.output.RatingModel;
import io.github.isuru.oasis.model.handlers.output.PointModel;
import io.github.isuru.oasis.model.handlers.output.RaceModel;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.services.injector.MsgAcknowledger;
import io.github.isuru.oasis.services.utils.BufferedRecords;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class ConsumerTest {

    private static final String consumerTag = "testConsumer";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ConsumerContext consumerContext;
    private BufferedRecords feedBuffer;
    private IOasisDao dao;
    private MsgAcknowledger acknowledger;
    private Random random;

    @Before
    public void before() throws DbException {
        random = new Random(System.currentTimeMillis());
        feedBuffer = Mockito.mock(BufferedRecords.class);
        consumerContext = new ConsumerContext(2, feedBuffer);
        dao = Mockito.mock(IOasisDao.class);
        acknowledger = Mockito.mock(MsgAcknowledger.class);

        consumerContext.getInterceptor().init(dao);
    }

    @After
    public void after() {
        consumerContext.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testBadgeConsumer() throws IOException, DbException {
        BadgeConsumer consumer = new BadgeConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/batch/addBadge", consumer.getInsertScriptName());

        {
            BadgeModel model = new BadgeModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setEvents(Collections.singletonList(jsonEvent));
            model.setBadgeId(100L);
            model.setSubBadgeId("subBadge");
            model.setEventType(jsonEvent.getEventType());
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toBadgeDaoData(model));

            Mockito.verify(feedBuffer).push(Mockito.any());
        }

    }

    @SuppressWarnings("unchecked")
    @Test
    public void testChallengeConsumer() throws IOException, DbException {
        BaseConsumer consumer = new ChallengeConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/batch/addChallengeWinner", consumer.getInsertScriptName());

        {
            ChallengeModel model = new ChallengeModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setWinNo(2);
            model.setWonAt(System.currentTimeMillis() - 40000);
            model.setPoints(1500.0);
            model.setChallengeId(1L);
            model.setEventExtId(jsonEvent.getExternalId());
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toChallengeDaoData(model));
        }

        Mockito.verify(feedBuffer).push(Mockito.any());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMilestoneConsumer() throws IOException, DbException {
        BaseConsumer consumer = new MilestoneConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/batch/addMilestone", consumer.getInsertScriptName());

        {
            MilestoneModel model = new MilestoneModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setMaximumLevel(10);
            model.setMilestoneId(1L);
            model.setLevel(2);
            model.setEvent(jsonEvent);
            model.setEventType(jsonEvent.getEventType());
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toMilestoneDaoData(model));

            Mockito.verify(feedBuffer).push(Mockito.any());
        }

        consumer.close();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMilestoneStateConsumer() throws IOException, DbException {
        {
            BaseConsumer consumer = new MilestoneStateConsumer(dao, consumerContext, acknowledger);
            Assertions.assertThatThrownBy(consumer::getInsertScriptName).isInstanceOf(IllegalStateException.class);
            Assertions.assertThatThrownBy(() -> consumer.handle(null)).isInstanceOf(IllegalStateException.class);

            // positive state update
            MilestoneStateModel model = new MilestoneStateModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setValue(5000.0);
            model.setCurrBaseValue(4500.0);
            model.setNextValue(6000.0);
            model.setMilestoneId(1L);
            model.setGameId(jsonEvent.getGameId());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toMilestoneStateDaoData(model));

            consumer.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMilestoneStateLossConsumer() throws IOException, DbException {
        {
            BaseConsumer consumer = new MilestoneStateConsumer(dao, consumerContext, acknowledger);

            // positive state update
            MilestoneStateModel model = new MilestoneStateModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setLossUpdate(true);
            model.setLossValue(-500.0);
            model.setMilestoneId(1L);
            model.setGameId(jsonEvent.getGameId());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toMilestoneLossStateDaoData(model));

            consumer.close();
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStateConsumer() throws IOException, DbException {
        BaseConsumer consumer = new RatingConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/updateState", consumer.getInsertScriptName());

        {
            RatingModel model = new RatingModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setCurrentPoints(1200.0);
            model.setCurrentState(2);
            model.setCurrentStateName("a");
            model.setCurrentValue("5.0");
            model.setEvent(jsonEvent);
            model.setExtId(jsonEvent.getExternalId());
            model.setPreviousState(1);
            model.setPreviousStateName("b");
            model.setPrevStateChangedAt(System.currentTimeMillis() - 50000);
            model.setPreviousState(3);
            model.setCurrency(true);
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toStateDaoData(model));


            Mockito.verify(feedBuffer).push(Mockito.any());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testStateNoChangeConsumer() throws IOException, DbException {
        BaseConsumer consumer = new RatingConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/updateState", consumer.getInsertScriptName());

        {
            RatingModel model = new RatingModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setCurrentPoints(1200.0);
            model.setCurrentState(1);
            model.setCurrentStateName("b");
            model.setCurrentValue("5.0");
            model.setEvent(jsonEvent);
            model.setExtId(jsonEvent.getExternalId());
            model.setPreviousState(1);
            model.setPreviousStateName("b");
            model.setPrevStateChangedAt(System.currentTimeMillis() - 50000);
            model.setCurrency(true);
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toStateDaoData(model));

            // should not call to feed
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRaceConsumer() throws IOException, DbException {
        BaseConsumer consumer = new RaceConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/batch/addRaceAward", consumer.getInsertScriptName());

        {
            RaceModel model = new RaceModel();
            JsonEvent jsonEvent = randomJsonEvent(random);
            model.setPoints(2000.0);
            model.setRaceEndedAt(System.currentTimeMillis());
            model.setRaceId(1);
            model.setRaceStartedAt(System.currentTimeMillis() - 804000L);
            model.setRank(2);
            model.setScoredCount(54L);
            model.setScoredPoints(1200.0);
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toRaceData(model));

            Mockito.verify(feedBuffer).push(Mockito.any());
        }
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testPointConsumer() throws IOException, DbException {
        BaseConsumer consumer = new PointConsumer(dao, consumerContext, acknowledger);
        Assert.assertEquals("game/batch/addPoint", consumer.getInsertScriptName());

        {
            PointModel model = new PointModel();
            JsonEvent jsonEvent = randomJsonEvent(random);

            model.setRuleName("pointRule");
            model.setRuleId(10L);
            model.setCurrency(true);
            model.setAmount(1200.0);
            model.setEventType(jsonEvent.getEventType());
            model.setEvents(Collections.singletonList(jsonEvent));
            model.setGameId(jsonEvent.getGameId());
            model.setSourceId(jsonEvent.getSource());
            model.setTeamId(jsonEvent.getTeam());
            model.setTeamScopeId(jsonEvent.getTeamScope());
            model.setTs(System.currentTimeMillis());
            model.setUserId(jsonEvent.getUser());
            consumer.handleMessage(objToBytes(model), 1L);
            consumer.flushNow();

            ArgumentCaptor<List<Map<String, Object>>> arg = ArgumentCaptor.forClass(List.class);
            Mockito.verify(dao).executeBatchInsert(Mockito.anyString(), arg.capture());
            List<Map<String, Object>> value = arg.getValue();
            Assert.assertEquals(1, value.size());
            Map<String, Object> map = value.get(0);
            Assertions.assertThat(map).isNotEmpty()
                    .containsAllEntriesOf(ConsumerUtils.toPointDaoData(model));

            Mockito.verify(feedBuffer).push(Mockito.any());
        }
    }


    private byte[] objToBytes(Object obj) throws IOException {
        return MAPPER.writeValueAsBytes(obj);
    }

    static JsonEvent randomJsonEvent(Random random) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis());
        jsonEvent.put(Constants.FIELD_USER, random.nextInt(1000) + 1);
        jsonEvent.put(Constants.FIELD_GAME_ID, random.nextInt(5) + 1);
        jsonEvent.put(Constants.FIELD_SOURCE, random.nextInt(100) + 500);
        jsonEvent.put(Constants.FIELD_ID, UUID.randomUUID().toString());
        jsonEvent.put(Constants.FIELD_EVENT_TYPE, "oasis.test.");

        return jsonEvent;
    }

    private Envelope createEnvelope(long ms, String entity) {
        return new Envelope(ms, true, entity + ".exchange", entity);
    }
}

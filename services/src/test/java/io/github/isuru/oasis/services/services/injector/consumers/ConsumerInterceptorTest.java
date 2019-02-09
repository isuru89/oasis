package io.github.isuru.oasis.services.services.injector.consumers;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.services.services.injector.ConsumerContext;
import io.github.isuru.oasis.services.utils.BufferedRecords;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ConsumerInterceptorTest {

    @Test
    public void testEvents() {
        Random random = new Random(System.currentTimeMillis());

        IOasisDao dao = Mockito.mock(IOasisDao.class);
        ConsumerContext consumerContext = new ConsumerContext(1);
        ConsumerInterceptor interceptor = consumerContext.getInterceptor();
        Assert.assertNotNull(interceptor);

        interceptor.init(dao);

        // should not fail, with null or different objects
        interceptor.accept(null);
        interceptor.accept("Hello");

        {
            JsonEvent jsonEvent = ConsumerTest.randomJsonEvent(random);
            Pair<String, String> pair = interceptor.deriveCausedEvent(jsonEvent);
            Assert.assertEquals(jsonEvent.getExternalId(), pair.getValue1());
            Assert.assertEquals(jsonEvent.getEventType(), pair.getValue0());

            Assert.assertNull(interceptor.deriveCausedEvent((JsonEvent) null));
        }

        {
            JsonEvent jsonEvent = ConsumerTest.randomJsonEvent(random);
            Pair<String, String> pair = interceptor.deriveCausedEvent(Collections.singletonList(jsonEvent));
            Assert.assertEquals(jsonEvent.getExternalId(), pair.getValue1());
            Assert.assertEquals(jsonEvent.getEventType(), pair.getValue0());
        }

        {
            JsonEvent event1 = ConsumerTest.randomJsonEvent(random);
            JsonEvent event2 = ConsumerTest.randomJsonEvent(random);
            Pair<String, String> pair = interceptor.deriveCausedEvent(Arrays.asList(event1, event2));
            // the last one should be returned
            Assert.assertEquals(event2.getExternalId(), pair.getValue1());
            Assert.assertEquals(event2.getEventType(), pair.getValue0());

            // empty or null should return null
            Assert.assertNull(interceptor.deriveCausedEvent((List<JsonEvent>) null));
            Assert.assertNull(interceptor.deriveCausedEvent(new ArrayList<>()));
        }
    }

    @Test
    public void testBufferEmptyOrNull() throws DbException {
        IOasisDao dao = Mockito.mock(IOasisDao.class);
        ConsumerContext consumerContext = new ConsumerContext(1);
        ConsumerInterceptor interceptor = consumerContext.getInterceptor();
        Assert.assertNotNull(interceptor);

        interceptor.init(dao);

        // should not fail with null
        interceptor.flush(null);

        // empty field should not call to db
        interceptor.flush(new ArrayList<>());
        Mockito.verify(dao, Mockito.never()).executeBatchInsert(Mockito.anyString(), Mockito.anyList());
    }

    @Test
    public void testBufferNonEmpty() throws DbException {
        IOasisDao dao = Mockito.mock(IOasisDao.class);
        ConsumerContext consumerContext = new ConsumerContext(1);
        ConsumerInterceptor interceptor = consumerContext.getInterceptor();
        Assert.assertNotNull(interceptor);

        interceptor.init(dao);

        BufferedRecords.ElementRecord record1 = new BufferedRecords.ElementRecord(new HashMap<>(),
                System.currentTimeMillis());
        BufferedRecords.ElementRecord record2 = new BufferedRecords.ElementRecord(new HashMap<>(),
                System.currentTimeMillis());

        // empty field should not call to db
        interceptor.flush(Arrays.asList(record1, record2));
        Mockito.verify(dao).executeBatchInsert(Mockito.eq("game/batch/addFeed"), Mockito.anyList());
    }

    @Test
    public void testBufferNeverThrowsError() throws DbException {
        IOasisDao dao = Mockito.mock(IOasisDao.class);
        Mockito.when(dao.executeBatchInsert(Mockito.anyString(), Mockito.anyList())).thenThrow(new DbException("error"));

        ConsumerContext consumerContext = new ConsumerContext(1);
        ConsumerInterceptor interceptor = consumerContext.getInterceptor();
        Assert.assertNotNull(interceptor);

        interceptor.init(dao);

        BufferedRecords.ElementRecord record1 = new BufferedRecords.ElementRecord(new HashMap<>(),
                System.currentTimeMillis());
        BufferedRecords.ElementRecord record2 = new BufferedRecords.ElementRecord(new HashMap<>(),
                System.currentTimeMillis());

        // empty field should not call to db
        interceptor.flush(Arrays.asList(record1, record2));
        Mockito.verify(dao).executeBatchInsert(Mockito.eq("game/batch/addFeed"), Mockito.anyList());
    }

}

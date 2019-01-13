package io.github.isuru.oasis.services.services;

import com.github.slugify.Slugify;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.EventSourceToken;
import io.github.isuru.oasis.services.model.IGameController;
import io.github.isuru.oasis.services.services.caches.InMemoryCache;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
public class EventServiceTest {

    @TestConfiguration
    static class EventServiceCtx {

        @Bean
        public IEventsService createService() {
            return new EventsServiceImpl();
        }

        @Bean
        public ICacheProxy createMemCache() {
            return new InMemoryCache();
        }

    }

    @Autowired
    private IEventsService eventsService;

    @MockBean
    private IOasisDao dao;

    @Autowired
    private ICacheProxy cacheProxy;

    @MockBean
    private IGameController gameController;

    @MockBean
    private DataCache dataCache;

    @MockBean
    private IProfileService profileService;

    @Before
    public void beforeEach() throws Exception {
        Mockito.reset(dao);
        Mockito.reset(gameController);

        List<EventSourceToken> tokens = new ArrayList<>();

        Mockito.when(dao.executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class))
                .thenAnswer((Answer<List<EventSourceToken>>) invocation ->
                        tokens.stream().filter(EventSourceToken::isActive).collect(Collectors.toList()));
        Mockito.when(dao.executeInsert(Mockito.eq(Q.EVENTS.ADD_EVENT_SOURCE), Mockito.anyMap(), Mockito.eq("id")))
                .thenAnswer(new Answer<Long>() {
                    long count = 0;
                    @Override
                    public Long answer(InvocationOnMock invocation) {
                        count++;
                        Map<String, Object> data = invocation.getArgument(1);
                        EventSourceToken token = new EventSourceToken();
                        token.setId((int) count);
                        token.setActive(true);
                        token.setSourceName(data.get("sourceName").toString());
                        token.setInternal((Boolean) data.get("isInternal"));
                        token.setDisplayName(data.get("displayName").toString());
                        token.setDownloaded(false);
                        token.setPublicKey((byte[]) data.get("keyPublic"));
                        token.setSecretKey((byte[]) data.get("keySecret"));
                        token.setToken((String) data.get("token"));
                        tokens.add(token);
                        return count;
                    }
                });
        Mockito.when(dao.executeCommand(Mockito.eq(Q.EVENTS.UPDATE_AS_DOWNLOADED), Mockito.anyMap()))
                .thenAnswer((Answer<Long>) invocation -> {
                    Map<String, Object> data = invocation.getArgument(1);
                    int id = (int) data.get("id");
                    Optional<EventSourceToken> first = tokens.stream().filter(t -> t.getId() == id).findFirst();
                    if (first.isPresent() && !first.get().isDownloaded()) {
                        first.get().setDownloaded(true);
                        return first.get().getId().longValue();
                    } else {
                        return 0L;
                    }
                });
        Mockito.when(dao.executeQuery(Mockito.eq(Q.EVENTS.READ_EVENT_SOURCE), Mockito.anyMap(), (Class<?>) Mockito.any()))
                .thenAnswer((Answer<Iterable<EventSourceToken>>) invocation -> {
                    Map<String, Object> data = invocation.getArgument(1);
                    int id = (int) data.get("id");
                    Optional<EventSourceToken> first = tokens.stream().filter(t -> t.getId() == id).findFirst();
                    return Collections.singleton(first.orElse(null));
                });
        Mockito.when(dao.executeCommand(Mockito.eq(Q.EVENTS.DISABLE_EVENT_SOURCE), Mockito.anyMap()))
                .thenAnswer((Answer<Long>) invocation -> {
                    Map<String, Object> data = invocation.getArgument(1);
                    int id = (int) data.get("id");
                    Optional<EventSourceToken> first = tokens.stream().filter(t -> t.getId() == id).findFirst();
                    if (first.isPresent() && !first.get().isInternal()) {
                        first.get().setActive(false);
                        return first.get().getId().longValue();
                    } else {
                        return 0L;
                    }
                });
    }

    @Test
    public void testTokenAdd() throws Exception {
        int invoc = 0;
        List<EventSourceToken> sourceTokens = eventsService.listAllEventSources();
        Assert.assertNotNull(sourceTokens);
        Assert.assertEquals(0, sourceTokens.size());

        invoc++;
        Mockito.verify(dao, Mockito.only()).executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);

        {
            // add internal token
            EventSourceToken tokenInt = new EventSourceToken();
            tokenInt.setDisplayName("Oasis");
            tokenInt.setInternal(true);
            tokenInt.setSourceName("Oasis-Internal");
            tokenInt.setActive(true);

            EventSourceToken addedToken = eventsService.addEventSource(tokenInt);
            Assert.assertNotNull(addedToken);
            Assert.assertEquals("Oasis", addedToken.getDisplayName());
            Assert.assertEquals(EventSourceToken.INTERNAL_NAME, addedToken.getSourceName());
            Assert.assertTrue(addedToken.isInternal());
            Assert.assertTrue(addedToken.isActive());
            Assert.assertFalse(addedToken.isDownloaded());
            Assert.assertTrue(addedToken.getToken().length() > 0);
            Assert.assertTrue(addedToken.getPublicKey().length > 0);
            Assert.assertTrue(addedToken.getSecretKey().length > 0);

            invoc += 2;
            Mockito.verify(dao, Mockito.times(invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }

        {
            // try to add another internal token => should fail
            EventSourceToken tokenInt = new EventSourceToken();
            tokenInt.setDisplayName("Oasis2");
            tokenInt.setInternal(true);
            tokenInt.setSourceName("Oasis2-Internal");
            tokenInt.setActive(true);

            Assertions.assertThrows(InputValidationException.class, () -> eventsService.addEventSource(tokenInt));
            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }

        {
            // add application token => should success
            EventSourceToken appToken = new EventSourceToken();
            appToken.setDisplayName("Stack Overflow");
            appToken.setSourceName("stackoverflow");
            appToken.setInternal(false);
            appToken.setActive(true);
            appToken.setToken("myassignedtoken");
            appToken.setSecretKey("sourcesecret".getBytes(StandardCharsets.UTF_8));
            appToken.setPublicKey("sourcepublic".getBytes(StandardCharsets.UTF_8));

            EventSourceToken addedToken = eventsService.addEventSource(appToken);
            Assert.assertNotNull(addedToken);
            Assert.assertEquals("Stack Overflow", addedToken.getDisplayName());
            Assert.assertEquals("stackoverflow", addedToken.getSourceName());
            Assert.assertFalse(addedToken.isInternal());
            Assert.assertTrue(addedToken.isActive());
            Assert.assertFalse(addedToken.isDownloaded());
            Assert.assertTrue(addedToken.getToken().length() > 0);
            Assert.assertFalse(addedToken.getToken().equalsIgnoreCase(appToken.getToken()));
            Assert.assertNotEquals(addedToken.getSecretKey().length, appToken.getSecretKey().length);
            Assert.assertNotEquals(addedToken.getPublicKey().length, appToken.getPublicKey().length);

            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);

            // read token again using different method
            Optional<EventSourceToken> eventSourceToken = eventsService.readSourceByToken(addedToken.getToken());
            Assert.assertTrue(eventSourceToken.isPresent());
            Assert.assertEquals(eventSourceToken.get().getId(), addedToken.getId());
        }

        {
            // reading non existing token should fail
            Optional<EventSourceToken> abc = eventsService.readSourceByToken("nonexistingtoken");
            Assert.assertFalse(abc.isPresent());
        }

        {
            // when display name is null or empty => must fail
            EventSourceToken token = new EventSourceToken();

            token.setDisplayName(null);
            Assertions.assertThrows(InputValidationException.class, () -> eventsService.addEventSource(token));
            token.setDisplayName(" ");
            Assertions.assertThrows(InputValidationException.class, () -> eventsService.addEventSource(token));
        }

        {
            // now there should have 2 tokens => 1 internal, 1 external
            List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
            Assert.assertEquals(2, eventSourceTokens.size());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(EventSourceToken::isInternal).count());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(t -> !t.isInternal()).count());

            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }

        {
            // try to disable with invalid id => should fail
            Assertions.assertThrows(InputValidationException.class, () -> eventsService.disableEventSource(0));
            Assertions.assertThrows(InputValidationException.class, () -> eventsService.disableEventSource(-10));
        }

        {
            // read internal token => should exist
            Optional<EventSourceToken> eventSourceToken = eventsService.readInternalSourceToken();
            Assert.assertTrue(eventSourceToken.isPresent());

            // internal token cannot be deleted/disabled
            Assert.assertFalse(eventsService.disableEventSource(eventSourceToken.get().getId()));
            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }

        {
            // add another application token => should success
            EventSourceToken appToken = new EventSourceToken();
            appToken.setDisplayName("Github");
            appToken.setSourceName("github");
            appToken.setInternal(false);
            appToken.setActive(true);

            EventSourceToken addedToken = eventsService.addEventSource(appToken);
            Assert.assertNotNull(addedToken);
            Assert.assertEquals("Github", addedToken.getDisplayName());
            Assert.assertEquals("github", addedToken.getSourceName());
            Assert.assertFalse(addedToken.isInternal());
            Assert.assertTrue(addedToken.isActive());
            Assert.assertFalse(addedToken.isDownloaded());
            Assert.assertTrue(addedToken.getToken().length() > 0);

            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);

            // now there should have 3 tokens => 1 internal, 2 external
            List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
            Assert.assertEquals(3, eventSourceTokens.size());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(EventSourceToken::isInternal).count());
            Assert.assertEquals(2, eventSourceTokens.stream().filter(t -> !t.isInternal()).count());
            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);


            // should be able to download key first time
            Optional<EventSourceToken> eventSourceToken = eventsService.makeDownloadSourceKey(addedToken.getId());
            Assert.assertTrue(eventSourceToken.isPresent());
            Assert.assertTrue(eventSourceToken.get().getPublicKey().length > 0);
            Assert.assertTrue(eventSourceToken.get().getSecretKey().length > 0);

            // download again the same key must be failed
            eventSourceToken = eventsService.makeDownloadSourceKey(addedToken.getId());
            Assert.assertFalse(eventSourceToken.isPresent());

            // disable the just added token
            Assert.assertTrue(eventsService.disableEventSource(addedToken.getId()));
            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }

        {
            // now there should have 2 tokens => 1 internal, 1 external
            List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
            Assert.assertEquals(2, eventSourceTokens.size());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(EventSourceToken::isInternal).count());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(t -> !t.isInternal()).count());
            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }

        {
            // add another application token without source name => should success
            EventSourceToken appToken = new EventSourceToken();
            appToken.setDisplayName("Bitbucket Code Repository");
            appToken.setInternal(false);
            appToken.setActive(true);

            EventSourceToken addedToken = eventsService.addEventSource(appToken);
            Slugify slugify = new Slugify();
            Assert.assertNotNull(addedToken);
            Assert.assertEquals("Bitbucket Code Repository", addedToken.getDisplayName());
            String srcName = slugify.slugify("Bitbucket Code Repository");
            System.out.println(srcName);
            Assert.assertEquals(srcName, addedToken.getSourceName());

            Mockito.verify(dao, Mockito.times(++invoc))
                    .executeQuery(Q.EVENTS.LIST_ALL_EVENT_SOURCES, null, EventSourceToken.class);
        }
    }

}

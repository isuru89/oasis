package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.db.DbProperties;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.db.OasisDbFactory;
import io.github.isuru.oasis.db.OasisDbPool;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.Maps;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
class EventTest extends AbstractApiTest {

    private static IOasisDao oasisDao;
    private static IOasisApiService apiService;

    @Test
    void testFailEventSubmission() throws Exception {
        IEventsService eventService = apiService.getEventService();
        EventSourceToken inttoken = new EventSourceToken();
        inttoken.setDisplayName("Oasis-Test-Internal-Token");
        inttoken.setInternal(true);

        EventSourceToken addedIntToken = eventService.addEventSource(inttoken);
        String itoken = addedIntToken.getToken();

        {
            Map<String, Object> event = Maps.create()
                    .put("type", "test")
                    .put("ts", System.currentTimeMillis())
                    .put("user", 1)
                    .build();
            assertFail(() -> eventService.submitEvent(null, event), InputValidationException.class);
            assertFail(() -> eventService.submitEvent("", event), InputValidationException.class);
            assertFail(() -> eventService.submitEvent("abcdefgh", event), ApiAuthException.class);
        }
        {
            Map<String, Object> empty = Maps.create().build();
            assertFail(() -> eventService.submitEvent(itoken, empty), InputValidationException.class);
        }
        {
            assertFail(() -> eventService.submitEvent(itoken,
                    Maps.create()
                        .put("ts", System.currentTimeMillis())
                        .put("user", 1).build()), InputValidationException.class);
            assertFail(() -> eventService.submitEvent(itoken,
                    Maps.create()
                            .put("type", "ddd")
                            .put("user", 1).build()), InputValidationException.class);
            assertFail(() -> eventService.submitEvent(itoken,
                    Maps.create()
                            .put("ts", System.currentTimeMillis())
                            .put("type", "ddd").build()), InputValidationException.class);
        }
        {
            // non existence user
            Map<String, Object> event = Maps.create()
                    .put("type", "test")
                    .put("ts", System.currentTimeMillis())
                    .put("user", "isuru@nonexist.com")
                    .build();
            assertFail(() -> eventService.submitEvent(itoken, event), InputValidationException.class);
        }


        {
            // sending no event
            eventService.submitEvents(itoken, null);
            eventService.submitEvents(itoken, new ArrayList<>());
            Map<String, Object> event = Maps.create()
                    .put("type", "test")
                    .put("ts", System.currentTimeMillis())
                    .put("user", "isuru@nonexist.com")
                    .build();
            assertFail(() -> eventService.submitEvents(itoken, Collections.singletonList(event)),
                    InputValidationException.class);
        }
    }

    @Test
    void testEventSource() throws Exception {
        IEventsService eventService = apiService.getEventService();
        Assertions.assertTrue(eventService.listAllEventSources().isEmpty());

        EventSourceToken itoken = new EventSourceToken();
        itoken.setDisplayName("Oasis-Test-Internal-Token");
        itoken.setInternal(true);

        EventSourceToken addedIntToken = eventService.addEventSource(itoken);
        Assertions.assertNotNull(addedIntToken);
        Assertions.assertTrue(addedIntToken.getId() > 0);
        Assertions.assertEquals(addedIntToken.getDisplayName(), itoken.getDisplayName());
        Assertions.assertTrue(addedIntToken.isInternal());
        Assertions.assertTrue(addedIntToken.isActive());

        {
            // try to add a new internal token again should fail
            assertFail(() -> {
                EventSourceToken iitoken = new EventSourceToken();
                iitoken.setDisplayName("Other-Internal-Token");
                iitoken.setInternal(true);
                eventService.addEventSource(iitoken);
            }, InputValidationException.class);
        }

        EventSourceToken utoken = new EventSourceToken();
        utoken.setDisplayName("Jira Token");
        EventSourceToken jiraSource = eventService.addEventSource(utoken);
        Assertions.assertTrue(jiraSource.isActive());
        Assertions.assertEquals(utoken.getDisplayName(), jiraSource.getDisplayName());
        Assertions.assertNotNull(jiraSource.getToken());
        Assertions.assertTrue(jiraSource.getId() > 0);
        Assertions.assertFalse(jiraSource.isInternal());

        List<EventSourceToken> allTokens = eventService.listAllEventSources();
        Assertions.assertEquals(2, allTokens.size());

        Assertions.assertTrue(eventService.disableEventSource(jiraSource.getId()));
        allTokens = eventService.listAllEventSources();
        Assertions.assertEquals(1, allTokens.size());
    }

    @BeforeEach
    void beforeTest() throws Exception {
        // clear table
        clearTables("OA_EVENT_SOURCE");
    }

    @BeforeAll
    static void beforeAnyTest() throws Exception {
        DbProperties properties = new DbProperties(OasisDbPool.DEFAULT);
        properties.setUrl("jdbc:mysql://localhost/oasis");
        properties.setUsername("isuru");
        properties.setPassword("isuru");
        File file = new File("./scripts/db");
        if (!file.exists()) {
            file = new File("../scripts/db");
            if (!file.exists()) {
                Assertions.fail("Database scripts directory is not found!");
            }
        }
        properties.setQueryLocation(file.getAbsolutePath());

        oasisDao = OasisDbFactory.create(properties);
        apiService = new DefaultOasisApiService(oasisDao, null);
    }

    @AfterAll
    static void afterAnyTest() throws Exception {
        System.out.println("Shutting down db connection.");
        try {
            oasisDao.executeRawCommand("TRUNCATE OA_EVENT_SOURCE", new HashMap<>());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        oasisDao.close();
        apiService = null;
    }

    static void clearTables(String... tableNames) throws Exception {
        for (String tbl : tableNames) {
            oasisDao.executeRawCommand("TRUNCATE " + tbl, null);
        }
    }


}

package io.github.isuru.oasis.services.services;

import com.github.slugify.Slugify;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.EventSourceToken;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EventServiceTest extends AbstractServiceTest {

    @Autowired
    private IEventsService eventsService;

    @Before
    public void beforeEach() throws Exception {
        truncateTables("OA_EVENT_SOURCE");

        List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
        assertThat(eventSourceTokens).isNotNull().hasSize(0);
    }

    @Test
    @DisplayName("Testing Event Source Token CRUDs for external applications")
    public void testAddToken() throws Exception {
        {
            // add internal token
            EventSourceToken tokenInt = getInternalToken("Oasis", "Oasis-Internal");
            EventSourceToken addedToken = eventsService.addEventSource(tokenInt);

            addedToken = assertToken(addedToken, tokenInt.getDisplayName(), EventSourceToken.INTERNAL_NAME);
            Assert.assertTrue(addedToken.isActive());
        }

        {
            // try to add another internal token => should fail
            EventSourceToken tokenInt = getInternalToken("Oasis2", "Oasis2-Internal");

            assertThatThrownBy(() -> eventsService.addEventSource(tokenInt))
                    .isInstanceOf(InputValidationException.class);
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
            addedToken = assertToken(addedToken, appToken.getDisplayName(), appToken.getSourceName());
            Assert.assertFalse(addedToken.getToken().equalsIgnoreCase(appToken.getToken()));
            Assert.assertNotEquals(addedToken.getSecretKey().length, appToken.getSecretKey().length);
            Assert.assertNotEquals(addedToken.getPublicKey().length, appToken.getPublicKey().length);

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
            assertThatThrownBy(() -> eventsService.addEventSource(token))
                    .isInstanceOf(InputValidationException.class);

            token.setDisplayName(" ");
            assertThatThrownBy(() -> eventsService.addEventSource(token))
                    .isInstanceOf(InputValidationException.class);
        }

        {
            // now there should have 2 tokens => 1 internal, 1 external
            List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
            Assert.assertEquals(2, eventSourceTokens.size());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(EventSourceToken::isInternal).count());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(t -> !t.isInternal()).count());
        }

        {
            // try to disable with invalid id => should fail
            assertThatThrownBy(() -> eventsService.disableEventSource(0))
                    .isInstanceOf(InputValidationException.class);
            assertThatThrownBy(() -> eventsService.disableEventSource(-10))
                    .isInstanceOf(InputValidationException.class);
        }

        {
            // read internal token => should exist
            Optional<EventSourceToken> eventSourceToken = eventsService.readInternalSourceToken();
            Assert.assertTrue(eventSourceToken.isPresent());

            // internal token cannot be deleted/disabled
            Assert.assertFalse(eventsService.disableEventSource(eventSourceToken.get().getId()));
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

            // now there should have 3 tokens => 1 internal, 2 external
            List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
            Assert.assertEquals(3, eventSourceTokens.size());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(EventSourceToken::isInternal).count());
            Assert.assertEquals(2, eventSourceTokens.stream().filter(t -> !t.isInternal()).count());


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
        }

        {
            // now there should have 2 tokens => 1 internal, 1 external
            List<EventSourceToken> eventSourceTokens = eventsService.listAllEventSources();
            Assert.assertEquals(2, eventSourceTokens.size());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(EventSourceToken::isInternal).count());
            Assert.assertEquals(1, eventSourceTokens.stream().filter(t -> !t.isInternal()).count());
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
            Assert.assertEquals(srcName, addedToken.getSourceName());
        }
    }

    private EventSourceToken assertToken(EventSourceToken token,
                                         String name,
                                         String srcName) {
        Assert.assertNotNull(token);
        Assert.assertEquals(name, token.getDisplayName());
        Assert.assertEquals(srcName, token.getSourceName());
        Assert.assertTrue(token.isActive());
        Assert.assertFalse(token.isDownloaded());
        Assert.assertTrue(token.getToken().length() > 0);
        Assert.assertTrue(token.getPublicKey().length > 0);
        Assert.assertTrue(token.getSecretKey().length > 0);
        return token;
    }

    private EventSourceToken getInternalToken(String name, String srcName) {
        EventSourceToken tokenInt = new EventSourceToken();
        tokenInt.setDisplayName(name);
        tokenInt.setInternal(true);
        tokenInt.setSourceName(srcName);
        tokenInt.setActive(true);
        return tokenInt;
    }
}

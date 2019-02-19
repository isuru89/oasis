package io.github.isuru.oasis.services.services.control;

import org.junit.Assert;
import org.junit.Test;

public class LocalEndEventTest {

    @Test
    public void testLocalEndEvent() {
        LocalEndEvent event = new LocalEndEvent();
        Assert.assertNull(event.getGameId());
        Assert.assertNull(event.getEventType());
        Assert.assertNull(event.getExternalId());
        Assert.assertNull(event.getSource());
        Assert.assertNull(event.getTeam());
        Assert.assertNull(event.getTeamScope());
        Assert.assertNull(event.getUserId("any"));
        Assert.assertNull(event.getFieldValue("any"));
        Assert.assertEquals(0L, event.getTimestamp());
        Assert.assertEquals(0L, event.getUser());
        Assert.assertEquals(0L, event.getUser());

        Assert.assertNull(event.getAllFieldValues());
        event.setFieldValue("key", "val");
        Assert.assertNull(event.getAllFieldValues());
    }

}

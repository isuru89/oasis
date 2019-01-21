package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.game.parser.BadgeParser;
import io.github.isuru.oasis.model.Badge;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.BadgeNotification;
import io.github.isuru.oasis.model.handlers.output.BadgeModel;
import io.github.isuru.oasis.model.rules.BadgeRule;
import org.apache.flink.api.common.functions.MapFunction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class MappersTest {

    private Random random;

    @BeforeEach
    public void before() {
        random = new Random(System.currentTimeMillis());
    }

    @Test
    public void testBadgeNotifier() throws Exception {
        MapFunction<BadgeNotification, String> mapper = new BadgeNotificationMapper();
        JsonEvent event = randomJsonEvent();
        BadgeRule badgeRule = readRule("/badge-test1/rules/badges.yml", 1);
        Badge badge = badgeRule.getBadge();

        BadgeNotification notification = new BadgeNotification(2,
                Collections.singletonList(event),
                badgeRule,
                badge,
                "here is the tag");

        String content = mapper.map(notification);
        BadgeModel model = toObj(content, BadgeModel.class);

        assertBadgeOutput(model, event, badgeRule, notification);
    }

    private void assertBadgeOutput(BadgeModel model, Event event, BadgeRule rule,
                                   BadgeNotification notification) {
        Assertions.assertEquals(rule.getBadge().getId(), model.getBadgeId());
        Assertions.assertEquals(event.getTeam(), model.getTeamId());
        Assertions.assertEquals(event.getTeamScope(), model.getTeamScopeId());
        Assertions.assertEquals(event.getTimestamp(), model.getTs().longValue());
        Assertions.assertEquals(event.getEventType(), model.getEventType());
        Assertions.assertEquals(rule.getBadge().getName(), model.getSubBadgeId());
        Assertions.assertEquals(notification.getTag(), model.getTag());
        Assertions.assertEquals(notification.getUserId(), model.getUserId().longValue());
    }

    private JsonEvent randomJsonEvent() {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis());
        jsonEvent.put(Constants.FIELD_USER, random.nextInt(1000) + 1);
        jsonEvent.put(Constants.FIELD_GAME_ID, random.nextInt(5) + 1);
        jsonEvent.put(Constants.FIELD_SOURCE, random.nextInt(100) + 500);
        jsonEvent.put(Constants.FIELD_ID, UUID.randomUUID().toString());
        jsonEvent.put(Constants.FIELD_EVENT_TYPE, "oasis.test.");

        return jsonEvent;
    }

    private <T> T toObj(String content, Class<T> clz) throws IOException {
        return BaseNotificationMapper.OBJECT_MAPPER.readValue(content, clz);
    }

    private BadgeRule readRule(String resPath, int index) throws IOException {
        try (InputStream stream = MappersTest.class.getResourceAsStream(resPath)) {
            List<BadgeRule> parsed = BadgeParser.parse(stream);
            return parsed.get(index);
        }
    }
}

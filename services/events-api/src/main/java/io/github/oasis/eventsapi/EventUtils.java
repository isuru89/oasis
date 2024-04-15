package io.github.oasis.eventsapi;

import io.github.oasis.core.Event;
import io.github.oasis.core.EventJson;
import org.springframework.stereotype.Component;

@Component
public final class EventUtils {

    public EventJson duplicateTo(EventJson source, long gameId, int teamId, int eventSourceId, long playerId) {
        var cloned = source.getAllFieldValues();
        cloned.put(Event.SOURCE_ID, eventSourceId);
        cloned.put(Event.TEAM_ID, teamId);
        cloned.put(Event.USER_ID, playerId);
        cloned.put(Event.GAME_ID, gameId);

        return new EventJson(cloned);
    }

}

package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.LRUCache;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.RabbitDispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class EventsService extends BaseService implements IEventsService {

    private final Map<String, Long> USER_CACHE;

    EventsService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);

        USER_CACHE = new LRUCache<>(Configs.get().getInt("oasis.cache.user.size", 300));
    }

    @Override
    public void submitEvent(Map<String, Object> eventData) throws Exception {
        Checks.validate(eventData.containsKey(Constants.FIELD_EVENT_TYPE), "No event-type ('type') field in the event!");
        Checks.validate(eventData.containsKey(Constants.FIELD_TIMESTAMP), "No timestamp ('ts') field in the event!");
        Checks.validate(eventData.containsKey(Constants.FIELD_USER), "No user ('user') field in the event!");

        Object user = eventData.get(Constants.FIELD_USER);
        long userId;
        if (user instanceof String) {
            String email = user.toString();
            userId = resolveUser(email);
        } else {
            userId = Long.parseLong(String.valueOf(user));
        }

        Map<String, Object> event = new HashMap<>(eventData);
        if (!event.containsKey(Constants.FIELD_TEAM)) {
            UserTeam userTeam = getApiService().getProfileService().findCurrentTeamOfUser(userId);
            event.put(Constants.FIELD_TEAM, userTeam.getTeamId());
        }
        RabbitDispatcher.get().dispatch(event);
    }

    @Override
    public void submitEvents(List<Map<String, Object>> events) throws Exception {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (Map<String, Object> event : events) {
            submitEvent(event);
        }
    }

    @Override
    public List<EventSourceToken> listAllEventSources() throws Exception {
        return toList(getDao().executeQuery("def/events/listAllEventSources",
                new HashMap<>(),
                EventSourceToken.class));
    }

    @Override
    public EventSourceToken addEventSource(EventSourceToken sourceToken) throws Exception {
        String token = AuthUtils.get().issueSourceToken(sourceToken);
        long id = getDao().executeInsert("def/events/addEventSource",
                Maps.create().put("token", sourceToken.getToken())
                    .put("displayName", sourceToken.getDisplayName())
                    .put("authToken", token)
                    .build(), "id");

        List<EventSourceToken> eventSourceTokens = listAllEventSources();
        for (EventSourceToken eventSourceToken : eventSourceTokens) {
            if (eventSourceToken.getId() == id) {
                return eventSourceToken;
            }
        }
        throw new Exception("Unable to register event source!");
    }

    @Override
    public boolean disableEventSource(int id) throws Exception {
        return getDao().executeCommand("def/events/disableEventSource",
                Maps.create("id", id)) > 0;
    }

    private long resolveUser(String email) throws Exception {
        if (USER_CACHE.containsKey(email)) {
            return USER_CACHE.get(email);
        } else {
            UserProfile profile = getApiService().getProfileService().readUserProfile(email);
            if (profile == null) {
                throw new InputValidationException("There is no user by having email '" + email + "'!");
            }
            USER_CACHE.put(email, profile.getId());
            return profile.getId();
        }
    }
}

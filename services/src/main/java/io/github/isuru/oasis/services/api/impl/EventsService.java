package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.LRUCache;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.RabbitDispatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public class EventsService extends BaseService implements IEventsService {

    private static final Object SOURCE_LOCK = new Object();

    private final Map<String, Long> USER_CACHE;
    private final Map<String, EventSourceToken> SOURCE_CACHE = new ConcurrentHashMap<>();

    EventsService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);

        USER_CACHE = new LRUCache<>(Configs.get().getInt("oasis.cache.user.size", 300));
        try {
            listAllEventSources();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot load event sources from database!", e);
        }
    }

    @Override
    public void submitEvent(String token, Map<String, Object> eventData) throws Exception {
        Checks.nonNullOrEmpty(token, "token");
        Checks.validate(eventData.containsKey(Constants.FIELD_EVENT_TYPE), "No event-type ('type') field in the event!");
        Checks.validate(eventData.containsKey(Constants.FIELD_TIMESTAMP), "No timestamp ('ts') field in the event!");
        Checks.validate(eventData.containsKey(Constants.FIELD_USER), "No user ('user') field in the event!");
        if (DataCache.get().getGameCount() > 1 && !eventData.containsKey(Constants.FIELD_GAME_ID)) {
            throw new InputValidationException("Unable to find associated game id for this event!");
        }

        // authenticate event...
        EventSourceToken eventSourceToken = SOURCE_CACHE.get(token);
        if (eventSourceToken == null || !eventSourceToken.isActive()) {
            throw new ApiAuthException("Unable to verify authentication of event source for token '" + token + "'!");
        }
        eventData.put(Constants.FIELD_SOURCE, eventSourceToken.getId());

        Object user = eventData.get(Constants.FIELD_USER);
        long userId;
        if (user instanceof String) {
            String email = user.toString();
            userId = resolveUser(email);
        } else {
            userId = Long.parseLong(String.valueOf(user));
        }

        Map<String, Object> event = new HashMap<>(eventData);
        // @TODO should we scope events for game?
        event.put(Constants.FIELD_GAME_ID, DataCache.get().getDefGameId());

        if (!event.containsKey(Constants.FIELD_TEAM)) {
            UserTeam userTeam = getApiService().getProfileService().findCurrentTeamOfUser(userId);
            if (userTeam != null) {
                event.put(Constants.FIELD_TEAM, userTeam.getTeamId());
                event.put(Constants.FIELD_SCOPE, userTeam.getScopeId());
            } else {
                event.put(Constants.FIELD_TEAM, DataCache.get().getTeamDefault().getId());
                event.put(Constants.FIELD_SCOPE, DataCache.get().getTeamScopeDefault().getId());
            }
        } else {
            // @TODO validate team and user
            event.put(Constants.FIELD_TEAM, DataCache.get().getTeamDefault().getId());
            event.put(Constants.FIELD_SCOPE, DataCache.get().getTeamScopeDefault().getId());
        }

        RabbitDispatcher.get().dispatch(event);
    }

    @Override
    public void submitEvents(String token, List<Map<String, Object>> events) throws Exception {
        if (events == null || events.isEmpty()) {
            return;
        }

        for (Map<String, Object> event : events) {
            submitEvent(token, event);
        }
    }

    @Override
    public List<EventSourceToken> listAllEventSources() throws Exception {
        List<EventSourceToken> eventSourceTokens = toList(getDao().executeQuery("def/events/listAllEventSources",
                null,
                EventSourceToken.class));
        refreshSourceTokens(eventSourceTokens);
        return eventSourceTokens;
    }

    @Override
    public EventSourceToken addEventSource(EventSourceToken sourceToken) throws Exception {
        if (sourceToken.isInternal()) {
            // check for existing internal sources
            Optional<EventSourceToken> eventSourceToken = readInternalSourceToken();
            if (eventSourceToken.isPresent()) {
                throw new InputValidationException("Source token cannot be marked as internal!");
            }
        }

        String token = AuthUtils.get().issueSourceToken(sourceToken);
        long id = getDao().executeInsert("def/events/addEventSource",
                Maps.create().put("token", token)
                    .put("displayName", sourceToken.getDisplayName())
                    .put("isInternal", sourceToken.isInternal())
                    .build(),
                "id");

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
        boolean success = getDao().executeCommand("def/events/disableEventSource",
                Maps.create("id", id)) > 0;

        if (success) {
            synchronized (SOURCE_LOCK) {
                String token = null;
                for (Map.Entry<String, EventSourceToken> entry : SOURCE_CACHE.entrySet()) {
                    if (entry.getValue().getId() == id) {
                        token = entry.getKey();
                        break;
                    }
                }

                if (token != null) {
                    SOURCE_CACHE.remove(token);
                }
            }
        }
        return success;
    }

    @Override
    public Optional<EventSourceToken> readInternalSourceToken() throws Exception {
        return listAllEventSources().stream()
                .filter(EventSourceToken::isActive)
                .filter(EventSourceToken::isInternal)
                .findFirst();
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

    private void refreshSourceTokens(List<EventSourceToken> tokens) {
        synchronized (SOURCE_LOCK) {
            SOURCE_CACHE.clear();

            if (tokens != null && !tokens.isEmpty()) {
                for (EventSourceToken token : tokens) {
                    SOURCE_CACHE.put(token.getToken(), token);
                }
            }
        }
    }
}

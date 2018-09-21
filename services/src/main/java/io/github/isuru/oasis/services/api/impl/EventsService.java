package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.*;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.sql.Blob;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author iweerarathna
 */
public class EventsService extends BaseService implements IEventsService {

    private final ICacheProxy cacheProxy;
    private final EventSources sources = new EventSources();

    private IGameController gameController;

    EventsService(IOasisApiService apiService,
                  OasisOptions oasisOptions) {
        super(apiService);

        cacheProxy = oasisOptions.getCacheProxy();
        gameController = oasisOptions.getGameController();
        //USER_CACHE = new LRUCache<>(oasisOptions.getConfigs().getInt("oasis.cache.user.size", 300));
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
        Optional<EventSourceToken> eventSourceToken = sources.getSourceByToken(token);
        if (!eventSourceToken.isPresent() || !eventSourceToken.get().isActive()) {
            throw new ApiAuthException("Unable to verify authorization token of event source '" + token + "'!");
        }
        eventData.put(Constants.FIELD_SOURCE, eventSourceToken.get().getId());

        Object user = eventData.get(Constants.FIELD_USER);
        long userId;
        if (user instanceof String) {
            String email = user.toString();
            userId = resolveUser(email);
        } else {
            userId = Long.parseLong(String.valueOf(user));
        }

        Object gobj = eventData.get(Constants.FIELD_GAME_ID);
        long gid = gobj != null ? Long.parseLong(gobj.toString()) : DataCache.get().getDefGameId();

        Map<String, Object> event = new HashMap<>(eventData);
        event.remove(Constants.FIELD_GAME_ID);

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
            Object team = event.get(Constants.FIELD_TEAM);
            if (team instanceof String) {
                TeamProfile teamProfile = resolveTeam(String.valueOf(team));
                event.put(Constants.FIELD_TEAM, teamProfile.getId());
                event.put(Constants.FIELD_SCOPE, teamProfile.getTeamScope());
            } else {
                event.put(Constants.FIELD_TEAM, Long.parseLong(String.valueOf(team)));
                event.put(Constants.FIELD_SCOPE, DataCache.get().getTeamScopeDefault().getId());
            }
        }

        gameController.submitEvent(gid, event);
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
        Checks.nonNullOrEmpty(sourceToken.getDisplayName(), "displayName");

        if (sourceToken.isInternal()) {
            // check for existing internal sources
            Optional<EventSourceToken> eventSourceToken = readInternalSourceToken();
            if (eventSourceToken.isPresent()) {
                throw new InputValidationException("Source token cannot be marked as internal!");
            }
        }

        Pair<String, Integer> tokenNoncePair = AuthUtils.get().issueSourceToken(sourceToken);
        Pair<PrivateKey, PublicKey> key = AuthUtils.generateRSAKey(sourceToken);
        long id = getDao().executeInsert("def/events/addEventSource",
                Maps.create().put("token", tokenNoncePair.getValue0())
                    .put("nonce", tokenNoncePair.getValue1())
                    .put("sourceName", EventSourceToken.INTERNAL_NAME)
                    .put("keySecret", key.getValue0().getEncoded())
                    .put("keyPublic", key.getValue1().getEncoded())
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
        Checks.greaterThanZero(id, "id");

        boolean success = getDao().executeCommand("def/events/disableEventSource",
                Maps.create("id", id)) > 0;

        if (success) {
            listAllEventSources();
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

    @Override
    public Optional<EventSourceToken> makeDownloadSourceKey(int id) throws Exception {
        boolean canDownload = getDao().executeCommand("def/events/updateAsDownloaded",
                Maps.create("id", id)) > 0;
        if (canDownload) {
            listAllEventSources();
        }
        return Optional.empty();
    }

    @Override
    public Optional<EventSourceToken> readSourceByToken(String token) {
        return sources.getSourceByToken(token);
    }

    private long resolveUser(String email) throws Exception {
        Optional<String> uidOpt = cacheProxy.get("user.email." + email);
        if (uidOpt.isPresent()) {
            return Long.parseLong(uidOpt.get());
        } else {
            UserProfile profile = getApiService().getProfileService().readUserProfile(email);
            if (profile == null) {
                throw new InputValidationException("There is no user by having email '" + email + "'!");
            }
            cacheProxy.update("user.email." + email, String.valueOf(profile.getId()));
            return profile.getId();
        }
    }

    private TeamProfile resolveTeam(String team) throws Exception {
        TeamProfile teamByName = getApiService().getProfileService().findTeamByName(team);
        if (teamByName != null) {
            return teamByName;
        } else {
            throw new InputValidationException("No team is found by name of '" + team + "'!");
        }
    }

    private void refreshSourceTokens(List<EventSourceToken> tokens) {
        sources.setSources(tokens);
    }
}

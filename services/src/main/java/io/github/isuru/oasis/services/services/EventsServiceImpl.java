package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author iweerarathna
 */
@Service("eventService")
public class EventsServiceImpl implements IEventsService {

    private static final Logger LOG = LoggerFactory.getLogger(EventsServiceImpl.class);

    @Autowired
    private ICacheProxy cacheProxy;

    @Autowired
    private IGameController gameController;

    @Autowired
    private IOasisDao dao;

    @Autowired
    private IProfileService profileService;


    private final EventSources sources = new EventSources();

    @PostConstruct
    public void init() {
        try {
            LOG.debug("Fetching all event sources from database...");
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
            throw new ApiAuthException("Unauthorized event source identified by token '" + token + "'!");
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
            UserTeam userTeam = profileService.findCurrentTeamOfUser(userId);
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
        List<EventSourceToken> eventSourceTokens = ServiceUtils.toList(dao.executeQuery(
                Q.EVENTS.LIST_ALL_EVENT_SOURCES,
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
                throw new InputValidationException("Only one internal token can exist in the game!");
            }
        }

        Pair<String, Integer> tokenNoncePair = AuthUtils.get().issueSourceToken(sourceToken);
        Pair<PrivateKey, PublicKey> key = AuthUtils.generateRSAKey(sourceToken);
        long id = dao.executeInsert(Q.EVENTS.ADD_EVENT_SOURCE,
                Maps.create()
                    .put("token", tokenNoncePair.getValue0())
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

        boolean success = dao.executeCommand(Q.EVENTS.DISABLE_EVENT_SOURCE,
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
        boolean canDownload = dao.executeCommand(Q.EVENTS.UPDATE_AS_DOWNLOADED,
                Maps.create("id", id)) > 0;
        if (canDownload) {
            return Optional.ofNullable(ServiceUtils.getTheOnlyRecord(dao, Q.EVENTS.READ_EVENT_SOURCE,
                    Maps.create("id", id), EventSourceToken.class));
        }
        return Optional.empty();
    }

    @Override
    public Optional<EventSourceToken> readSourceByToken(String token) {
        return sources.getSourceByToken(token);
    }

    private long resolveUser(String email) throws Exception {
        String key = "user.email." + email;
        Optional<String> uidOpt = cacheProxy.get(key);
        if (uidOpt.isPresent()) {
            return Long.parseLong(uidOpt.get());
        } else {
            UserProfile profile = profileService.readUserProfile(email);
            if (profile == null) {
                throw new InputValidationException("There is no user by having email '" + email + "'!");
            }
            cacheProxy.update(key, String.valueOf(profile.getId()));
            return profile.getId();
        }
    }

    private TeamProfile resolveTeam(String team) throws Exception {
        TeamProfile teamByName = profileService.findTeamByName(team);
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

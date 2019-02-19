package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.services.DataCache;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.EventSourceToken;
import io.github.isuru.oasis.services.model.IGameController;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.services.caches.CacheProxyManager;
import io.github.isuru.oasis.services.services.control.GameControllerManager;
import io.github.isuru.oasis.services.utils.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EventSubmitTest {

    private CacheProxyManager cacheProxyManager;
    private ICacheProxy cacheProxy;
    private GameControllerManager gameControllerManager;
    private IGameController gameController;
    private IOasisDao dao;
    private IProfileService profileService;
    private DataCache dataCache;

    private IEventsService eventsService;

    private String tokenInternal = "";
    private String token1 = "";
    private String token2 = "";

    @Before
    public void before() throws DbException {
        cacheProxyManager = Mockito.mock(CacheProxyManager.class);
        gameControllerManager = Mockito.mock(GameControllerManager.class);
        dao = Mockito.mock(IOasisDao.class);
        profileService = Mockito.mock(IProfileService.class);
        dataCache = Mockito.mock(DataCache.class);

        gameController = Mockito.mock(IGameController.class);
        cacheProxy = Mockito.mock(ICacheProxy.class);

        Mockito.when(gameControllerManager.get()).thenReturn(gameController);
        Mockito.when(cacheProxyManager.get()).thenReturn(cacheProxy);

        eventsService = new EventsServiceImpl(cacheProxyManager,
                gameControllerManager,
                dao,
                profileService,
                dataCache);


        EventSourceToken tokenInt = new EventSourceToken();
        tokenInt.setInternal(true);
        tokenInt.setActive(true);
        tokenInt.setDisplayName("token-internal");
        tokenInternal = UUID.randomUUID().toString();
        tokenInt.setToken(tokenInternal);
        tokenInt.setId(1);

        EventSourceToken token = new EventSourceToken();
        token.setInternal(false);
        token.setActive(true);
        token.setDisplayName("token-active");
        token1 = UUID.randomUUID().toString();
        token.setToken(token1);
        token.setId(2);

        EventSourceToken tokenInact = new EventSourceToken();
        tokenInact.setInternal(false);
        tokenInact.setActive(false);
        tokenInact.setDisplayName("token-inactive");
        token2 = UUID.randomUUID().toString();
        tokenInact.setToken(token2);
        tokenInact.setId(3);

        List<EventSourceToken> eventSourceTokens = Arrays.asList(tokenInt, token, tokenInact);
        Mockito.when(dao.executeQuery(Mockito.eq(Q.EVENTS.LIST_ALL_EVENT_SOURCES),
                        Mockito.isNull(), (Class<EventSourceToken>) Mockito.any()))
                .thenReturn(eventSourceTokens);

        eventsService.init();
    }

    @Test
    public void testSubmitEventFailure() {
        // token non empty
        Assertions.assertThrows(InputValidationException.class, () -> eventsService.submitEvent(null, null));
        Assertions.assertThrows(InputValidationException.class, () -> eventsService.submitEvent("", null));
        Assertions.assertThrows(InputValidationException.class, () -> eventsService.submitEvent("  ", null));

        Assertions.assertThrows(InputValidationException.class, () -> eventsService.submitEvent(token1, null));

        // mandatory keys (eventType, ts, user)
        Assertions.assertThrows(InputValidationException.class,
                () -> eventsService.submitEvent(token1, Maps.create("a", 1)));
        Assertions.assertThrows(InputValidationException.class,
                () -> eventsService.submitEvent(token1,
                        Maps.create(Constants.FIELD_EVENT_TYPE, "so.event.type")));
        Assertions.assertThrows(InputValidationException.class,
                () -> eventsService.submitEvent(token1,
                        Maps.create(Constants.FIELD_EVENT_TYPE, "so.event.type",
                                Constants.FIELD_TIMESTAMP, System.currentTimeMillis())));

        // when there are multiple games running in, but game id does not exists...
        Mockito.when(dataCache.getGameCount()).thenReturn(2);
        Assertions.assertThrows(InputValidationException.class,
                () -> eventsService.submitEvent(token1,
                        Maps.create()
                                .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                                .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                                .put(Constants.FIELD_USER, 1)
                                .build()));

        // non existing token
        Assertions.assertThrows(ApiAuthException.class,
                () -> eventsService.submitEvent("nonexisting-token",
                        Maps.create()
                                .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                                .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                                .put(Constants.FIELD_USER, 1)
                                .put(Constants.FIELD_GAME_ID, 1)
                                .build()));

        // inactive token
        Assertions.assertThrows(ApiAuthException.class,
                () -> eventsService.submitEvent(token2,
                        Maps.create()
                                .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                                .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                                .put(Constants.FIELD_USER, 1)
                                .put(Constants.FIELD_GAME_ID, 1)
                                .build()));
    }

    @Test
    public void testEventSubmitWithUserIdOnly() throws Exception {
        Mockito.when(dataCache.getGameCount()).thenReturn(1);
        {
            TeamProfile teamProfile = new TeamProfile();
            teamProfile.setId(1);
            Mockito.when(dataCache.getTeamDefault()).thenReturn(teamProfile);

            TeamScope teamScope = new TeamScope();
            teamScope.setId(1);
            Mockito.when(dataCache.getTeamScopeDefault()).thenReturn(teamScope);
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(1L);
        userTeam.setTeamId(100);
        userTeam.setScopeId(300);
        Mockito.when(profileService.findCurrentTeamOfUser(1)).thenReturn(userTeam);

        {
            Map<String, Object> data = Maps.create()
                    .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                    .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                    .put(Constants.FIELD_USER, 1)
                    .put(Constants.FIELD_GAME_ID, 1L)
                    .put("edata1", 12345)
                    .build();
            eventsService.submitEvent(tokenInternal, data);

            Mockito.verify(gameController).submitEvent(Mockito.eq(1L), Mockito.eq(tokenInternal), Mockito.anyMap());
        }
    }

    @Test
    public void testEventSubmitWithUserEmailOnly() throws Exception {
        Mockito.when(dataCache.getGameCount()).thenReturn(1);
        Mockito.when(dataCache.getDefGameId()).thenReturn(1L);

        {
            TeamProfile teamProfile = new TeamProfile();
            teamProfile.setId(1);
            Mockito.when(dataCache.getTeamDefault()).thenReturn(teamProfile);

            TeamScope teamScope = new TeamScope();
            teamScope.setId(1);
            Mockito.when(dataCache.getTeamScopeDefault()).thenReturn(teamScope);

            UserProfile user = new UserProfile();
            user.setId(100);
            user.setEmail("isuru@domain.com");
            Mockito.when(profileService.readUserProfile(user.getEmail())).thenReturn(user);
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(1L);
        userTeam.setTeamId(100);
        userTeam.setScopeId(300);
        Mockito.when(profileService.findCurrentTeamOfUser(1)).thenReturn(userTeam);

        {
            Map<String, Object> data = Maps.create()
                    .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                    .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                    .put(Constants.FIELD_USER, "isuru@domain.com")
                    .put("edata1", 123.45)
                    .build();
            eventsService.submitEvent(tokenInternal, data);

            Mockito.verify(gameController).submitEvent(Mockito.eq(1L), Mockito.eq(tokenInternal),
                    Mockito.anyMap());
        }
    }

    @Test
    public void testEventSubmitWithUserAndTeamName() throws Exception {
        Mockito.when(dataCache.getGameCount()).thenReturn(1);
        {
            TeamProfile teamProfile = new TeamProfile();
            teamProfile.setId(1);
            Mockito.when(dataCache.getTeamDefault()).thenReturn(teamProfile);

            TeamScope teamScope = new TeamScope();
            teamScope.setId(1);
            Mockito.when(dataCache.getTeamScopeDefault()).thenReturn(teamScope);
        }

        {
            TeamProfile teamProfile = new TeamProfile();
            teamProfile.setId(100);
            teamProfile.setName("test-team");
            Mockito.when(profileService.findTeamByName(teamProfile.getName()))
                    .thenReturn(teamProfile);
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(1L);
        userTeam.setTeamId(100);
        userTeam.setScopeId(300);
        Mockito.when(profileService.findCurrentTeamOfUser(1)).thenReturn(userTeam);

        {
            Map<String, Object> data = Maps.create()
                    .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                    .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                    .put(Constants.FIELD_USER, 1)
                    .put(Constants.FIELD_TEAM, "test-team")
                    .put(Constants.FIELD_GAME_ID, 1L)
                    .put("edata1", 12345)
                    .build();
            eventsService.submitEvent(tokenInternal, data);

            Mockito.verify(gameController).submitEvent(Mockito.eq(1L), Mockito.eq(tokenInternal), Mockito.anyMap());
        }
    }

    @Test
    public void testEventSubmitWithUserAndTeamId() throws Exception {
        Mockito.when(dataCache.getGameCount()).thenReturn(1);
        {
            TeamProfile teamProfile = new TeamProfile();
            teamProfile.setId(1);
            Mockito.when(dataCache.getTeamDefault()).thenReturn(teamProfile);

            TeamScope teamScope = new TeamScope();
            teamScope.setId(1);
            Mockito.when(dataCache.getTeamScopeDefault()).thenReturn(teamScope);
        }

        {
            TeamProfile teamProfile = new TeamProfile();
            teamProfile.setId(100);
            teamProfile.setName("test-team");
            Mockito.when(profileService.findTeamByName(teamProfile.getName()))
                    .thenReturn(teamProfile);
            Mockito.when(profileService.readTeam(100))
                    .thenReturn(teamProfile);
        }

        UserTeam userTeam = new UserTeam();
        userTeam.setUserId(1L);
        userTeam.setTeamId(100);
        userTeam.setScopeId(300);
        Mockito.when(profileService.findCurrentTeamOfUser(1)).thenReturn(userTeam);

        {
            Map<String, Object> data = Maps.create()
                    .put(Constants.FIELD_EVENT_TYPE, "so.event.type")
                    .put(Constants.FIELD_TIMESTAMP, System.currentTimeMillis())
                    .put(Constants.FIELD_USER, 1)
                    .put(Constants.FIELD_TEAM, 100)
                    .put(Constants.FIELD_GAME_ID, 1L)
                    .put("edata1", 12345)
                    .build();
            eventsService.submitEvent(tokenInternal, data);

            Mockito.verify(gameController).submitEvent(Mockito.eq(1L), Mockito.eq(tokenInternal), Mockito.anyMap());
        }
    }

}

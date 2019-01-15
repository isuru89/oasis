package io.github.isuru.oasis.services.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.services.IOasisApiService;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.test.routes.IOasisApi;
import io.github.isuru.oasis.services.test.routes.IOasisDefApi;
import okhttp3.Headers;
import org.junit.jupiter.api.Assertions;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class RoutingTest extends AbstractApiTest {

    private static final String APP_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";

    private static IOasisApi api;
    protected static IOasisDefApi defApi;

    private static IOasisApiService apiService;
    private static IOasisDao dao;

    private static String adminToken;
    private static String curatorToken;
    private static String playerToken;


    IOasisApi getApi() {
        return api;
    }

    <T> T shouldBeSuccess(Call<T> call) throws IOException {
        Response<T> execute = call.execute();
        Assertions.assertEquals(200, execute.code());
        Headers headers = execute.headers();
        Assertions.assertTrue(headers.names().contains(CONTENT_TYPE));
        Assertions.assertEquals(APP_JSON, headers.get(CONTENT_TYPE));
        return execute.body();
    }

    <T> T shouldForbidden(Call<T> call) throws IOException {
        Response<T> execute = call.execute();
        Assertions.assertEquals(401, execute.code());
        return execute.body();
    }

    <T> T shouldInternalError(Call<T> call) throws IOException {
        Response<T> execute = call.execute();
        Assertions.assertEquals(500, execute.code());
        return execute.body();
    }

    <T> T shouldBadRequest(Call<T> call) throws IOException {
        Response<T> execute = call.execute();
        Assertions.assertEquals(400, execute.code());
        return execute.body();
    }

    String getAdminToken() throws IOException {
        if (adminToken != null) {
            return adminToken;
        }
        String up = "admin@oasis.com:admin";
        String encoded = "Basic " + Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> login = shouldBeSuccess(getApi().login(encoded));
        adminToken =  "Bearer " + login.get("token");
        return adminToken;
    }

    String getNoUserToken() {
        return "";
    }

    String getCuratorToken() throws IOException {
        if (curatorToken != null) return curatorToken;
        String up = "curator@oasis.com:curator";
        String encoded = "Basic " + Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> login = shouldBeSuccess(getApi().login(encoded));
        curatorToken = "Bearer " + login.get("token");
        return curatorToken;
    }

    String getPlayerToken() throws IOException {
        if (playerToken != null) return playerToken;
        String up = "player@oasis.com:player";
        String encoded = "Basic " + Base64.getEncoder().encodeToString(up.getBytes(StandardCharsets.UTF_8));
        Map<String, Object> login = shouldBeSuccess(getApi().login(encoded));
        playerToken = "Bearer " + login.get("token");
        return playerToken;
    }

    static void startServer() throws Exception {
//        Configs configs = Configs.create();
//        System.setProperty("oasis.mode", "local");
//        configs.append("oasis.config.file", "./src/test/resources/oasis/configs/oasis.properties," +
//                "./src/test/resources/oasis/configs/jdbc.properties," +
//                "./src/test/resources/oasis/configs/ldap.properties");
//        OasisServer.loadConfigFiles(configs, configs.getStrReq("oasis.config.file"));
//        configs.append("oasis.public.key", "../configs/auth/public.der");
//        configs.append("oasis.private.key", "../configs/auth/private.der");
//        configs.append("oasis.logs.config.file", "./src/test/resources/oasis/configs/logger.properties");
//        OasisServer.start(configs);
    }

    static void runBeforeApi() {
//        apiService = OasisServer.apiService;
        dao = apiService.getDao();

        ObjectMapper mapper = new ObjectMapper();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:5885/api/v1/")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
        api = retrofit.create(IOasisApi.class);
        defApi = retrofit.create(IOasisDefApi.class);
    }

    static void createUsers() throws Exception {
        IProfileService profileService = apiService.getProfileService();

        TeamScope scope = new TeamScope();
        scope.setName("testoasis");
        scope.setDisplayName("Test-OASIS");
        long scopeId = profileService.addTeamScope(scope);

        TeamProfile test = new TeamProfile();
        test.setName("test-group");
        test.setTeamScope((int) scopeId);
        long teamId = profileService.addTeam(test);

//        UserProfile admin = new UserProfile();
//        admin.setName("admin");
//        admin.setMale(true);
//        admin.setEmail("admin@oasis.com");
//        long adminId = profileService.addUserProfile(admin);
//
//        UserProfile curator = new UserProfile();
//        curator.setEmail("curator@oasis.com");
//        curator.setMale(false);
//        curator.setName("curator");
//        long curatorId = profileService.addUserProfile(curator);
//
//        UserProfile player = new UserProfile();
//        player.setEmail("player@oasis.com");
//        player.setMale(true);
//        player.setName("player");
//        long playerId = profileService.addUserProfile(player);

//        profileService.addUserToTeam(adminId, teamId, UserRole.ADMIN);
//        profileService.addUserToTeam(curatorId, teamId, UserRole.CURATOR);
//        profileService.addUserToTeam(playerId, teamId, UserRole.PLAYER);

//        UserTeam profile = profileService.findCurrentTeamOfUser(adminId);
//        Assertions.assertEquals(UserRole.ADMIN, (int) profile.getRoleId());
//        profile = profileService.findCurrentTeamOfUser(curatorId);
//        Assertions.assertEquals(UserRole.CURATOR, (int) profile.getRoleId());
//        profile = profileService.findCurrentTeamOfUser(playerId);
//        Assertions.assertEquals(UserRole.PLAYER, (int) profile.getRoleId());
    }

    protected static IOasisDao getDao() {
        return dao;
    }

    protected static IOasisApiService getApiService() {
        return apiService;
    }

}

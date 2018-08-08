package io.github.isuru.oasis.services.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.services.OasisServer;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.api.impl.DefaultOasisApiService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.UserRole;
import io.github.isuru.oasis.services.test.routes.IOasisApi;
import okhttp3.Headers;
import org.junit.jupiter.api.Assertions;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;
import java.util.Map;

/**
 * @author iweerarathna
 */
class RoutingTest extends AbstractApiTest {

    private static final String APP_JSON = "application/json";
    private static final String CONTENT_TYPE = "Content-Type";

    private static IOasisApi api;
    private static DefaultOasisApiService apiService;
    private static IOasisDao dao;

    IOasisApi getApi() {
        return api;
    }

    Map<String, Object> shouldBeSuccess(Call<Map<String, Object>> call) throws IOException {
        Response<Map<String, Object>> execute = call.execute();
        Assertions.assertEquals(200, execute.code());
        Headers headers = execute.headers();
        Assertions.assertTrue(headers.names().contains(CONTENT_TYPE));
        Assertions.assertEquals(APP_JSON, headers.get(CONTENT_TYPE));
        return execute.body();
    }

    Map<String, Object> shouldForbidden(Call<Map<String, Object>> call) throws IOException {
        Response<Map<String, Object>> execute = call.execute();
        Assertions.assertEquals(401, execute.code());
        return execute.body();
    }

    Map<String, Object> shouldInternalError(Call<Map<String, Object>> call) throws IOException {
        Response<Map<String, Object>> execute = call.execute();
        Assertions.assertEquals(500, execute.code());
        return execute.body();
    }

    Map<String, Object> shouldBadRequest(Call<Map<String, Object>> call) throws IOException {
        Response<Map<String, Object>> execute = call.execute();
        Assertions.assertEquals(400, execute.code());
        return execute.body();
    }

    static void startServer() throws Exception {
        Configs configs = Configs.get();
        configs.append("oasis.config.file", "./src/test/resources/oasis/configs/oasis.properties," +
                "./src/test/resources/oasis/configs/jdbc.properties," +
                "./src/test/resources/oasis/configs/ldap.properties");
        configs.append("oasis.logs.config.file", "./src/test/resources/oasis/configs/logger.properties");
        OasisServer.main(new String[0]);
    }

    static void runBeforeApi() {
        apiService = (DefaultOasisApiService) OasisServer.apiService;
        dao = apiService.getDao();

        ObjectMapper mapper = new ObjectMapper();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://localhost:5885/api/v1/")
                .addConverterFactory(JacksonConverterFactory.create(mapper))
                .build();
        api = retrofit.create(IOasisApi.class);
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

        UserProfile admin = new UserProfile();
        admin.setName("admin");
        admin.setMale(true);
        admin.setEmail("admin@oasis.com");
        long adminId = profileService.addUserProfile(admin);

        UserProfile curator = new UserProfile();
        curator.setEmail("curator@oasis.com");
        curator.setMale(false);
        curator.setName("curator");
        long curatorId = profileService.addUserProfile(curator);

        UserProfile player = new UserProfile();
        player.setEmail("player@oasis.com");
        player.setMale(true);
        player.setName("player");
        long playerId = profileService.addUserProfile(player);

        profileService.addUserToTeam(adminId, teamId, UserRole.ADMIN);
        profileService.addUserToTeam(curatorId, teamId, UserRole.CURATOR);
        profileService.addUserToTeam(playerId, teamId, UserRole.PLAYER);

        UserTeam profile = profileService.findCurrentTeamOfUser(adminId);
        Assertions.assertEquals(UserRole.ADMIN, (int) profile.getRoleId());
        profile = profileService.findCurrentTeamOfUser(curatorId);
        Assertions.assertEquals(UserRole.CURATOR, (int) profile.getRoleId());
        profile = profileService.findCurrentTeamOfUser(playerId);
        Assertions.assertEquals(UserRole.PLAYER, (int) profile.getRoleId());
    }

    protected static IOasisDao getDao() {
        return dao;
    }

    protected static DefaultOasisApiService getApiService() {
        return apiService;
    }

}

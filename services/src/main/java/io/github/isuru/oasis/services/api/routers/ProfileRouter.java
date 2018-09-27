package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.OasisOptions;
import io.github.isuru.oasis.services.utils.UserRole;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class ProfileRouter extends BaseRouters {

    private static final String USER_ID = "userId";
    private static final String TEAM_ID = "teamId";
    private static final String SCOPE_ID = "scopeId";

    ProfileRouter(IOasisApiService apiService, OasisOptions oasisOptions) {
        super(apiService, oasisOptions);
    }

    @Override
    public void register() {
        IProfileService ps = getApiService().getProfileService();

        Spark.before("/*", (request, response) -> checkAuth(request));

        // user related end-points
        //
        post("/user/add", this::addUser, UserRole.ADMIN)
        .post("/user/:userId/edit", this::editUser)
        .get("/user/:userId", (req, res) -> ps.readUserProfile(asPLong(req, USER_ID)))
        .get("/user/ext/:userId", (req, res) -> ps.readUserProfileByExtId(asPLong(req, USER_ID)))
        .delete("/user/:userId", this::deleteUser, UserRole.ADMIN);

        // team end points
        //
        post("/team/add", this::addTeam, UserRole.CURATOR)
        .post("/team/:teamId/edit", this::editTeam)
        .get("/team/:teamId", (req, res) -> ps.readTeam(asPLong(req, TEAM_ID)))
        .post("/team/:teamId/users", this::findUsersInTeam);

        // team scope end points
        //
        post("/scope/add", this::addScope, UserRole.ADMIN)
        .post("/scope/:scopeId/edit", this::editScope)
        .post("/scope/list", (req, res) -> ps.listTeamScopes())
        .get("/scope/:scopeId", (req, res) -> ps.readTeamScope(asPLong(req, SCOPE_ID)))
        .post("/scope/:scopeId/teams", (req, res) -> ps.listTeams(asPLong(req, SCOPE_ID)));

        post("/user/add-to-team", this::addUserToTeam, UserRole.CURATOR)
        .post("/user/:userId/current-team", this::findUserTeam);
    }

    private IProfileService getProfileService() {
        return getApiService().getProfileService();
    }

    private Object deleteUser(Request req, Response res) throws Exception {
        return asResBool(getProfileService().deleteUserProfile(asPLong(req, USER_ID)));
    }

    private Object addUser(Request req, Response res) throws Exception {
        return asResAdd(getProfileService().addUserProfile(bodyAs(req, UserProfile.class)));
    }

    private Object editUser(Request req, Response res) throws Exception {
        long tid = asPLong(req, USER_ID);
        checkSameUser(req, tid);

        return asResBool(getProfileService().editUserProfile(
                asPLong(req, USER_ID),
                bodyAs(req, UserProfile.class)));
    }

    private Object findUsersInTeam(Request req, Response res) throws Exception {
        return getProfileService().listUsers(
                asPLong(req, TEAM_ID),
                asQLong(req, "start", 0),
                asQLong(req, "size", 50));
    }

    private Object editTeam(Request req, Response res) throws Exception {
        return asResBool(getProfileService().editTeam(
                asPLong(req, TEAM_ID),
                bodyAs(req, TeamProfile.class)));
    }

    private Object addTeam(Request req, Response res) throws Exception {
        return asResAdd(getProfileService().addTeam(bodyAs(req, TeamProfile.class)));
    }

    private Object addScope(Request req, Response res) throws Exception {
        return asResAdd(getProfileService().addTeamScope(bodyAs(req, TeamScope.class)));
    }

    private Object editScope(Request req, Response res) throws Exception {
        return asResBool(getProfileService().editTeamScope(
                asPLong(req, SCOPE_ID),
                bodyAs(req, TeamScope.class)));
    }

    private Object findUserTeam(Request req, Response res) throws Exception {
        return getProfileService().findCurrentTeamOfUser(asPLong(req, USER_ID));
    }

    private Object addUserToTeam(Request req, Response res) throws Exception {
        UserTeam userTeam = bodyAs(req, UserTeam.class);
        return asResBool(getProfileService().addUserToTeam(
                userTeam.getUserId(), userTeam.getTeamId(), userTeam.getRoleId()));
    }
}

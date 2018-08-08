package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.UserRole;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class ProfileRouter extends BaseRouters {
    ProfileRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        Spark.before("/*", new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                checkAuth(request);
            }
        });

        post("/user/add", (req, res) -> {
            checkAdmin(req);
            return getApiService().getProfileService().addUserProfile(bodyAs(req, UserProfile.class));
        })
        .post("/user/:uid/edit", (req, res) -> {
            long tid = asPLong(req, "uid");
            checkSameUser(req, tid);

            return getApiService().getProfileService().editUserProfile(
                    asPLong(req, "uid"),
                    bodyAs(req, UserProfile.class));
        })
        .get("/user/:uid", (req, res) -> {
            return getApiService().getProfileService().readUserProfile(asPLong(req,"uid"));
        })
        .get("/user/ext/:uid", (req, res) -> {
            return getApiService().getProfileService().readUserProfileByExtId(asPLong(req,"uid"));
        })
        .delete("/user/:uid", (req, res) ->
                getApiService().getProfileService().deleteUserProfile(asPLong(req,"uid")),
                UserRole.ADMIN);

        // team end points
        //
        post("/team/add", (req, res) -> {
            checkCurator(req);
            return getApiService().getProfileService().addTeam(bodyAs(req, TeamProfile.class));
        })
        .post("/team/:tid/edit", (req, res) -> {
            return getApiService().getProfileService().editTeam(
                    asPLong(req, "tid"),
                    bodyAs(req, TeamProfile.class));
        })
        .get("/team/:tid", (req, res) -> {
            return getApiService().getProfileService().readTeam(asPLong(req,"tid"));
        })
        .post("/team/:tid/users", (req, res) -> {
            return getApiService().getProfileService().listUsers(
                    asPLong(req, "tid"),
                    asQLong(req, "start", 0),
                    asQLong(req, "size", 50));
        });

        // team scope end points
        //
        post("/scope/add", (req, res) -> {
            checkAdmin(req);
            return getApiService().getProfileService().addTeamScope(bodyAs(req, TeamScope.class));
        })
        .post("/scope/:tsid/edit", (req, res) -> {
            return getApiService().getProfileService().editTeamScope(
                    asPLong(req, "tsid"),
                    bodyAs(req, TeamScope.class));
        })
        .post("/scope/list", (req, res) -> {
            return getApiService().getProfileService().listTeamScopes();
        })
        .get("/scope/:tsid", (req, res) -> {
            return getApiService().getProfileService().readTeamScope(asPLong(req,"tsid"));
        })
        .post("/scope/:tsid/teams", (req, res) -> {
            return getApiService().getProfileService().listTeams(asPLong(req, "tsid"));
        });


        post("/user/add-to-team/", (req, res) -> {
            checkCurator(req);

            UserTeam userTeam = bodyAs(req, UserTeam.class);
            return getApiService().getProfileService().addUserToTeam(userTeam.getUserId(), userTeam.getTeamId(),
                    userTeam.getRoleId());
        })
        .post("/user/:uid/current-team", (req, res) -> {
            return getApiService().getProfileService().findCurrentTeamOfUser(asPLong(req, "uid"));
        });

    }
}

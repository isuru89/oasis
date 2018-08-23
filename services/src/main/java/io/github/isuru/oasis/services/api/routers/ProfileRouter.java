package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
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
        IProfileService ps = getApiService().getProfileService();

        Spark.before("/*", new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                checkAuth(request);
            }
        });

        post("/user/add", (req, res) -> {
            checkAdmin(req);
            return asResAdd(ps.addUserProfile(bodyAs(req, UserProfile.class)));
        })
        .post("/user/:uid/edit", (req, res) -> {
            long tid = asPLong(req, "uid");
            checkSameUser(req, tid);

            return asResBool(ps.editUserProfile(asPLong(req, "uid"),
                    bodyAs(req, UserProfile.class)));
        })
        .get("/user/:uid", (req, res) -> ps.readUserProfile(asPLong(req,"uid")))
        .get("/user/ext/:uid", (req, res) -> ps.readUserProfileByExtId(asPLong(req,"uid")))
        .delete("/user/:uid", (req, res) ->
                asResBool(ps.deleteUserProfile(asPLong(req,"uid"))),
                UserRole.ADMIN);

        // team end points
        //
        post("/team/add",
                (req, res) -> {
                    checkCurator(req);
                    return asResAdd(ps.addTeam(bodyAs(req, TeamProfile.class)));
                })
        .post("/team/:tid/edit",
                (req, res) -> asResBool(ps.editTeam(asPLong(req, "tid"), bodyAs(req, TeamProfile.class))))
        .get("/team/:tid",
                (req, res) -> ps.readTeam(asPLong(req,"tid")))
        .post("/team/:tid/users",
                (req, res) -> ps.listUsers(asPLong(req, "tid"),
                        asQLong(req, "start", 0), asQLong(req, "size", 50)));

        // team scope end points
        //
        post("/scope/add", (req, res) -> {
            checkAdmin(req);
            return asResAdd(ps.addTeamScope(bodyAs(req, TeamScope.class)));
        })
        .post("/scope/:tsid/edit",
                (req, res) -> asResBool(ps.editTeamScope(asPLong(req, "tsid"), bodyAs(req, TeamScope.class))))
        .post("/scope/list", (req, res) -> ps.listTeamScopes())
        .get("/scope/:tsid", (req, res) -> ps.readTeamScope(asPLong(req,"tsid")))
        .post("/scope/:tsid/teams", (req, res) -> ps.listTeams(asPLong(req, "tsid")));


        post("/user/add-to-team", (req, res) -> {
            checkCurator(req);

            UserTeam userTeam = bodyAs(req, UserTeam.class);
            return asResBool(ps.addUserToTeam(userTeam.getUserId(),
                    userTeam.getTeamId(), userTeam.getRoleId()));
        })
        .post("/user/:uid/current-team", (req, res) -> ps.findCurrentTeamOfUser(asPLong(req, "uid")));

    }
}

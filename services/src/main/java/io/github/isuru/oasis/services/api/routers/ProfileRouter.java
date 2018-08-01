package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import spark.Filter;
import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class ProfileRouter extends BaseRouters {
    public ProfileRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        Spark.before("/*", new Filter() {
            @Override
            public void handle(Request request, Response response) throws Exception {
                if (!checkAuth(request)) {
                    Spark.halt(401);
                }
            }
        });

        post("/user/add", (req, res) -> {
            return getApiService().getProfileService().addUserProfile(bodyAs(req, UserProfile.class));
        })
        .post("/user/:uid/edit", (req, res) -> {
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
        .delete("/user/:uid", (req, res) -> {
            return getApiService().getProfileService().deleteUserProfile(asPLong(req,"uid"));
        });

        // team end points
        //
        post("/team/add", (req, res) -> {
            return getApiService().getProfileService().addTeam(bodyAs(req, TeamProfile.class));
        })
        .post("/team/:tid/edit", (req, res) -> {
            return getApiService().getProfileService().editTeam(
                    asPLong(req, "tid"),
                    bodyAs(req, TeamProfile.class));
        })
        .get("/team/:tid", (req, res) -> {
            return getApiService().getProfileService().readTeam(asPLong(req,"tid"));
        });

        post("/user/add-to-team/", (req, res) -> {
            UserTeam userTeam = bodyAs(req, UserTeam.class);
            return getApiService().getProfileService().addUserToTeam(userTeam.getUserId(), userTeam.getTeamId());
        })
        .post("/user/:uid/current-team", (req, res) -> {
            return getApiService().getProfileService().findCurrentTeamOfUser(asPLong(req, "uid"));
        });

    }
}

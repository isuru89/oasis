package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.Pojos;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class ProfileService extends BaseService implements IProfileService {

    ProfileService(IOasisDao dao) {
        super(dao);
    }

    @Override
    public long addUserProfile(UserProfile profile) throws Exception {
        Map<String, Object> data = Maps.create()
                .put("name", profile.getName())
                .put("male", profile.isMale())
                .put("avatarId", profile.getAvatarId())
                .put("extId", profile.getExtId())
                .put("email", profile.getEmail())
                .build();

        return getDao().executeInsert("profile/addUser", data, "user_id");
    }

    @Override
    public UserProfile readUserProfile(long userId) throws Exception {
        return getTheOnlyRecord("profile/readUser",
                Maps.create("userId", userId),
                UserProfile.class);
    }

    @Override
    public UserProfile readUserProfileByExtId(long extUserId) throws Exception {
        return getTheOnlyRecord("profile/readUserByExtId",
                Maps.create("extId", extUserId),
                UserProfile.class);
    }

    @Override
    public boolean editUserProfile(long userId, UserProfile latest) throws Exception {
        UserProfile prev = readUserProfile(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("name", Pojos.compareWith(latest.getName(), prev.getName()));
        data.put("avatarId", Pojos.compareWith(latest.getAvatarId(), prev.getAvatarId()));
        data.put("isMale", latest.isMale());
        data.put("userId", userId);

        return getDao().executeCommand("profile/editUser", data) > 0;
    }

    @Override
    public boolean deleteUserProfile(long userId) throws Exception {
        return getDao().executeCommand("profile/disableUser", Maps.create("userId", userId)) > 0;
    }

    @Override
    public long addTeam(TeamProfile teamProfile) throws Exception {
        Map<String, Object> data = Maps.create()
                .put("teamScope", teamProfile.getTeamScope())
                .put("name", teamProfile.getName())
                .put("avatarId", teamProfile.getAvatarId())
                .build();

        return getDao().executeInsert("profile/addTeam", data, "team_id");
    }

    @Override
    public TeamProfile readTeam(long teamId) throws Exception {
        return getTheOnlyRecord("profile/readTeam",
                Maps.create("teamId", teamId),
                TeamProfile.class);
    }

    @Override
    public boolean editTeam(long teamId, TeamProfile latest) throws Exception {
        TeamProfile prev = readTeam(teamId);
        Map<String, Object> data = Maps.create()
                .put("name", Pojos.compareWith(latest.getName(), prev.getName()))
                .put("avatarId", Pojos.compareWith(latest.getAvatarId(), prev.getAvatarId()))
                .put("teamScope", Pojos.compareWith(latest.getTeamScope(), prev.getTeamScope()))
                .put("teamId", teamId)
                .build();

        return getDao().executeCommand("profile/editTeam", data) > 0;
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId) throws Exception {
        return getDao().executeInsert("profile/addUserToTeam",
                Maps.create()
                        .put("userId", userId)
                        .put("teamId", teamId)
                        .put("since", System.currentTimeMillis())
                        .build(),
                null) > 0;
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId) throws Exception {
        return getTheOnlyRecord("profile/findCurrentTeamOfUser",
                Maps.create().put("userId", userId)
                    .put("currentEpoch", System.currentTimeMillis())
                    .build(),
                UserTeam.class);
    }

    @Override
    public void readUserGameStats(long userId) {

    }

    @Override
    public void readUserGameTimeline(long userId) {

    }

    @Override
    public void readUserBadges(long userId) {

    }

    @Override
    public void readUserPoints(long userId) {

    }

    @Override
    public void readUserMilestones(long userId) {

    }

    @Override
    public void readUserRankings(long userId) {

    }
}

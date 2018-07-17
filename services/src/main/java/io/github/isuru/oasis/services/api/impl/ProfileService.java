package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.db.IOasisDao;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.utils.Maps;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class ProfileService extends BaseService implements IProfileService {

    protected ProfileService(IOasisDao dao) {
        super(dao);
    }

    @Override
    public void addUserProfile(UserProfile profile) throws Exception {
        Map<String, Object> data = Maps.create()
                .put("userId", profile.getId())
                .put("name", profile.getName())
                .put("male", profile.isMale())
                .put("avatarId", profile.getAvatarId())
                .build();

        getDao().executeCommand("profile/addUser", data);
    }

    @Override
    public UserProfile readUserProfile(long userId) throws Exception {
        return getTheOnlyRecord("profile/readUser",
                Maps.create("userId", userId),
                UserProfile.class);
    }

    @Override
    public void editUserProfile(long userId, UserProfile profile) {

    }

    @Override
    public boolean deleteUserProfile(long userId) throws Exception {
        return getDao().executeCommand("profile/disableUser", Maps.create("userId", userId)) > 0;
    }

    @Override
    public void addTeam(TeamProfile teamProfile) {

    }

    @Override
    public TeamProfile readTeam(long teamId) {
        return null;
    }

    @Override
    public void addUserToTeam(long userId, long teamId) {

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

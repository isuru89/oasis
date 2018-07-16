package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;

/**
 * @author iweerarathna
 */
public class ProfileService implements IProfileService {

    @Override
    public void addUserProfile(UserProfile profile) {

    }

    @Override
    public UserProfile readUserProfile(long userId) {
        return null;
    }

    @Override
    public void editUserProfile(long userId, UserProfile profile) {

    }

    @Override
    public void deleteUserProfile(long userId) {

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

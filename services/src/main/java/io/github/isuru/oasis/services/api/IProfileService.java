package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;

public interface IProfileService {

    long addUserProfile(UserProfile profile) throws Exception;
    UserProfile readUserProfile(long userId) throws Exception;
    UserProfile readUserProfileByExtId(long extUserId) throws Exception;
    boolean editUserProfile(long userId, UserProfile profile) throws Exception;
    boolean deleteUserProfile(long userId) throws Exception;

    void addTeam(TeamProfile teamProfile);
    TeamProfile readTeam(long teamId);

    void addUserToTeam(long userId, long teamId);

    void readUserGameStats(long userId);
    void readUserGameTimeline(long userId);
    void readUserBadges(long userId);
    void readUserPoints(long userId);
    void readUserMilestones(long userId);
    void readUserRankings(long userId);


}

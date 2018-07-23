package io.github.isuru.oasis.services.api;

import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;

public interface IProfileService {

    long addUserProfile(UserProfile profile) throws Exception;
    UserProfile readUserProfile(long userId) throws Exception;
    UserProfile readUserProfileByExtId(long extUserId) throws Exception;
    boolean editUserProfile(long userId, UserProfile profile) throws Exception;
    boolean deleteUserProfile(long userId) throws Exception;

    long addTeam(TeamProfile teamProfile) throws Exception;
    TeamProfile readTeam(long teamId) throws Exception;
    boolean editTeam(long teamId, TeamProfile teamProfile) throws Exception;

    boolean addUserToTeam(long userId, long teamId) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId) throws Exception;

    void readUserGameStats(long userId);
    void readUserGameTimeline(long userId);
    void readUserBadges(long userId);
    void readUserPoints(long userId);
    void readUserMilestones(long userId);
    void readUserRankings(long userId);


}

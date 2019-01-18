package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.services.dto.edits.TeamProfileEditDto;
import io.github.isuru.oasis.services.dto.edits.TeamScopeEditDto;
import io.github.isuru.oasis.services.dto.edits.UserProfileEditDto;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.UserTeamScope;

import java.util.List;

public interface IProfileService {

    long addUserProfile(UserProfile profile) throws Exception;
    UserProfile readUserProfile(long userId) throws Exception;
    UserProfile readUserProfile(String email) throws Exception;
    UserProfile readUserProfileByExtId(long extUserId) throws Exception;
    boolean editUserProfile(long userId, UserProfileEditDto profile) throws Exception;
    boolean deleteUserProfile(long userId) throws Exception;
    List<UserProfile> findUser(String email, String name) throws Exception;

    List<UserProfile> listUsers(long teamId, long offset, long size) throws Exception;

    long addTeam(TeamProfile teamProfile) throws Exception;
    TeamProfile readTeam(long teamId) throws Exception;
    boolean editTeam(long teamId, TeamProfileEditDto teamProfile) throws Exception;
    List<TeamProfile> listTeams(long scopeId) throws Exception;

    long addTeamScope(TeamScope teamScope) throws Exception;
    TeamScope readTeamScope(long scopeId) throws Exception;
    TeamScope readTeamScope(String scopeName) throws Exception;
    List<TeamScope> listTeamScopes() throws Exception;
    boolean editTeamScope(long scopeId, TeamScopeEditDto scope) throws Exception;

    boolean addUserToTeam(long userId, long teamId, int roleId) throws Exception;
    boolean addUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval) throws Exception;
    boolean addUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval,
                          long since) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly) throws Exception;
    UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly, long atTime) throws Exception;
    TeamProfile findTeamByName(String name) throws Exception;

    long requestForRole(long byUser, int teamScopeId, int roleId, long startTime) throws Exception;
    boolean rejectRequestedRole(int requestId, long rejectedBy) throws Exception;
    boolean removeCurrentRole(long userId, int teamScopeId, long endTime, long removedBy) throws Exception;
    boolean approveRole(int requestId, long approvedTime, long approvedBy) throws Exception;
    List<UserTeamScope> listCurrentUserRoles(long userId) throws Exception;

    boolean logoutUser(long userId, long ts) throws Exception;

}

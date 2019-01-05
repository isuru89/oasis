package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.*;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.Pojos;
import io.github.isuru.oasis.services.utils.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.*;

/**
 * @author iweerarathna
 */
@Service("profileService")
public class ProfileServiceImpl implements IProfileService {

    @Autowired
    private IOasisDao dao;

    @Autowired
    private IGameDefService gameDefService;

    @Override
    public long addUserProfile(UserProfile profile) throws Exception {
        Checks.nonNullOrEmpty(profile.getEmail(), "email");
        Checks.nonNullOrEmpty(profile.getName(), "name");

        Map<String, Object> data = Maps.create()
                .put("name", profile.getName())
                .put("male", profile.isMale())
                .put("avatarId", profile.getAvatarId())
                .put("extId", profile.getExtId())
                .put("email", profile.getEmail())
                .put("isAutoUser", false)
                .build();

        return dao.executeInsert(Q.PROFILE.ADD_USER, data, "user_id");
    }

    @Override
    public UserProfile readUserProfile(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_USER,
                Maps.create("userId", userId),
                UserProfile.class);
    }

    @Override
    public UserProfile readUserProfile(String email) throws Exception {
        Checks.nonNullOrEmpty(email, "email");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_USER_BY_EMAIL,
                Maps.create("email", email),
                UserProfile.class);
    }

    @Override
    public UserProfile readUserProfileByExtId(long extUserId) throws Exception {
        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_USER_BY_EXTID,
                Maps.create("extId", extUserId),
                UserProfile.class);
    }

    @Override
    public boolean editUserProfile(long userId, UserProfile latest) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        UserProfile prev = readUserProfile(userId);
        Map<String, Object> data = new HashMap<>();
        data.put("name", Pojos.compareWith(latest.getName(), prev.getName()));
        data.put("avatarId", Pojos.compareWith(latest.getAvatarId(), prev.getAvatarId()));
        data.put("isMale", latest.isMale());
        data.put("userId", userId);

        return dao.executeCommand(Q.PROFILE.EDIT_USER, data) > 0;
    }

    @Override
    public boolean deleteUserProfile(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return dao.executeCommand(Q.PROFILE.DISABLE_USER, Maps.create("userId", userId)) > 0;
    }

    @Override
    public List<UserProfile> findUser(String email, String name) throws Exception {
        Checks.nonNullOrEmpty(email, "email");

        if (email.length() < 4) {
            return new LinkedList<>();
        }

        String param = email.replace("!", "!!")
                            .replace("%", "!%")
                            .replace("_", "!_")
                            .replace("[", "![");

        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.SEARCH_USER,
                Maps.create()
                    .put("email", "%" + param + "%")
                    .put("name", name)
                    .build(),
                UserProfile.class,
                Maps.create("hasName", name != null && name.length() > 3)
        ));
    }

    @Override
    public List<UserProfile> listUsers(long teamId, long offset, long size) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");
        Checks.nonNegative(offset, "offset");
        Checks.nonNegative(size, "size");

        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.LIST_USERS_OF_TEAM,
                Maps.create()
                        .put("teamId", teamId)
                        .put("offset", offset)
                        .put("limit", size).build(),
                UserProfile.class
        ));
    }

    @Override
    public long addTeam(TeamProfile teamProfile) throws Exception {
        Checks.nonNullOrEmpty(teamProfile.getName(), "name");
        Checks.greaterThanZero(teamProfile.getTeamScope(), "scope");

        return (Long) dao.runTx(Connection.TRANSACTION_READ_COMMITTED, input -> {
            Map<String, Object> data = Maps.create()
                    .put("teamScope", teamProfile.getTeamScope())
                    .put("name", teamProfile.getName())
                    .put("avatarId", teamProfile.getAvatarId())
                    .build();

            Long teamId = input.executeInsert(Q.PROFILE.ADD_TEAM, data, "team_id");

            // add user for team scope
            Map<String, Object> playerData = Maps.create()
                    .put("name", teamProfile.getName())
                    .put("male", false)
                    .put("avatarId", null)
                    .put("extId", null)
                    .put("email", "")
                    .put("isAutoUser", true)
                    .build();
            Long userId = input.executeInsert(Q.PROFILE.ADD_USER, playerData, "user_id");

            input.executeInsert(Q.PROFILE.ADD_USER_TO_TEAM,
                    Maps.create()
                            .put("userId", userId)
                            .put("teamId", teamId)
                            .put("roleId", UserRole.PLAYER)
                            .put("since", System.currentTimeMillis())
                            .build(),
                    null);
            return teamId;
        });
    }

    @Override
    public TeamProfile readTeam(long teamId) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_TEAM,
                Maps.create("teamId", teamId),
                TeamProfile.class);
    }

    @Override
    public boolean editTeam(long teamId, TeamProfile latest) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");

        TeamProfile prev = readTeam(teamId);
        Map<String, Object> data = Maps.create()
                .put("name", Pojos.compareWith(latest.getName(), prev.getName()))
                .put("avatarId", Pojos.compareWith(latest.getAvatarId(), prev.getAvatarId()))
                .put("teamScope", Pojos.compareWith(latest.getTeamScope(), prev.getTeamScope()))
                .put("teamId", teamId)
                .build();

        return dao.executeCommand(Q.PROFILE.EDIT_TEAM, data) > 0;
    }

    @Override
    public List<TeamProfile> listTeams(long scopeId) throws Exception {
        Checks.greaterThanZero(scopeId, "scopeId");

        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.LIST_TEAMS_OF_SCOPE,
                Maps.create("scopeId", scopeId),
                TeamProfile.class));
    }

    @Override
    public long addTeamScope(TeamScope teamScope) throws Exception {
        Checks.nonNullOrEmpty(teamScope.getName(), "name");
        Checks.nonNullOrEmpty(teamScope.getDisplayName(), "displayName");

        return (Long) dao.runTx(Connection.TRANSACTION_READ_COMMITTED, input -> {
            Map<String, Object> data = Maps.create()
                    .put("extId", teamScope.getExtId())
                    .put("name", teamScope.getName())
                    .put("displayName", teamScope.getDisplayName())
                    .build();

            Long addedScopeId = input.executeInsert(Q.PROFILE.ADD_TEAMSCOPE, data, "scope_id");

            // add default team
            Map<String, Object> teamData = Maps.create()
                    .put("teamScope", addedScopeId)
                    .put("name", "default_" + teamScope.getName())
                    .put("avatarId", null)
                    .build();
            Long addedTeamId = input.executeInsert(Q.PROFILE.ADD_TEAM, teamData, "team_id");

            // add user for team scope
            Map<String, Object> playerData = Maps.create()
                    .put("name", teamScope.getName())
                    .put("male", false)
                    .put("avatarId", null)
                    .put("extId", null)
                    .put("email", "default@"+teamScope.getName() + ".oasis.com")
                    .put("isAutoUser", true)
                    .build();
            Long userId = input.executeInsert(Q.PROFILE.ADD_USER, playerData, "user_id");

            input.executeInsert("profile/addUserToTeam",
                    Maps.create()
                            .put("userId", userId)
                            .put("teamId", addedTeamId)
                            .put("roleId", UserRole.PLAYER)
                            .put("since", System.currentTimeMillis())
                            .build(),
                    null);
            return addedScopeId;
        });
    }

    @Override
    public TeamScope readTeamScope(long scopeId) throws Exception {
        Checks.greaterThanZero(scopeId, "scopeId");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_TEAMSCOPE,
                Maps.create("scopeId", scopeId),
                TeamScope.class);
    }

    @Override
    public TeamScope readTeamScope(String scopeName) throws Exception {
        Checks.nonNullOrEmpty(scopeName, "scopeName");

        return ServiceUtils.getTheOnlyRecord(dao,Q.PROFILE.FIND_SCOPE_BY_NAME,
                Maps.create("scopeName", scopeName),
                TeamScope.class);
    }

    @Override
    public List<TeamScope> listTeamScopes() throws Exception {
        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.LIST_TEAM_SCOPES,
                null, TeamScope.class));
    }

    @Override
    public boolean editTeamScope(long scopeId, TeamScope latest) throws Exception {
        Checks.greaterThanZero(scopeId, "scopeId");

        TeamScope prev = readTeamScope(scopeId);
        Map<String, Object> data = Maps.create()
                .put("displayName", Pojos.compareWith(latest.getDisplayName(), prev.getDisplayName()))
                .put("scopeId", scopeId)
                .build();

        return dao.executeCommand(Q.PROFILE.EDIT_TEAMSCOPE, data) > 0;
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId, int roleId) throws Exception {
        return addUserToTeam(userId, teamId, roleId, false);
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval) throws Exception {
        Checks.greaterThanZero(userId, "userId");
        Checks.greaterThanZero(teamId, "teamId");
        Checks.validate(roleId > 0 && roleId <= UserRole.ALL_ROLE, "roleId must be a flag of 1,2,4, or 8.");

        UserTeam userTeam = findCurrentTeamOfUser(userId, false);
        if (userTeam != null && userTeam.getTeamId() == teamId) {
            // if the current team is same as previous team, then don't add
            if (roleId == userTeam.getRoleId()) {
                return false;
            }
        }

        // if the previous team is not yet approved, then disable it
        if (userTeam != null && !userTeam.isApproved()) {
            dao.executeCommand(Q.PROFILE.REJECT_USER_IN_TEAM,
                    Maps.create("id", userTeam.getId()));
        }

        return (Boolean) dao.runTx(Connection.TRANSACTION_READ_COMMITTED, ctx -> {
            long currentTimeMillis = System.currentTimeMillis();

            if (userTeam != null) {
                ctx.executeCommand(Q.PROFILE.DEALLOCATE_FROM_TEAM,
                        Maps.create()
                                .put("id", userTeam.getId())
                                .put("endTime", currentTimeMillis)
                                .build());
            }

            return ctx.executeCommand(Q.PROFILE.ADD_USER_TO_TEAM,
                    Maps.create()
                            .put("userId", userId)
                            .put("teamId", teamId)
                            .put("roleId", roleId)
                            .put("since", currentTimeMillis)
                            .put("isApproved", !pendingApproval)
                            .put("approvedAt", pendingApproval ? null : currentTimeMillis)
                            .build()) > 0;
        });
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId) throws Exception {
        return findCurrentTeamOfUser(userId, true);
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        long l = System.currentTimeMillis();
        // @TODO handle when no record is found
        Iterable<UserTeam> userTeams = dao.executeQuery(Q.PROFILE.FIND_CURRENT_TEAM_OF_USER,
                Maps.create().put("userId", userId)
                        .put("currentEpoch", l)
                        .build(),
                UserTeam.class,
                Maps.create("checkApproved", returnApprovedOnly));
        if (userTeams != null) {
            Iterator<UserTeam> iterator = userTeams.iterator();
            if (iterator.hasNext()) {
                return iterator.next();
            }
        }
        return null;
    }

    @Override
    public TeamProfile findTeamByName(String name) throws Exception {
        Checks.nonNullOrEmpty(name, "teamName");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.FIND_TEAM_BY_NAME,
                Maps.create("teamName", name),
                TeamProfile.class);
    }

    @Override
    public boolean logoutUser(long userId, long ts) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return dao.executeCommand(Q.PROFILE.LOGOUT_USER,
                Maps.create()
                    .put("userId", userId)
                    .put("logoutAt", ts).build()) > 0;
    }

    @Override
    public long requestForRole(long byUser, int teamScopeId, int roleId, long startTime) throws Exception {
        Checks.greaterThanZero(byUser, "byUser");
        Checks.greaterThanZero(teamScopeId, "teamScopeId");
        Checks.greaterThanZero(roleId, "roleId");
        Checks.greaterThanZero(startTime, "startTime");

        return dao.executeInsert("profile/flow/requestRole",
                Maps.create()
                    .put("teamScopeId", teamScopeId)
                    .put("userId", byUser)
                    .put("roleId", roleId)
                    .put("startTime", startTime)
                    .build(),
                "id");
    }

    @Override
    public boolean rejectRequestedRole(int requestId, long rejectedBy) throws Exception {
        Checks.greaterThanZero(requestId, "requestId");
        Checks.greaterThanZero(rejectedBy, "rejectedBy");

        UserTeamScope userTeamScope = ServiceUtils.getTheOnlyRecord(dao, "profile/flow/readRoleRequest",
                Maps.create("id", requestId),
                UserTeamScope.class);

        if (userTeamScope == null) {
            throw new InputValidationException("Given request id is not found in the system!");
        }
        if (userTeamScope.isApproved() || ServiceUtils.isValid(userTeamScope.getModifiedBy())) {
            throw new InputValidationException("Given request id is already has been approved or rejected!");
        }

        // @TODO check rejectedBy user has permissions

        return dao.executeCommand("profile/flow/rejectRole",
                Maps.create()
                    .put("id", requestId)
                    .put("modifiedBy", rejectedBy).build()) > 0;
    }

    @Override
    public boolean removeCurrentRole(long userId, int teamScopeId, long endTime, long removedBy) throws Exception {
        Checks.greaterThanZero(userId, "userId");
        Checks.greaterThanZero(teamScopeId, "teamScopeId");
        Checks.greaterThanZero(endTime, "endTime");
        Checks.greaterThanZero(removedBy, "removedBy");

        // @TODO check removed by user has the permissions

        return dao.executeCommand("profile/flow/removeRole",
                Maps.create()
                    .put("userId", userId)
                    .put("teamScopeId", teamScopeId)
                    .put("endTime", endTime)
                    .put("modifiedBy", removedBy)
                    .build()) > 0;
    }

    @Override
    public boolean approveRole(int requestId, long approvedTime, long approvedBy) throws Exception {
        Checks.greaterThanZero(requestId, "requestId");
        Checks.greaterThanZero(approvedBy, "approvedBy");
        Checks.greaterThanZero(approvedTime, "approvedTime");

        UserTeamScope userTeamScope = ServiceUtils.getTheOnlyRecord(dao, "profile/flow/readRoleRequest",
                Maps.create("id", requestId),
                UserTeamScope.class);

        if (userTeamScope == null) {
            throw new InputValidationException("Given request id is not found in the system!");
        }
        if (userTeamScope.isApproved() || ServiceUtils.isValid(userTeamScope.getModifiedBy())) {
            throw new InputValidationException("Given request id is already has been approved or rejected!");
        }

        // @TODO check removed by user has the permissions

        return dao.executeCommand("profile/flow/approveRole",
                Maps.create()
                        .put("id", requestId)
                        .put("approvedAt", approvedTime)
                        .put("modifiedBy", approvedBy)
                        .build()) > 0;
    }

    @Override
    public List<UserTeamScope> listCurrentUserRoles(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.toList(dao.executeQuery("profile/flow/listCurrentUserRoles",
                Maps.create("userId", userId),
                UserTeamScope.class));
    }
}

package io.github.isuru.oasis.services.services;

import com.github.slugify.Slugify;
import io.github.isuru.oasis.model.DefaultEntities;
import io.github.isuru.oasis.model.db.DbException;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.dto.edits.TeamProfileEditDto;
import io.github.isuru.oasis.services.dto.edits.TeamScopeEditDto;
import io.github.isuru.oasis.services.dto.edits.UserProfileEditDto;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserRole;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.model.UserTeamScope;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Commons;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.Pojos;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
@Service("profileService")
public class ProfileServiceImpl implements IProfileService {

    private static final Slugify SLUGIFY = new Slugify();

    @Autowired
    private IOasisDao dao;

    @Autowired
    private IGameDefService gameDefService;

    @Override
    public long addUserProfile(UserProfile profile) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(profile.getEmail(), "email");
        Checks.nonNullOrEmpty(profile.getName(), "name");

        Map<String, Object> templating = Maps.create("isActivated", profile.isActivated());

        Map<String, Object> data = Maps.create()
                .put("name", profile.getName())
                .put("nickname", profile.getNickName())
                .put("male", profile.isMale())
                .put("avatarId", profile.getAvatarId())
                .put("extId", profile.getExtId())
                .put("email", profile.getEmail())
                .put("isAutoUser", profile.isAutoUser())
                .put("activated", profile.isActivated())
                .build();

        return dao.executeInsert(Q.PROFILE.ADD_USER, data, templating, "user_id");
    }

    @Override
    public UserProfile readUserProfile(long userId) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_USER,
                Maps.create("userId", userId),
                UserProfile.class);
    }

    @Override
    public UserProfile readUserProfile(String email) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(email, "email");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_USER_BY_EMAIL,
                Maps.create("email", email),
                UserProfile.class);
    }

    @Override
    public UserProfile readUserProfileByExtId(long extUserId) throws DbException {
        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_USER_BY_EXTID,
                Maps.create("extId", extUserId),
                UserProfile.class);
    }

    @Override
    public boolean editUserProfile(long userId, UserProfileEditDto latest) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");

        UserProfile prev = readUserProfile(userId);
        if (prev == null) {
            throw new InputValidationException("No user is found by id " + userId + "!");
        }
        Map<String, Object> data = new HashMap<>();
        data.put("name", Pojos.compareWith(latest.getName(), prev.getName()));
        data.put("avatarId", Pojos.compareWith(latest.getAvatarId(), prev.getAvatarId()));
        data.put("nickname", Pojos.compareWith(latest.getNickName(), prev.getNickName()));
        data.put("isMale", latest.getMale() != null ? latest.getMale() : prev.isMale());
        data.put("userId", userId);

        return dao.executeCommand(Q.PROFILE.EDIT_USER, data) > 0;
    }

    @Override
    public boolean deleteUserProfile(long userId) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");

        return dao.executeCommand(Q.PROFILE.DISABLE_USER, Maps.create("userId", userId)) > 0;
    }

    @Override
    public List<UserProfile> findUser(String email, String name) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(email, "search text");

        if (email.length() < 4) {
            return new LinkedList<>();
        }

        String param = Commons.fixSearchQuery(email);
        String sName = Commons.fixSearchQuery(name);

        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.SEARCH_USER,
                Maps.create()
                    .put("email", "%" + param + "%")
                    .put("name", "%" + name + "%")
                    .build(),
                UserProfile.class,
                Maps.create("hasName", sName != null && sName.length() > 3)
        ));
    }

    @Override
    public List<UserProfile> listUsers(long teamId, long offset, long size) throws DbException, InputValidationException {
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
    public long addTeam(TeamProfile teamProfile) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(teamProfile.getName(), "name");
        Checks.nonNull(teamProfile.getTeamScope(), "scope");
        Checks.greaterThanZero(teamProfile.getTeamScope(), "scope");

        return (Long) dao.runTx(Connection.TRANSACTION_READ_COMMITTED, input -> {
            long currTime = System.currentTimeMillis();

            Map<String, Object> data = Maps.create()
                    .put("teamScope", teamProfile.getTeamScope())
                    .put("name", teamProfile.getName())
                    .put("avatarId", teamProfile.getAvatarId())
                    .put("isAutoTeam", teamProfile.isAutoTeam())
                    .build();

            Long teamId = input.executeInsert(Q.PROFILE.ADD_TEAM, data, "team_id");

            // add user for team scope
            Map<String, Object> templating = Maps.create("isActivated", true);
            Map<String, Object> playerData = Maps.create()
                    .put("name", teamProfile.getName())
                    .put("nickname", teamProfile.getName())
                    .put("male", false)
                    .put("avatarId", null)
                    .put("extId", null)
                    .put("email", DefaultEntities.deriveDefTeamUser(SLUGIFY.slugify(teamProfile.getName())))
                    .put("isAutoUser", true)
                    .put("activated", true)
                    .build();
            Long userId = input.executeInsert(Q.PROFILE.ADD_USER, playerData, templating,"user_id");

            input.executeInsert(Q.PROFILE.ADD_USER_TO_TEAM,
                    Maps.create()
                            .put("userId", userId)
                            .put("teamId", teamId)
                            .put("roleId", UserRole.PLAYER)
                            .put("since", currTime)
                            .put("isApproved", true)
                            .put("approvedAt", currTime)
                            .build(),
                    Maps.create("hasApproved", true),
                    null);
            return teamId;
        });
    }

    @Override
    public TeamProfile readTeam(long teamId) throws DbException, InputValidationException {
        Checks.greaterThanZero(teamId, "teamId");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_TEAM,
                Maps.create("teamId", teamId),
                TeamProfile.class);
    }

    @Override
    public boolean editTeam(long teamId, TeamProfileEditDto latest) throws DbException, InputValidationException {
        Checks.greaterThanZero(teamId, "teamId");

        TeamProfile curr = readTeam(teamId);
        if (curr == null) {
            throw new InputValidationException("No team is found by id " + teamId + "!");
        }

        String nameNew = Pojos.compareWith(latest.getName(), curr.getName());
        Maps.MapBuilder dataMap = Maps.create()
                .put("avatarId", Pojos.compareWith(latest.getAvatarId(), curr.getAvatarId()))
                .put("teamId", teamId)
                .put("name", nameNew);

        if (curr.isAutoTeam() && !StringUtils.equals(nameNew, curr.getName())) {
            throw new InputValidationException("Not allowed to modify default team name!");
        }

        return dao.executeCommand(Q.PROFILE.EDIT_TEAM, dataMap.build()) > 0;
    }

    @Override
    public List<TeamProfile> listTeams(long scopeId) throws DbException, InputValidationException {
        Checks.greaterThanZero(scopeId, "scopeId");

        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.LIST_TEAMS_OF_SCOPE,
                Maps.create("scopeId", scopeId),
                TeamProfile.class));
    }

    @Override
    public long addTeamScope(TeamScope teamScope) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(teamScope.getName(), "name");
        Checks.nonNullOrEmpty(teamScope.getDisplayName(), "displayName");

        return (Long) dao.runTx(Connection.TRANSACTION_READ_COMMITTED, input -> {
            long currTime = System.currentTimeMillis();

            Map<String, Object> data = Maps.create()
                    .put("extId", teamScope.getExtId())
                    .put("name", teamScope.getName())
                    .put("displayName", teamScope.getDisplayName())
                    .put("isAutoScope", teamScope.isAutoScope())
                    .build();

            Long addedScopeId = input.executeInsert(Q.PROFILE.ADD_TEAMSCOPE, data, "scope_id");

            // add default team
            Map<String, Object> teamData = Maps.create()
                    .put("teamScope", addedScopeId)
                    .put("name", DefaultEntities.deriveDefaultTeamName(SLUGIFY.slugify(teamScope.getName())))
                    .put("avatarId", null)
                    .put("isAutoTeam", true)
                    .build();
            Long addedTeamId = input.executeInsert(Q.PROFILE.ADD_TEAM, teamData, "team_id");

            // add user for team scope
            Map<String, Object> templating = Maps.create("isActivated", true);
            Map<String, Object> playerData = Maps.create()
                    .put("name", teamScope.getName())
                    .put("nickname", teamScope.getName())
                    .put("male", false)
                    .put("avatarId", null)
                    .put("extId", null)
                    .put("email", DefaultEntities.deriveDefScopeUser(SLUGIFY.slugify(teamScope.getName())))
                    .put("isAutoUser", true)
                    .put("activated", true)
                    .build();
            Long userId = input.executeInsert(Q.PROFILE.ADD_USER, playerData, templating,"user_id");

            input.executeInsert(Q.PROFILE.ADD_USER_TO_TEAM,
                    Maps.create()
                            .put("userId", userId)
                            .put("teamId", addedTeamId)
                            .put("roleId", UserRole.PLAYER)
                            .put("since", currTime)
                            .put("isApproved", teamScope.isAutoScope())
                            .put("approvedAt", currTime)
                            .build(),
                    Maps.create("hasApproved", true),
                    null);
            return addedScopeId;
        });
    }

    @Override
    public TeamScope readTeamScope(long scopeId) throws DbException, InputValidationException {
        Checks.greaterThanZero(scopeId, "scopeId");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.READ_TEAMSCOPE,
                Maps.create("scopeId", scopeId),
                TeamScope.class);
    }

    @Override
    public TeamScope readTeamScope(String scopeName) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(scopeName, "scopeName");

        return ServiceUtils.getTheOnlyRecord(dao,Q.PROFILE.FIND_SCOPE_BY_NAME,
                Maps.create("scopeName", scopeName),
                TeamScope.class);
    }

    @Override
    public List<TeamScope> listTeamScopes() throws DbException {
        return ServiceUtils.toList(dao.executeQuery(Q.PROFILE.LIST_TEAM_SCOPES,
                null, TeamScope.class));
    }

    @Override
    public boolean editTeamScope(long scopeId, TeamScopeEditDto latest) throws DbException, InputValidationException {
        Checks.greaterThanZero(scopeId, "scopeId");

        TeamScope prev = readTeamScope(scopeId);
        if (prev == null) {
            throw new InputValidationException("No scope is found by id " + scopeId + "!");
        }

        Map<String, Object> data = Maps.create()
                .put("displayName", Pojos.compareWith(latest.getDisplayName(), prev.getDisplayName()))
                .put("scopeId", scopeId)
                .build();

        return dao.executeCommand(Q.PROFILE.EDIT_TEAMSCOPE, data) > 0;
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId, int roleId) throws DbException, InputValidationException {
        return addUserToTeam(userId, teamId, roleId, false);
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval) throws DbException, InputValidationException {
        return addUserToTeam(userId, teamId, roleId, pendingApproval, System.currentTimeMillis());
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId, int roleId, boolean pendingApproval,
                                 long since) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");
        Checks.greaterThanZero(teamId, "teamId");
        Checks.validate(roleId > 0 && roleId <= UserRole.ALL_ROLE, "roleId must be a flag of 1,2,4, or 8.");

        UserTeam currentTeam = findCurrentTeamOfUser(userId, false);
        if (currentTeam != null && currentTeam.getTeamId() == teamId) {
            // if the current team is same as previous team, then don't add
            if (roleId == currentTeam.getRoleId()) {
                return false;
            }
        }

        // if the previous team is not yet approved, then disable it
        if (currentTeam != null && !currentTeam.isApproved()) {
            dao.executeCommand(Q.PROFILE.REJECT_USER_IN_TEAM,
                    Maps.create("id", currentTeam.getId()));
        }

        return (Boolean) dao.runTx(Connection.TRANSACTION_READ_COMMITTED, ctx -> {
            if (currentTeam != null) {
                ctx.executeCommand(Q.PROFILE.DEALLOCATE_FROM_TEAM,
                        Maps.create()
                                .put("id", currentTeam.getId())
                                .put("endTime", since)
                                .build());
            }

            return ctx.executeCommand(Q.PROFILE.ADD_USER_TO_TEAM,
                    Maps.create()
                            .put("userId", userId)
                            .put("teamId", teamId)
                            .put("roleId", roleId)
                            .put("since", since)
                            .put("isApproved", !pendingApproval)
                            .put("approvedAt", pendingApproval ? null : since)
                            .build(),
                    Maps.create("hasApproved", true)) > 0;
        });
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId) throws DbException, InputValidationException {
        return findCurrentTeamOfUser(userId, true);
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly) throws DbException, InputValidationException {
        return findCurrentTeamOfUser(userId, returnApprovedOnly, System.currentTimeMillis());
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId, boolean returnApprovedOnly, long atTime) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");

        // @TODO handle when no record is found
        Iterable<UserTeam> userTeams = dao.executeQuery(Q.PROFILE.FIND_CURRENT_TEAM_OF_USER,
                Maps.create("userId", userId, "currentEpoch", atTime),
                UserTeam.class,
                Maps.create("checkApproved", returnApprovedOnly));
        Iterator<UserTeam> iterator = userTeams.iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return null;
    }

    @Override
    public TeamProfile findTeamByName(String name) throws DbException, InputValidationException {
        Checks.nonNullOrEmpty(name, "teamName");

        return ServiceUtils.getTheOnlyRecord(dao, Q.PROFILE.FIND_TEAM_BY_NAME,
                Maps.create("teamName", name.toLowerCase()),
                TeamProfile.class);
    }

    @Override
    public boolean logoutUser(long userId, long ts) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");

        return dao.executeCommand(Q.PROFILE.LOGOUT_USER,
                Maps.create("userId", userId, "logoutAt", ts)) > 0;
    }

    @Override
    public long requestForRole(long byUser, int teamScopeId, int roleId, long startTime) throws DbException, InputValidationException {
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
    public boolean rejectRequestedRole(int requestId, long rejectedBy) throws DbException, InputValidationException {
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
    public boolean removeCurrentRole(long userId, int teamScopeId, long endTime, long removedBy) throws DbException, InputValidationException {
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
    public boolean approveRole(int requestId, long approvedTime, long approvedBy) throws DbException, InputValidationException {
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
    public List<UserTeamScope> listCurrentUserRoles(long userId) throws DbException, InputValidationException {
        Checks.greaterThanZero(userId, "userId");

        return ServiceUtils.toList(dao.executeQuery("profile/flow/listCurrentUserRoles",
                Maps.create("userId", userId),
                UserTeamScope.class));
    }
}

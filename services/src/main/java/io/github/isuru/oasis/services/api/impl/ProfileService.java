package io.github.isuru.oasis.services.api.impl;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.IProfileService;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.Maps;
import io.github.isuru.oasis.services.utils.Pojos;
import io.github.isuru.oasis.services.utils.UserRole;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class ProfileService extends BaseService implements IProfileService {

    ProfileService(IOasisDao dao, IOasisApiService apiService) {
        super(dao, apiService);
    }

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
                .build();

        return getDao().executeInsert("profile/addUser", data, "user_id");
    }

    @Override
    public UserProfile readUserProfile(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        return getTheOnlyRecord("profile/readUser",
                Maps.create("userId", userId),
                UserProfile.class);
    }

    @Override
    public UserProfile readUserProfile(String email) throws Exception {
        Checks.nonNullOrEmpty(email, "email");

        return getTheOnlyRecord("profile/readUserByEmail",
                Maps.create("email", email),
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
        Checks.greaterThanZero(userId, "userId");

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
        Checks.greaterThanZero(userId, "userId");

        return getDao().executeCommand("profile/disableUser", Maps.create("userId", userId)) > 0;
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

        return toList(getDao().executeQuery("profile/searchUser",
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

        return toList(getDao().executeQuery("profile/listUsersOfTeam",
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

        Map<String, Object> data = Maps.create()
                .put("teamScope", teamProfile.getTeamScope())
                .put("name", teamProfile.getName())
                .put("avatarId", teamProfile.getAvatarId())
                .build();

        return getDao().executeInsert("profile/addTeam", data, "team_id");
    }

    @Override
    public TeamProfile readTeam(long teamId) throws Exception {
        Checks.greaterThanZero(teamId, "teamId");

        return getTheOnlyRecord("profile/readTeam",
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

        return getDao().executeCommand("profile/editTeam", data) > 0;
    }

    @Override
    public List<TeamProfile> listTeams(long scopeId) throws Exception {
        Checks.greaterThanZero(scopeId, "scopeId");

        return toList(getDao().executeQuery("profile/listTeamOfScope",
                Maps.create("scopeId", scopeId),
                TeamProfile.class));
    }

    @Override
    public long addTeamScope(TeamScope teamScope) throws Exception {
        Checks.nonNullOrEmpty(teamScope.getName(), "name");
        Checks.nonNullOrEmpty(teamScope.getDisplayName(), "displayName");

        Map<String, Object> data = Maps.create()
                .put("extId", teamScope.getExtId())
                .put("name", teamScope.getName())
                .put("displayName", teamScope.getDisplayName())
                .build();

        return getDao().executeInsert("profile/addTeamScope", data, "scope_id");
    }

    @Override
    public TeamScope readTeamScope(long scopeId) throws Exception {
        Checks.greaterThanZero(scopeId, "scopeId");

        return getTheOnlyRecord("profile/readTeamScope",
                Maps.create("scopeId", scopeId),
                TeamScope.class);
    }

    @Override
    public List<TeamScope> listTeamScopes() throws Exception {
        return toList(getDao().executeQuery("profile/listTeamScopes",
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

        return getDao().executeCommand("profile/editTeamScope", data) > 0;
    }

    @Override
    public boolean addUserToTeam(long userId, long teamId, int roleId) throws Exception {
        Checks.greaterThanZero(userId, "userId");
        Checks.greaterThanZero(teamId, "teamId");
        Checks.validate(roleId > 0 && roleId <= UserRole.ALL_ROLE, "roleId must be a flag of 1,2,4, or 8.");

        // if the current team is same as previous team, then don't add
        UserTeam userTeam = findCurrentTeamOfUser(userId);
        if (userTeam != null && userTeam.getTeamId() == teamId) {
            if (roleId == userTeam.getRoleId()) {
                return false;
            }
        }

        return getDao().executeInsert("profile/addUserToTeam",
                Maps.create()
                        .put("userId", userId)
                        .put("teamId", teamId)
                        .put("roleId", roleId)
                        .put("since", System.currentTimeMillis())
                        .build(),
                null) > 0;
    }

    @Override
    public UserTeam findCurrentTeamOfUser(long userId) throws Exception {
        Checks.greaterThanZero(userId, "userId");

        long l = System.currentTimeMillis();
        return getTheOnlyRecord("profile/findCurrentTeamOfUser",
                Maps.create().put("userId", userId)
                    .put("currentEpoch", l)
                    .build(),
                UserTeam.class);
    }

}

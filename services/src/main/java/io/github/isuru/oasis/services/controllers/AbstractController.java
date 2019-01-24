package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.UserRole;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IProfileService;
import io.github.isuru.oasis.services.utils.Commons;

abstract class AbstractController {

    private boolean isCurator(UserPrincipal authUser) {
        return hasRole(authUser, UserRole.ROLE_CURATOR);
    }

    private boolean hasRole(UserPrincipal authUser, String role) {
        if (authUser == null) {
            return false;
        }

        // no roles expecting for a user
        if (role == null) {
            return Commons.isNullOrEmpty(authUser.getAuthorities());
        } else {
            return authUser.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals(role));
        }
    }

    private boolean teamNotInScope(IProfileService profileService, long scopeId, long teamId) throws Exception {
        TeamProfile goingToAdd = profileService.readTeam(teamId);
        return !goingToAdd.getTeamScope().equals(scopeId);
    }

    boolean curatorNotOwnsTeam(IProfileService profileService, UserPrincipal authUser, long teamId) throws Exception {
        if (isCurator(authUser)) {
            return teamNotInScope(profileService, authUser.getTeam().getScopeId(), teamId);
        } else {
            return false;
        }
    }

}

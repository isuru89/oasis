package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import io.github.isuru.oasis.services.dto.DeleteResponse;
import io.github.isuru.oasis.services.dto.EditResponse;
import io.github.isuru.oasis.services.dto.crud.*;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.security.CurrentUser;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SuppressWarnings("unused")
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IProfileService profileService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/user/add")
    public DefinitionAddResponse addUser(@RequestBody UserProfileAddDto profile) throws Exception {
        return new DefinitionAddResponse("user", profileService.addUserProfile(profile));
    }

    @PreAuthorize("isAuthenticated() and #userId == #authUser.id")
    @PostMapping("/user/{id}/edit")
    public EditResponse editUser(@PathVariable("id") long userId,
                                 @RequestBody UserProfileEditDto profileEditDto,
                                 @CurrentUser UserPrincipal authUser) throws Exception {
        return new EditResponse("user", profileService.editUserProfile(userId, profileEditDto));
    }

    @GetMapping("/user/{id}")
    public UserProfile readUser(@PathVariable("id") long userId) throws Exception {
        return profileService.readUserProfile(userId);
    }

    @GetMapping("/user/ext/{id}")
    public UserProfile readUserByExternalId(@PathVariable("id") long externalId) throws Exception {
        return profileService.readUserProfileByExtId(externalId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/user/{id}")
    public DeleteResponse deleteUser(@PathVariable("id") long userId) throws Exception {
        return new DeleteResponse("user", profileService.deleteUserProfile(userId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/team/add")
    public DefinitionAddResponse addTeam(@RequestBody TeamProfileAddDto teamProfile) throws Exception {
        return new DefinitionAddResponse("team", profileService.addTeam(teamProfile));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CURATOR')")
    @PostMapping("/team/{id}/edit")
    public EditResponse editTeam(@PathVariable("id") int teamId,
                                 @RequestBody TeamProfileEditDto editDto) throws Exception {
        return new EditResponse("team", profileService.editTeam(teamId, editDto));
    }

    @GetMapping("/team/{id}")
    public TeamProfile readTeam(@PathVariable("id") int teamId) throws Exception {
        return profileService.readTeam(teamId);
    }

    @PostMapping("/team/{id}/users")
    public List<UserProfile> getUsersOfTeam(@PathVariable("id") int teamId,
                                            @RequestParam(value = "offset", defaultValue = "0") long offset,
                                            @RequestParam(value = "size", defaultValue = "50") long size) throws Exception {
        return profileService.listUsers(teamId, offset, size);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/scope/add")
    public DefinitionAddResponse addTeamScope(@RequestBody TeamScopeAddDto scope) throws Exception {
        return new DefinitionAddResponse("scope", profileService.addTeamScope(scope));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/scope/{id}/edit")
    public EditResponse editTeamScope(@PathVariable("id") int scopeId,
                                      @RequestBody TeamScopeEditDto editDto) throws Exception {
        return new EditResponse("scope", profileService.editTeamScope(scopeId, editDto));
    }

    @GetMapping("/scope/list")
    public List<TeamScope> readAllTeamScopes() throws Exception {
        return profileService.listTeamScopes();
    }

    @GetMapping("/scope/{id}")
    public TeamScope readTeamScope(@PathVariable("id") int scopeId) throws Exception {
        return profileService.readTeamScope(scopeId);
    }

    @PostMapping("/scope/{id}/teams")
    public List<TeamProfile> readTeamsInTeamScope(@PathVariable("id") int scopeId) throws Exception {
        return profileService.listTeams(scopeId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CURATOR')")
    @PostMapping("/user/add-to-team")
    public void addUserToTeam(@RequestBody UserTeam userTeam) throws Exception {
        profileService.addUserToTeam(userTeam.getUserId(), userTeam.getTeamId(), userTeam.getRoleId());
    }

    @PostMapping("/user/{id}/current-team")
    public UserTeam findTeamOfUser(@PathVariable("id") long userId) throws Exception {
        return profileService.findCurrentTeamOfUser(userId);
    }

}

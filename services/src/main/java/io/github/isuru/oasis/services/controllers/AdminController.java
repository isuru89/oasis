package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.dto.DefinitionAddResponse;
import io.github.isuru.oasis.services.dto.DeleteResponse;
import io.github.isuru.oasis.services.dto.EditResponse;
import io.github.isuru.oasis.services.model.TeamProfile;
import io.github.isuru.oasis.services.model.TeamScope;
import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.security.CurrentUser;
import io.github.isuru.oasis.services.security.UserPrincipal;
import io.github.isuru.oasis.services.services.IProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@SuppressWarnings("unused")
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private IProfileService profileService;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/user/add")
    @ResponseBody
    public DefinitionAddResponse addUser(@RequestBody UserProfile profile) throws Exception {
        return new DefinitionAddResponse("user", profileService.addUserProfile(profile));
    }

    @PreAuthorize("isAuthenticated() and #userId == #authUser.id")
    @PostMapping("/user/{id}/edit")
    @ResponseBody
    public EditResponse editUser(@PathVariable("id") long userId,
                                 @RequestBody UserProfile profile,
                                 @CurrentUser UserPrincipal authUser) throws Exception {
        return new EditResponse("user", profileService.editUserProfile(userId, profile));
    }

    @GetMapping("/user/{id}")
    @ResponseBody
    public UserProfile readUser(@PathVariable("id") long userId) throws Exception {
        return profileService.readUserProfile(userId);
    }

    @GetMapping("/user/ext/{id}")
    @ResponseBody
    public UserProfile readUserByExternalId(@PathVariable("id") long externalId) throws Exception {
        return profileService.readUserProfileByExtId(externalId);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/user/{id}")
    @ResponseBody
    public DeleteResponse deleteUser(@PathVariable("id") long userId) throws Exception {
        return new DeleteResponse("user", profileService.deleteUserProfile(userId));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/team/add")
    @ResponseBody
    public DefinitionAddResponse addTeam(@RequestBody TeamProfile teamProfile) throws Exception {
        return new DefinitionAddResponse("team", profileService.addTeam(teamProfile));
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CURATOR')")
    @PostMapping("/team/{id}/edit")
    @ResponseBody
    public EditResponse editTeam(@PathVariable("id") int teamId,
                                 @RequestBody TeamProfile profile) throws Exception {
        return new EditResponse("team", profileService.editTeam(teamId, profile));
    }

    @GetMapping("/team/{id}")
    @ResponseBody
    public TeamProfile readTeam(@PathVariable("id") int teamId) throws Exception {
        return profileService.readTeam(teamId);
    }

    @PostMapping("/team/{id}/users")
    @ResponseBody
    public List<UserProfile> getUsersOfTeam(@PathVariable("id") int teamId,
                                            @RequestParam(value = "offset", defaultValue = "0") long offset,
                                            @RequestParam(value = "size", defaultValue = "50") long size) throws Exception {
        return profileService.listUsers(teamId, offset, size);
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/scope/add")
    @ResponseBody
    public DefinitionAddResponse addTeamScope(@RequestBody TeamScope scope) throws Exception {
        return new DefinitionAddResponse("scope", profileService.addTeamScope(scope));
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/scope/{id}/edit")
    @ResponseBody
    public EditResponse editTeamScope(@PathVariable("id") int scopeId,
                                      @RequestBody TeamScope scope) throws Exception {
        return new EditResponse("scope", profileService.editTeamScope(scopeId, scope));
    }

    @GetMapping("/scope/list")
    @ResponseBody
    public List<TeamScope> readAllTeamScopes() throws Exception {
        return profileService.listTeamScopes();
    }

    @GetMapping("/scope/{id}")
    @ResponseBody
    public TeamScope readTeamScope(@PathVariable("id") int scopeId) throws Exception {
        return profileService.readTeamScope(scopeId);
    }

    @PostMapping("/scope/{id}/teams")
    @ResponseBody
    public List<TeamProfile> readTeamsInTeamScope(@PathVariable("id") int scopeId) throws Exception {
        return profileService.listTeams(scopeId);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_CURATOR')")
    @PostMapping("/user/add-to-team")
    @ResponseBody
    public void addUserToTeam(@RequestBody UserTeam userTeam) throws Exception {
        profileService.addUserToTeam(userTeam.getUserId(), userTeam.getTeamId(), userTeam.getRoleId());
    }

    @PostMapping("/user/{id}/current-team")
    @ResponseBody
    public UserTeam findTeamOfUser(@PathVariable("id") long userId) throws Exception {
        return profileService.findCurrentTeamOfUser(userId);
    }

}

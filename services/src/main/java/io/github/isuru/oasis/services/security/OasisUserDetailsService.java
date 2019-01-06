package io.github.isuru.oasis.services.security;

import io.github.isuru.oasis.services.model.UserProfile;
import io.github.isuru.oasis.services.model.UserTeam;
import io.github.isuru.oasis.services.services.IProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class OasisUserDetailsService implements UserDetailsService {

    @Autowired
    private IProfileService profileService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        try {
            UserProfile userProfile = profileService.readUserProfile(email);
            UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userProfile.getId());

            return UserPrincipal.create(userProfile, currentTeamOfUser);

        } catch (Exception e) {
            throw new UsernameNotFoundException("No user is found by email '" + email + "'!", e);
        }
    }

    public UserDetails loadUserById(Long id) {
        try {
            UserProfile userProfile = profileService.readUserProfile(id);
            UserTeam currentTeamOfUser = profileService.findCurrentTeamOfUser(userProfile.getId());

            return UserPrincipal.create(userProfile, currentTeamOfUser);

        } catch (Exception e) {
            throw new UsernameNotFoundException("User not found with id: " + id);
        }
    }
}

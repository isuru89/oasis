/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.security;

import io.github.oasis.services.model.UserProfile;
import io.github.oasis.services.model.UserRole;
import io.github.oasis.services.model.UserTeam;
import io.github.oasis.services.services.IProfileService;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class OasisUserDetailsServiceTest {

    @Test
    public void testLoadUserByUsername() throws Exception {
        IProfileService profileService = Mockito.mock(IProfileService.class);

        UserProfile userIsuru = createUser(1, "isuru@domain.com", "Isuru Weerarathna");
        UserTeam teamIsuru = createTeam(101, userIsuru, UserRole.ADMIN);
        UserProfile userJon = createUser(2, "jon@domain.com", "Jon Doe");
        UserTeam teamJon = createTeam(102, userJon, UserRole.PLAYER);
        UserProfile userZoe = createUser(3, "zoe@domain.com", "Zoe Nightshade");
        UserTeam teamZoe = createTeam(103, userZoe, UserRole.CURATOR);

        BDDMockito.given(profileService.readUserProfile(userIsuru.getId())).willReturn(userIsuru);
        BDDMockito.given(profileService.readUserProfile(userIsuru.getEmail())).willReturn(userIsuru);
        BDDMockito.given(profileService.readUserProfile(userJon.getId())).willReturn(userJon);
        BDDMockito.given(profileService.readUserProfile(userJon.getEmail())).willReturn(userJon);
        BDDMockito.given(profileService.readUserProfile(userZoe.getId())).willReturn(userZoe);
        BDDMockito.given(profileService.readUserProfile(userZoe.getEmail())).willReturn(userZoe);

        BDDMockito.given(profileService.findCurrentTeamOfUser(userIsuru.getId())).willReturn(teamIsuru);
        BDDMockito.given(profileService.findCurrentTeamOfUser(userJon.getId())).willReturn(teamJon);
        BDDMockito.given(profileService.findCurrentTeamOfUser(userZoe.getId())).willReturn(teamZoe);

        OasisUserDetailsService service = new OasisUserDetailsService(profileService);
        {
            UserDetails u = service.loadUserById(1L);
            Assertions.assertNotNull(u);
            Assertions.assertEquals("Isuru Weerarathna", u.getUsername());
            Assertions.assertTrue(u.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN)));
            Assertions.assertTrue(u.isEnabled());
        }
        {
            UserDetails u = service.loadUserById(2L);
            Assertions.assertNotNull(u);
            Assertions.assertEquals("Jon Doe", u.getUsername());
            Assertions.assertTrue(u.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER)));
            Assertions.assertTrue(u.isEnabled());
        }
        {
            UserDetails u = service.loadUserById(3L);
            Assertions.assertNotNull(u);
            Assertions.assertEquals("Zoe Nightshade", u.getUsername());
            Assertions.assertTrue(u.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_CURATOR)));
            Assertions.assertTrue(u.isEnabled());
        }

        // non existing user
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserById(13L));
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserById(-13L));

        // by name
        {
            UserDetails u = service.loadUserByUsername("isuru@domain.com");
            Assertions.assertNotNull(u);
            Assertions.assertEquals("Isuru Weerarathna", u.getUsername());
            Assertions.assertTrue(u.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN)));
            Assertions.assertTrue(u.isEnabled());

            UserDetails u2 = service.loadUserById(1L);
            Assertions.assertEquals(u, u2);
        }
        {
            UserDetails u = service.loadUserByUsername("jon@domain.com");
            Assertions.assertNotNull(u);
            Assertions.assertEquals("Jon Doe", u.getUsername());
            Assertions.assertTrue(u.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER)));
            Assertions.assertTrue(u.isEnabled());
        }
        {
            UserDetails u = service.loadUserByUsername("zoe@domain.com");
            Assertions.assertNotNull(u);
            Assertions.assertEquals("Zoe Nightshade", u.getUsername());
            Assertions.assertTrue(u.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_CURATOR)));
            Assertions.assertTrue(u.isEnabled());
        }

        // non existing user by email
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("nonexist@domain.com"));
        Assertions.assertThrows(UsernameNotFoundException.class, () -> service.loadUserByUsername("xyz"));

    }

    private UserTeam createTeam(int id, UserProfile profile, int role) {
        UserTeam team = new UserTeam();
        team.setRoleId(role);
        team.setTeamId(id);
        team.setUserId(profile.getId());
        return team;
    }

    private UserProfile createUser(long id, String email, String name) {
        UserProfile profile = new UserProfile();
        profile.setId(id);
        profile.setEmail(email);
        profile.setName(name);
        profile.setNickName(name);
        profile.setActive(true);
        return profile;
    }

}

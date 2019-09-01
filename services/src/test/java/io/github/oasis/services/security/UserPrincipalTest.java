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
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.LinkedList;
import java.util.List;

public class UserPrincipalTest {

    @Test
    public void testUserPrincipalEquality() {
        List<GrantedAuthority> playerAuthorities = new LinkedList<>();
        playerAuthorities.add(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER));

        List<GrantedAuthority> adminAuthorities = new LinkedList<>();
        adminAuthorities.add(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN));

        {
            UserPrincipal up1 = new UserPrincipal(
                    101L,
                    "Isuru",
                    "isuruwee",
                    "isuru@domain.com",
                    "mypassword",
                    UserRole.PLAYER,
                    true,
                    playerAuthorities
            );
            UserPrincipal up2 = new UserPrincipal(
                    101L,
                    "Isuru",
                    "isuruwee",
                    "isuru@domain.com",
                    "mypassword",
                    UserRole.PLAYER,
                    true,
                    adminAuthorities
            );
            Assertions.assertEquals(up1, up2);
            Assertions.assertEquals(up1.hashCode(), up2.hashCode());
        }
    }

    @Test
    public void testUserPrincipal() {
        List<GrantedAuthority> authorities = new LinkedList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PLAYER"));

        {
            UserPrincipal up = new UserPrincipal(
                    101L,
                    "Isuru",
                    "isuruwee",
                    "isuru@domain.com",
                    "mypassword",
                    UserRole.PLAYER,
                    true,
                    authorities
            );
            Assertions.assertEquals(101L, (long) up.getId());
            Assertions.assertEquals("Isuru", up.getName());
            Assertions.assertEquals("isuruwee", up.getUsername());
            Assertions.assertEquals("mypassword", up.getPassword());
            Assertions.assertEquals("isuru@domain.com", up.getEmail());
            Assertions.assertEquals(UserRole.PLAYER, up.getRole());
            Assertions.assertNotNull(up.getAuthorities());
            Assertions.assertEquals(1, up.getAuthorities().size());
            Assertions.assertTrue(up.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER)));
            Assertions.assertTrue(up.isAccountNonExpired());
            Assertions.assertTrue(up.isAccountNonLocked());
            Assertions.assertTrue(up.isCredentialsNonExpired());
            Assertions.assertTrue(up.isEnabled());
        }

        {
            UserProfile profile = new UserProfile();
            profile.setName("jon");
            profile.setEmail("jon@company.com");
            profile.setMale(true);
            profile.setActivated(true);
            profile.setAvatarId("/assets/imgs/p/jon.jpg");
            profile.setActive(false);
            profile.setExtId(100000001L);
            profile.setId(102L);
            profile.setAutoUser(false);
            profile.setNickName("Jon Doe");
            profile.setPassword("whoami");

            UserTeam team = new UserTeam();
            team.setUserId(profile.getId());
            team.setTeamId(500);
            team.setRoleId(UserRole.CURATOR);
            team.setApproved(true);
            team.setId(10001L);
            UserPrincipal up = UserPrincipal.create(profile, team);

            Assertions.assertEquals(102L, (long) up.getId());
            Assertions.assertEquals("jon", up.getName());
            Assertions.assertEquals("Jon Doe", up.getUsername());
            Assertions.assertEquals("whoami", up.getPassword());
            Assertions.assertEquals("jon@company.com", up.getEmail());
            Assertions.assertEquals(UserRole.CURATOR, up.getRole());
            Assertions.assertNotNull(up.getAuthorities());
            Assertions.assertEquals(2, up.getAuthorities().size());
            Assertions.assertTrue(up.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER)));
            Assertions.assertTrue(up.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_CURATOR)));
            Assertions.assertTrue(up.isAccountNonExpired());
            Assertions.assertTrue(up.isAccountNonLocked());
            Assertions.assertTrue(up.isCredentialsNonExpired());
            Assertions.assertFalse(up.isEnabled());
        }

        {
            UserProfile profile = new UserProfile();
            profile.setName("admin");
            profile.setEmail("admin@company.com");
            profile.setMale(false);
            profile.setActivated(false);
            profile.setAvatarId("/assets/imgs/p/admin.jpg");
            profile.setActive(true);
            profile.setExtId(100000002L);
            profile.setId(103L);
            profile.setAutoUser(false);
            profile.setNickName("Admin User");
            profile.setPassword("iamadmin");

            UserTeam team = new UserTeam();
            team.setUserId(profile.getId());
            team.setTeamId(555);
            team.setRoleId(UserRole.ADMIN);
            team.setApproved(false);
            team.setId(10002L);
            UserPrincipal up = UserPrincipal.create(profile, team);

            Assertions.assertEquals(103L, (long) up.getId());
            Assertions.assertEquals("admin", up.getName());
            Assertions.assertEquals("Admin User", up.getUsername());
            Assertions.assertEquals("iamadmin", up.getPassword());
            Assertions.assertEquals("admin@company.com", up.getEmail());
            Assertions.assertEquals(UserRole.ADMIN, up.getRole());
            Assertions.assertNotNull(up.getAuthorities());
            Assertions.assertEquals(2, up.getAuthorities().size());
            Assertions.assertTrue(up.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_ADMIN)));
            Assertions.assertTrue(up.getAuthorities().contains(new SimpleGrantedAuthority(UserRole.ROLE_PLAYER)));
            Assertions.assertTrue(up.isAccountNonExpired());
            Assertions.assertTrue(up.isAccountNonLocked());
            Assertions.assertTrue(up.isCredentialsNonExpired());
            Assertions.assertTrue(up.isEnabled());
        }
    }

}

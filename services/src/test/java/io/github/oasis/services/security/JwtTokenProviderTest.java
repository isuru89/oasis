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

import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.model.TokenInfo;
import io.github.oasis.services.model.UserRole;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.LinkedList;
import java.util.List;

public class JwtTokenProviderTest {

    private OasisConfigurations configurations;

    @Before
    public void beforeTest() {
        configurations = new OasisConfigurations();
        OasisConfigurations.AuthConfigs authConfigs = new OasisConfigurations.AuthConfigs();
        configurations.setAuth(authConfigs);
        authConfigs.setJwtSecret("thisisasecret");
        authConfigs.setJwtExpirationTime(3600L * 1000 * 24); // 1 day
    }

    @Test
    public void testGenerateToken() {
        JwtTokenProvider provider = new JwtTokenProvider(configurations);

        List<GrantedAuthority> authorities = new LinkedList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PLAYER"));

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

        TestingAuthenticationToken auth = new TestingAuthenticationToken(up, "mypassword");
        long time = System.currentTimeMillis();
        String token = provider.generateToken(auth);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.length() > 0);

        TokenInfo tokenInfo = provider.validateToken(token);
        Assertions.assertNotNull(tokenInfo);
        Assertions.assertEquals(up.getId().longValue(), tokenInfo.getUser());
        Assertions.assertEquals(UserRole.PLAYER, tokenInfo.getRole());
        Assertions.assertTrue(tokenInfo.getIssuedAt() * 1000L > time);

        Assertions.assertNull(provider.validateToken("abcd"));
    }

}

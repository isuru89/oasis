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
import io.github.oasis.services.utils.Commons;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.BeanIds;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component(BeanIds.AUTHENTICATION_MANAGER)
public class OasisAuthManager implements AuthenticationManager {

    private static final Logger LOG = LoggerFactory.getLogger(OasisAuthManager.class);

    private final OasisAuthenticator authenticator;
    private final UserDetailsService userDetailsService;

    @Autowired
    public OasisAuthManager(Map<String, OasisAuthenticator> authenticatorMap,
                            UserDetailsService userDetailsService,
                            OasisConfigurations configurations) {
        this.userDetailsService = userDetailsService;

        String type = Commons.firstNonNull(configurations.getAuth().getType(), "none");
        String key = "auth" + StringUtils.capitalize(type);

        LOG.info("Loading authentication type: {}", type);
        authenticator = authenticatorMap.get(key);
    }

    public OasisAuthenticator get() {
        return authenticator;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = (String) authentication.getPrincipal();
        Object credentials = authentication.getCredentials();
        if (authenticator.authenticate(username, (String) credentials)) {
            // create auth object with user object
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails,
                    null,
                    userDetails.getAuthorities());
        }
        throw new BadCredentialsException("Username or password incorrect!");
    }
}

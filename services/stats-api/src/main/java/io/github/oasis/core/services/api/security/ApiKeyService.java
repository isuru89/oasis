/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.security;

import io.github.oasis.core.services.api.dao.IApiKeyDao;
import io.github.oasis.core.services.api.dao.dto.ApiKeyDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author Isuru Weerarathna
 */
@Service
public class ApiKeyService implements UserDetailsService {

    private static final Logger LOG = LoggerFactory.getLogger(ApiKeyService.class);

    public static final int ROLE_ADMIN_FLAG = 4;
    public static final int ROLE_CURATOR_FLAG = 2;
    public static final int ROLE_PLAYER_FLAG = 1;

    private static final SimpleGrantedAuthority A_ADMIN = new SimpleGrantedAuthority("ROLE_admin");
    private static final SimpleGrantedAuthority A_CURATOR = new SimpleGrantedAuthority("ROLE_curator");
    private static final SimpleGrantedAuthority A_PLAYER = new SimpleGrantedAuthority("ROLE_player");

    private static final List<SimpleGrantedAuthority> ADMIN_AUTHORITIES = Arrays.asList(A_ADMIN, A_CURATOR, A_PLAYER);
    private static final List<SimpleGrantedAuthority> CURATOR_AUTHORITIES = Arrays.asList(A_CURATOR, A_PLAYER);
    private static final List<SimpleGrantedAuthority> PLAYER_AUTHORITIES = Collections.singletonList(A_PLAYER);

    private final IApiKeyDao dao;

    public ApiKeyService(IApiKeyDao dao) {
        this.dao = dao;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOG.info("Authenticating user {}", username);
        ApiKeyDto apiKeyDto = dao.readApiKey(username);
        if (Objects.isNull(apiKeyDto)) {
            LOG.error("No user found by name {}", username);
            throw new UsernameNotFoundException("The api key does not exist!");
        }

        List<SimpleGrantedAuthority> authorityList;
        int roles = apiKeyDto.getRoles();
        if ((roles & ROLE_ADMIN_FLAG) == ROLE_ADMIN_FLAG) {
            authorityList = ADMIN_AUTHORITIES;
        } else if ((roles & ROLE_CURATOR_FLAG) == ROLE_CURATOR_FLAG) {
            authorityList = CURATOR_AUTHORITIES;
        } else if ((roles & ROLE_PLAYER_FLAG) == ROLE_PLAYER_FLAG) {
            authorityList = PLAYER_AUTHORITIES;
        } else {
            authorityList = new ArrayList<>();
        }

        return new User(
                apiKeyDto.getToken(),
                apiKeyDto.getSecretKey(),
                authorityList
                );
    }
}

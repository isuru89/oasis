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

package io.github.oasis.core.services.api.handlers.impl;

import io.github.oasis.core.exception.OasisRuntimeException;
import io.github.oasis.core.services.api.handlers.UserHandlerSupport;
import io.github.oasis.core.services.api.to.UserCreateRequest;
import io.github.oasis.core.utils.Utils;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Connect to keycloak and handle user management related activities.
 *
 * @author Isuru Weerarathna
 */
@Component("KeycloakUserHandler")
public class KeycloakUserHandler implements UserHandlerSupport, DisposableBean {

    private static final Logger LOG = LoggerFactory.getLogger(KeycloakUserHandler.class);

    private static final String ATTR_GENDER = "gender";
    private static final String ATTR_ZONE_INFO = "zoneinfo";
    private static final String ATTR_USER_ID = "userId";


    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;
    @Value("${keycloak.realm}")
    private String realmName;

    @Value("${keycloak.resource}")
    private String clientId;
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;

    @Value("${oasis.keycloak.admin.user}")
    private String adminUsername;
    @Value("${oasis.keycloak.admin.pass}")
    private String adminPassword;

    private Keycloak keycloak;

    private UsersResource usersResource;

    @PostConstruct
    @Profile("!test")
    public void init() {
        keycloak = KeycloakBuilder.builder()
                .serverUrl(authServerUrl)
                .grantType(OAuth2Constants.PASSWORD)
                .realm(realmName)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(adminUsername)
                .password(adminPassword)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build())
                .build();

        keycloak.tokenManager().getAccessToken();
        usersResource = keycloak.realm(realmName).users();
    }

    @Override
    public void createUser(UserCreateRequest request) {
        UserRepresentation user = new UserRepresentation();
        user.setEmail(request.getEmail());
        user.setUsername(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEnabled(true);
        user.setEmailVerified(true);

        Map<String, List<String>> attrs = new HashMap<>();
        attrs.put(ATTR_GENDER, List.of(request.getGender().name()));
        attrs.put(ATTR_ZONE_INFO, List.of(request.getTimeZone()));
        attrs.put(ATTR_USER_ID, List.of(String.valueOf(request.getUserId())));
        user.setAttributes(attrs);

        Response response = usersResource.create(user);

        LOG.info("Add user status: {}", response.getStatus());
        LOG.info("Add user status: {}", response.getEntity());

        String userId = CreatedResponseUtil.getCreatedId(response);
        LOG.info("Created user id {}", userId);

        CredentialRepresentation pwCredentials = new CredentialRepresentation();
        pwCredentials.setTemporary(false);
        pwCredentials.setType(CredentialRepresentation.PASSWORD);
        pwCredentials.setValue(request.getInitialPassword());

        UserResource userResource = usersResource.get(userId);
        userResource.resetPassword(pwCredentials);
    }

    @Override
    public void deleteUser(String email) {
        List<UserRepresentation> list = usersResource.search(email);
        if (Utils.isEmpty(list)) {
            throw new OasisRuntimeException("No user found by email!");
        }

        list.forEach(u -> usersResource.delete(u.getId()));
    }

    @Override
    public void destroy() {
        if (keycloak != null) {
            keycloak.close();
        }
    }
}

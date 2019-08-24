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

package io.github.oasis.services.controllers;

import io.github.oasis.services.DataCache;
import io.github.oasis.services.Utils;
import io.github.oasis.services.dto.AuthResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class TestServer {

    @Autowired
    private WebTestClient webClient;

    @Autowired
    private DataCache dataCache;

    @Autowired
    private TestAuthenticator testAuthenticator;

    @Before
    public void beforeEach() {
        Map<String, String> users = new HashMap<>();
        users.put("isuru@domain.com", "rightpw");
        users.put("admin@oasis.com", "admin");
        users.put("curator@oasis.com", "curator");
        users.put("player@oasis.com", "player");
        testAuthenticator.refill(users);
    }

    @org.junit.Test
    public void testLogout() throws IOException {
        AuthResponse adminAuth = assertAuthResponse(readResponseJson(
                doLogin("Basic " + Utils.toBase64("admin@oasis.com:admin"))
                        .expectStatus().isOk()));

        doLogout(null).expectStatus().is4xxClientError();
        doLogout("").expectStatus().is4xxClientError();
        doLogout("Basic " + Utils.toBase64("admin@oasis.com:admin"))
                .expectStatus().is4xxClientError();

        doLogout("Bearer " + adminAuth.getToken()).expectStatus().isOk();
    }

    @Test
    public void testSuccessfulLogins() throws IOException {
        AuthResponse adminAuth = assertAuthResponse(readResponseJson(
                doLogin("Basic " + Utils.toBase64("admin@oasis.com:admin"))
                        .expectStatus().isOk()));

        {
            AuthResponse auth = assertAuthResponse(readResponseJson(
                    doLogin("Basic " + Utils.toBase64("admin@oasis.com:admin"))
                            .expectStatus().isOk()));
            Assertions.assertEquals("admin@oasis.com", auth.getUserProfile().getEmail());
        }

        {
            AuthResponse auth = assertAuthResponse(readResponseJson(
                    doLogin("Basic " + Utils.toBase64("curator@oasis.com:curator"))
                            .expectStatus().isOk()));
            Assertions.assertEquals("curator@oasis.com", auth.getUserProfile().getEmail());
        }

        {
            AuthResponse auth = assertAuthResponse(readResponseJson(
                    doLogin("Basic " + Utils.toBase64("player@oasis.com:player"))
                            .expectStatus().isOk()));
            Assertions.assertEquals("player@oasis.com", auth.getUserProfile().getEmail());
        }

        {
            // non reserved user success for universal password
            System.out.println(dataCache.getAllUserTmpPassword());
        }
    }

    @Test
    public void testInvalidLogins() {
        // header invalid errors
        doLogin(null).expectStatus().is4xxClientError();
        doLogin("").expectStatus().is4xxClientError();
        doLogin("invalidheadervalue").expectStatus().is4xxClientError();
        doLogin("Basic abc43243dsads").expectStatus().is4xxClientError();
        doLogin("Basic " + Utils.toBase64("isuru")).expectStatus().is4xxClientError();

        // credential invalids
        doLogin("Basic " + Utils.toBase64("isuru@domain.com:wrongpw"))
                .expectStatus().is4xxClientError();
        doLogin("Basic " + Utils.toBase64("admin@oasis.com:wrongadminpw"))
                .expectStatus().is4xxClientError();
        doLogin("Basic " + Utils.toBase64("curator@oasis.com:wrongcuratorpw"))
                .expectStatus().is4xxClientError();
        doLogin("Basic " + Utils.toBase64("player@oasis.com:wrongplayerpw"))
                .expectStatus().is4xxClientError();

        // non existing user
        doLogin("Basic " + Utils.toBase64("unknown@unknown.com:unknownpw"))
                .expectStatus().is4xxClientError();

        // @TODO ldap success, but not in the system
        doLogin("Basic " + Utils.toBase64("isuru@domain.com:rightpw"))
                .expectStatus().is4xxClientError();
    }

    private AuthResponse assertAuthResponse(AuthResponse auth) {
        Assertions.assertNotNull(auth);
        Assertions.assertNotNull(auth.getToken());
        Assertions.assertTrue(auth.getToken().trim().length() > 1);
        Assertions.assertNotNull(auth.getUserProfile());
        Assertions.assertNull(auth.getUserProfile().getPassword()); // password must not exist
        Assertions.assertTrue(auth.isSuccess());
        return auth;
    }

    AuthResponse readResponseJson(WebTestClient.ResponseSpec spec) throws IOException {
        return Utils.fromJson(spec.expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .returnResult()
                .getResponseBody(), AuthResponse.class);
    }

    WebTestClient.ResponseSpec doLogin(String authHeader) {
        if (authHeader == null) {
            return webClient.post().uri("/api/auth/login")
                    .exchange();
        } else {
            return webClient.post().uri("/api/auth/login")
                    .header("Authorization", authHeader)
                    .exchange();
        }
    }

    WebTestClient.ResponseSpec doLogout(String authHeader) {
        if (authHeader == null) {
            return webClient.post().uri("/api/auth/logout")
                    .exchange();
        } else {
            return webClient.post().uri("/api/auth/logout")
                    .header("Authorization", authHeader)
                    .exchange();
        }
    }
}
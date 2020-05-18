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

package io.github.oasis.simulations;

import io.github.oasis.core.external.messages.PersistedDef;

import java.net.Authenticator;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class SimulationWithApi extends Simulation {

    static final int PORT = 8050;

    private HttpClient client = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(20))
            .build();

    @Override
    protected void announceGame(PersistedDef def) throws Exception {
        super.announceGame(def);
    }

    @Override
    protected void announceRule(PersistedDef def) throws Exception {
        super.announceRule(def);
    }

    @Override
    protected void sendEvent(PersistedDef def) throws Exception {
        Map<String, Object> body = Map.of("data", def.getData());
        String msg = gson.toJson(body);
        String signature = signPayload(msg);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(context.getApiUrl() + "/api/event"))
                .timeout(Duration.ofSeconds(2))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + SOURCE_TOKEN + ":" + signature)
                .PUT(HttpRequest.BodyPublishers.ofString(msg))
                .build();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println);

    }

    private String signPayload(String msg) throws Exception {
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        Signature sha1withRSA = Signature.getInstance("SHA1withRSA");
        sha1withRSA.initSign(sourceKeyPair.getPrivate());
        sha1withRSA.update(data);
        return Base64.getEncoder().encodeToString(sha1withRSA.sign());
    }
}

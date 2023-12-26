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

package io.github.oasis.services.events.model;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.CredentialValidationException;
import io.vertx.ext.auth.authentication.Credentials;

/**
 * @author Isuru Weerarathna
 */
public class ApiKeyCredentials implements Credentials {

    public static final String SOURCE_ID = "id";
    public static final String SOURCE_DIGEST = "digest";


    private String sourceId;
    private String sourceDigest;

    public ApiKeyCredentials() {
    }

    public ApiKeyCredentials(String sourceId, String sourceDigest) {
        this.sourceId = sourceId;
        this.sourceDigest = sourceDigest;
    }

    @Override
    public <V> void checkValid(V arg) throws CredentialValidationException {
        if (sourceId == null || sourceId.isEmpty()) {
            throw new CredentialValidationException("source ID cannot be null or empty");
        }
        if (sourceDigest == null) {
            throw new CredentialValidationException("digest cannot be null");
        }
    }

    @Override
    public JsonObject toJson() {
        return new JsonObject()
                .put(SOURCE_ID, getSourceId())
                .put(SOURCE_DIGEST, getSourceDigest());
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getSourceDigest() {
        return sourceDigest;
    }
}

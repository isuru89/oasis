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

package io.github.oasis.services.events.model;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.User;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * @author Isuru Weerarathna
 */
@DataObject
public class EventSource implements User {

    private JsonObject data;

    private PublicKey publicKey;

    public static EventSource create(String id, byte[] key) {
        JsonObject data = new JsonObject()
                .put("id", id)
                .put("publicKey", key);
        try {
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(key));
            return new EventSource(data).setPublicKey(publicKey);
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Invalid key data!", e);
        }
    }

    public EventSource(JsonObject ref) {
        this.data = ref;
        try {
            this.publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(ref.getBinary("publicKey")));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Invalid key data!", e);
        }
    }

    EventSource setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
        return this;
    }

    public JsonObject toJson() {
        return data;
    }

    public boolean verifyEvent(Buffer event, String providedSignatureB64) {
        try {
            Signature verifier = Signature.getInstance("SHA1withRSA");
            verifier.initVerify(publicKey);
            verifier.update(event.getBytes());
            return verifier.verify(Base64.getDecoder().decode(providedSignatureB64));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            return false;
        }
    }

    public int getSourceId() {
        return data.getInteger("id");
    }

    @Override
    public User isAuthorized(String authority, Handler<AsyncResult<Boolean>> resultHandler) {
        return null;
    }

    @Override
    public User clearCache() {
        return null;
    }

    @Override
    public JsonObject principal() {
        return data;
    }

    @Override
    public void setAuthProvider(AuthProvider authProvider) {

    }

    @Override
    public String toString() {
        return "EventSource{" +
                "id=" + data.getValue("id") +
                '}';
    }
}

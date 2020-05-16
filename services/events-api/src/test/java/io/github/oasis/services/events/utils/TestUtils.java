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

package io.github.oasis.services.events.utils;

import io.github.oasis.core.Event;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.Assertions;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.Base64;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class TestUtils {

    public static KeyPair createKeys() throws NoSuchAlgorithmException {
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
        return pairGenerator.generateKeyPair();
    }

    public static String signPayload(JsonArray json, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(privateKey);
            signer.update(json.toBuffer().getBytes());
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Assertions.fail(e);
            return null;
        }
    }

    public static String signPayload(JsonObject json, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(privateKey);
            signer.update(json.toBuffer().getBytes());
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Assertions.fail(e);
            return null;
        }
    }

    public static String signPayload(String text, PrivateKey privateKey) {
        try {
            Signature signer = Signature.getInstance("SHA1withRSA");
            signer.initSign(privateKey);
            signer.update(text.getBytes());
            return Base64.getEncoder().encodeToString(signer.sign());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            Assertions.fail(e);
            return null;
        }
    }

    public static JsonObject aEvent(String email, long ts, String eventType, int value) {
        return new JsonObject().put(Event.USER_NAME, email)
                .put(Event.ID, UUID.randomUUID().toString())
                .put(Event.TIMESTAMP, ts)
                .put(Event.EVENT_TYPE, eventType)
                .put("payload", new JsonObject().put("value", value));
    }

}

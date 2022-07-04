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

package io.github.oasis.services.events.auth;

import io.github.oasis.services.events.model.EventSource;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Isuru Weerarathna
 */
public class PublicKeyCache {

    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();
    private final Map<String, String> keyStringCache = new ConcurrentHashMap<>();

    public Optional<PublicKey> readKey(String token) {
        return Optional.ofNullable(keyCache.get(token));
    }

    public PublicKey createOrLoad(String token, String keyAsBase64String) {
        String currKey = keyStringCache.computeIfAbsent(token, s -> keyAsBase64String);
        if (keyAsBase64String.equals(currKey)) {
            return keyCache.computeIfAbsent(token, s -> this.loadKey(keyAsBase64String));
        } else {
            PublicKey newKey = loadKey(keyAsBase64String);
            keyCache.put(token, newKey);
            return newKey;
        }
    }

    private PublicKey loadKey(String keyAsBase64String) {
        try {
            return KeyFactory.getInstance(EventSource.KEY_ALGORITHM)
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(keyAsBase64String)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Invalid key data!", e);
        }
    }

    public static PublicKeyCache getInstance() {
        return Holder.INSTANCE;
    }

    private PublicKeyCache() {}

    private static class Holder {
        private static final PublicKeyCache INSTANCE = new PublicKeyCache();
    }

}

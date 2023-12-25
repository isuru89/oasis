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

package io.github.oasis.core.services.api.beans;

import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.services.KeyGeneratorSupport;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * @author Isuru Weerarathna
 */
@Component("RSA-KeyGenerator")
public class KeyGeneratorHelper implements KeyGeneratorSupport {

    private static final String RSA = "RSA";

    private KeyFactory keyFactory;
    private KeyPairGenerator keyPairGenerator;

    @PostConstruct
    public void init() throws NoSuchAlgorithmException {
        keyFactory = KeyFactory.getInstance(RSA);
        keyPairGenerator = KeyPairGenerator.getInstance(RSA);
    }

    @Override
    public KeyPair generate(String sourceId) {
        return keyPairGenerator.generateKeyPair();
    }

    @Override
    public PublicKey readPublicKey(byte[] keyData) throws OasisException {
        try {
            return keyFactory.generatePublic(new X509EncodedKeySpec(keyData));
        } catch (InvalidKeySpecException e) {
            throw new OasisException("Unable to decode provided key data to a public key!", e);
        }
    }
}

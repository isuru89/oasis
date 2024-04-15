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

package io.github.oasis.services.events;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

/**
 * @author Isuru Weerarathna
 */
public class SecurityTest {

    @Test
    void signatureVerify() throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidKeySpecException {
        KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
        KeyPair keyPair = pairGenerator.generateKeyPair();

        String msg = "Hello Isuru";
        byte[] data = msg.getBytes(StandardCharsets.UTF_8);
        Signature sha1withRSA = Signature.getInstance("SHA1withRSA");
        sha1withRSA.initSign(keyPair.getPrivate());
        sha1withRSA.update(data);
        byte[] signedBytes = sha1withRSA.sign();
        System.out.println(signedBytes.length);

//        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(new byte[]{}));
//        PrivateKey privateKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(new byte[]{}));
        Signature verifyier = Signature.getInstance("SHA1withRSA");
        verifyier.initVerify(keyPair.getPublic());
        verifyier.update(msg.getBytes());
        System.out.println(verifyier.verify(signedBytes));
    }

}

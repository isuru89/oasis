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

package io.github.oasis.services.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.oasis.model.configs.Configs;
import io.github.oasis.services.exception.ApiAuthException;
import io.github.oasis.services.model.TokenInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDate;
import java.time.ZoneOffset;

/**
 * @author iweerarathna
 */
public final class AuthUtils {

    //private Algorithm algorithm;
    //private JWTVerifier verifier;
    private long expiryDate;

    private final ObjectMapper mapper = new ObjectMapper();

    private Configs configs;

    private AuthUtils() {}


    public void init(Configs configs) throws Exception {
        this.configs = configs;
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        // init jwt configurations
        String publicKeyPath = configs.getStrReq("oasis.public.key");
        String privateKeyPath = configs.getStrReq("oasis.private.key");
        if (!new File(privateKeyPath).exists() || !new File(publicKeyPath).exists()) {
            throw new FileNotFoundException("Required authenticate keys not found in the deployment!");
        }
        byte[] bytesPrivate = Files.readAllBytes(Paths.get(privateKeyPath));
        byte[] bytesPublic = Files.readAllBytes(Paths.get(publicKeyPath));
        PKCS8EncodedKeySpec specPrivate = new PKCS8EncodedKeySpec(bytesPrivate);
        X509EncodedKeySpec specPublic = new X509EncodedKeySpec(bytesPublic);
        RSAPrivateKey rsaPrivate = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(specPrivate);
        RSAPublicKey rsaPublic = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(specPublic);

        //algorithm = Algorithm.RSA256(rsaPublic, rsaPrivate);
        //verifier = JWT.require(algorithm)
        //        .withIssuer(OASIS_ISSUER)
        //        .build();

        expiryDate = LocalDate.of(2030, 12, 31)
                .atStartOfDay()
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    public void ldapAuthUser(String username, String password) throws ApiAuthException {

    }

    public long getExpiryDate() {
        return expiryDate;
    }



    private static String byteArrayToHexString(byte[] b) {
        StringBuilder result = new StringBuilder();
        for (byte aB : b) {
            result.append(Integer.toString((aB & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public String issueToken(TokenInfo tokenInfo) throws ApiAuthException {
//        try {
//            return JWT.create()
//                    .withIssuer(OASIS_ISSUER)
//                    .withExpiresAt(new Date(tokenInfo.getExp()))
//                    .withIssuedAt(new Date(System.currentTimeMillis()))
//                    .withClaim("user", tokenInfo.getUser())
//                    .withClaim("role", tokenInfo.getRole())
//                    .sign(algorithm);
//        } catch (IllegalArgumentException | JWTCreationException e) {
//            e.printStackTrace();
//            throw new ApiAuthException("Unable to create login information for user " + tokenInfo.getUser() + "!");
//        }
        return null;
    }

    public TokenInfo verifyToken(String token) throws ApiAuthException {
//        try {
//            DecodedJWT jwt = verifier.verify(token);
//            TokenInfo tokenInfo = new TokenInfo();
//            tokenInfo.setUser(jwt.getClaim("user").asLong());
//            tokenInfo.setRole(jwt.getClaim("role").asInt());
//            tokenInfo.setIssuedAt(jwt.getIssuedAt().getTime());
//            tokenInfo.setExp(jwt.getExpiresAt().getTime());
//            return tokenInfo;
//
//        } catch (JWTVerificationException e) {
//            throw new ApiAuthException("Provided token is invalid! It is either modified or expired! ["
//                    + e.getClass().getSimpleName() + "]");
//        }
        return null;
    }

    public static AuthUtils get() {
        return Holder.INSTANCE;
    }

    private static class Holder {
        private static final AuthUtils INSTANCE = new AuthUtils();
    }
}
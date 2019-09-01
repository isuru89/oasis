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

package io.github.oasis.services.security;

import io.github.oasis.services.configs.OasisConfigurations;
import io.github.oasis.services.model.TokenInfo;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtTokenProvider {

    private static final Logger LOG = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String OASIS_ISSUER = "oasis";

    private final OasisConfigurations oasisConfigurations;

    @Autowired
    public JwtTokenProvider(OasisConfigurations oasisConfigurations) {
        this.oasisConfigurations = oasisConfigurations;
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        OasisConfigurations.AuthConfigs auth = oasisConfigurations.getAuth();

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + auth.getJwtExpirationTime());

        return Jwts.builder()
                .setIssuer(OASIS_ISSUER)
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("user", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole())
                .signWith(SignatureAlgorithm.HS512, auth.getJwtSecret())
                .compact();
    }

    public TokenInfo validateToken(String authToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(oasisConfigurations.getAuth().getJwtSecret())
                    .parseClaimsJws(authToken)
                    .getBody();

            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setIssuedAt(claims.getIssuedAt().getTime());
            tokenInfo.setUser(Long.parseLong(claims.getSubject()));
            tokenInfo.setRole(claims.get("role", Integer.class));
            return tokenInfo;
        } catch (JwtException ex) {
            LOG.error("Invalid JWT signature! [Reason: " + ex.getMessage() + "]");
        }
        return null;
    }

}

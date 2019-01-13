package io.github.isuru.oasis.services.security;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.model.TokenInfo;
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

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + oasisConfigurations.getAuthJwtExpirationTime());

        return Jwts.builder()
                .setIssuer(OASIS_ISSUER)
                .setSubject(Long.toString(userPrincipal.getId()))
                .setIssuedAt(new Date())
                .setExpiration(expiryDate)
                .claim("user", userPrincipal.getEmail())
                .claim("role", userPrincipal.getRole())
                .signWith(SignatureAlgorithm.HS512, oasisConfigurations.getAuthJwtSecret())
                .compact();
    }

    public TokenInfo validateToken(String authToken) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(oasisConfigurations.getAuthJwtSecret())
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

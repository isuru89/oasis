package io.github.isuru.oasis.services.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.services.exception.ApiAuthException;

import java.util.Date;

/**
 * @author iweerarathna
 */
public final class AuthUtils {

    private static final String OASIS_ISSUER = "oasis";

    private Algorithm algorithm;
    private JWTVerifier verifier;

    private final ObjectMapper mapper = new ObjectMapper();

    private AuthUtils() {}

    public void init() throws Exception {
        mapper.configure(JsonGenerator.Feature.IGNORE_UNKNOWN, true);

        // @TODO init jwt configurations
        algorithm = Algorithm.HMAC256("1234qwer$ddwqwq");
        verifier = JWT.require(algorithm)
                .withIssuer(OASIS_ISSUER)
                .build();
    }

    public String issueToken(TokenInfo tokenInfo) throws ApiAuthException {
        return JWT.create()
                .withIssuer(OASIS_ISSUER)
                .withExpiresAt(new Date(tokenInfo.getExp()))
                .withClaim("user", tokenInfo.user)
                .withClaim("admin", tokenInfo.admin)
                .withClaim("curator", tokenInfo.curator)
                .sign(algorithm);
    }

    public TokenInfo verifyToken(String token) throws ApiAuthException {
        try {
            DecodedJWT jwt = verifier.verify(token);
            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setUser(jwt.getClaim("user").asLong());
            tokenInfo.setAdmin(jwt.getClaim("admin").asBoolean());
            tokenInfo.setCurator(jwt.getClaim("curator").asBoolean());
            return tokenInfo;

        } catch (JWTVerificationException e) {
            throw new ApiAuthException("Provided token is invalid! It is either modified or expired! ["
                    + e.getClass().getSimpleName() + "]");
        }
    }

    public static AuthUtils get() {
        return Holder.INSTANCE;
    }

    public static class TokenInfo {
        private long user;
        private long exp;
        private boolean admin = false;
        private boolean curator = false;

        public long getExp() {
            return exp;
        }

        public void setExp(long exp) {
            this.exp = exp;
        }

        public boolean isCurator() {
            return curator;
        }

        public void setCurator(boolean curator) {
            this.curator = curator;
        }

        public boolean isAdmin() {
            return admin;
        }

        public void setAdmin(boolean admin) {
            this.admin = admin;
        }

        public long getUser() {
            return user;
        }

        public void setUser(long user) {
            this.user = user;
        }
    }

    private static class Holder {
        private static final AuthUtils INSTANCE = new AuthUtils();
    }
}

package io.github.isuru.oasis.services.security;

import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.model.TokenInfo;
import io.github.isuru.oasis.services.utils.UserRole;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.LinkedList;
import java.util.List;

class JwtTokenProviderTest {

    private OasisConfigurations configurations;

    @BeforeEach
    void beforeTest() {
        configurations = new OasisConfigurations();
        OasisConfigurations.AuthConfigs authConfigs = new OasisConfigurations.AuthConfigs();
        configurations.setAuth(authConfigs);
        authConfigs.setJwtSecret("thisisasecret");
        authConfigs.setJwtExpirationTime(3600L * 1000 * 24); // 1 day
    }

    @Test
    void testGenerateToken() {
        JwtTokenProvider provider = new JwtTokenProvider(configurations);

        List<GrantedAuthority> authorities = new LinkedList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_PLAYER"));

        UserPrincipal up = new UserPrincipal(
                101L,
                "Isuru",
                "isuruwee",
                "isuru@domain.com",
                "mypassword",
                UserRole.PLAYER,
                true,
                authorities
        );

        TestingAuthenticationToken auth = new TestingAuthenticationToken(up, "mypassword");
        long time = System.currentTimeMillis();
        String token = provider.generateToken(auth);
        Assertions.assertNotNull(token);
        Assertions.assertTrue(token.length() > 0);

        TokenInfo tokenInfo = provider.validateToken(token);
        Assertions.assertNotNull(tokenInfo);
        Assertions.assertEquals(up.getId().longValue(), tokenInfo.getUser());
        Assertions.assertEquals(UserRole.PLAYER, tokenInfo.getRole());
        Assertions.assertTrue(tokenInfo.getIssuedAt() * 1000L > time);

        Assertions.assertNull(provider.validateToken("abcd"));
    }

}

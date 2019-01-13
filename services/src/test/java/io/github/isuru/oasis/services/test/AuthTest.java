package io.github.isuru.oasis.services.test;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.utils.AuthUtils;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

class AuthTest {

    @Test
    void testHMAC() throws NoSuchAlgorithmException, InvalidKeyException {
        EventSourceToken token = new EventSourceToken();
        token.setSourceName("jira");
        token.setDisplayName("Jira Source");

        Pair<PrivateKey, PublicKey> keyPair = AuthUtils.generateRSAKey(token.getSourceName());
        String hash = AuthUtils.generateHMAC("{ name: 'Isuru', age: 23 }", keyPair.getValue0());
        Assertions.assertNotNull(hash);
        System.out.println(hash);
    }

}

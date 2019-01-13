package io.github.isuru.oasis.services.model;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.utils.SecurityUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

public class EventSourceTokenTest {

    @Test
    public void testToken() throws NoSuchAlgorithmException, IOException {
        EventSourceToken token = new EventSourceToken();
        token.setDisplayName("Stack Overflow");
        token.setSourceName("stack-overflow");
        token.setActive(true);

        Pair<PrivateKey, PublicKey> pair = SecurityUtils.generateRSAKey(token.getSourceName());
        token.setPublicKey(pair.getValue1().getEncoded());
        token.setSecretKey(pair.getValue0().getEncoded());

        PrivateKey decoded = token.getSecretPrivateKey();
        Assert.assertEquals(pair.getValue0(), decoded);
    }

}

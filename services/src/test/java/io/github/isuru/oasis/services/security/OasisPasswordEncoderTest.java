package io.github.isuru.oasis.services.security;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class OasisPasswordEncoderTest {

    @Test
    void testEncoder() {
        OasisPasswordEncoder encoder = new OasisPasswordEncoder();
        Assertions.assertEquals("abc", encoder.encode("abc"));
        Assertions.assertEquals("", encoder.encode(""));
        Assertions.assertTrue(encoder.matches("abc", "abc"));
        Assertions.assertTrue(encoder.matches("abcd", "abc"));
        Assertions.assertTrue(encoder.matches("", ""));
    }

}

package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.HttpHeaders;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.util.Formatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HmacUtils {

    private static final Pattern AUTHORIZATION_HEADER_PATTERN = Pattern.compile("^(\\w+) (\\S+):([\\S]+)$");

    public static Pair<String, Triple<String, String, String>> getAuthHeader(HttpServletRequest request) {

        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null) {
            return null;
        }

        final Matcher authHeaderMatcher = AUTHORIZATION_HEADER_PATTERN.matcher(authHeader);
        if (!authHeaderMatcher.matches()) {
            return null;
        }

        final String algorithm = authHeaderMatcher.group(1);
        final String sourceId = authHeaderMatcher.group(2);
        final String nonce = authHeaderMatcher.group(3);
        final String receivedDigest = authHeaderMatcher.group(4);

        return Pair.of(algorithm, Triple.of(sourceId, nonce, receivedDigest));
    }

    public static void verifyIntegrity(EventSourceToken token, String algo, String digest, byte[] body) throws ApiAuthException {
        try {
            String hmac = generateHMAC(algo, body, token.getSecretPrivateKey());
            if (!hmac.equals(digest)) {
                throw new IOException("Integrity of the event has been compromised!");
            }
        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException e) {
            throw new ApiAuthException("Unable to verify integrity of the event!", e);
        }
    }

    private static String generateHMAC(String algo, byte[] data, PrivateKey key) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec signingKey = new SecretKeySpec(key.getEncoded(), algo);
        Mac mac = Mac.getInstance(algo);
        mac.init(signingKey);
        return toHexString(mac.doFinal(data));
    }

    private static String toHexString(byte[] data) {
        Formatter result = new Formatter();
        for (byte b : data) {
            result.format("%02x", b);
        }
        return result.toString();
    }
}

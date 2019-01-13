package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.model.collect.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.http.HttpHeaders;

import javax.servlet.http.HttpServletRequest;
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

}

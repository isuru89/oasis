package io.github.isuru.oasis.db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {

    private static final Pattern JDBC_PATTERN = Pattern.compile("^(\\w+):(\\w+):.+");

    public static String captureDbName(String url) {
        Matcher matcher = JDBC_PATTERN.matcher(url.trim());
        if (matcher.find()) {
            return matcher.group(2);
        } else {
            return "";
        }
    }

}

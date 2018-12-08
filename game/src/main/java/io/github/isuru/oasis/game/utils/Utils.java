package io.github.isuru.oasis.game.utils;


import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.EventNames;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.mvel2.MVEL;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern TIME_PATTERN = Pattern.compile("([0-9]+)\\s*([a-zA-Z]+)");
    private static final Pattern NUMBER_PATTERN = Pattern.compile("([0-9\\\\.]+)\\s*([kKmMbB]?)");

    @SuppressWarnings("unchecked")
    public static <T> T createInst(String clz) throws ReflectiveOperationException {
        return (T) Thread.currentThread().getContextClassLoader()
                .loadClass(clz)
                .newInstance();
    }

    public static <T> T firstNonNull(T v1, T v2) {
        if (v1 != null) {
            return v1;
        } else {
            return v2;
        }
    }

    public static boolean eventEquals(Event against, String eventType) {
        return eventType.equals(against.getEventType());
    }

    public static String queueReplace(String name) {
        return name.replace("{gid}", System.getProperty(Constants.ENV_OASIS_GAME_ID, ""));
    }

    public static boolean isEndOfStream(Event event) {
        return Utils.eventEquals(event, EventNames.TERMINATE_GAME);
    }

    public static boolean isNonEmpty(Collection<?> list) {
        return list != null && !list.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static Serializable compileExpression(String expr) {
        return MVEL.compileExpression(expr);
    }

    public static boolean evaluateCondition(Serializable expression, Map<String, Object> variables) throws IOException {
        Object result = executeExpression(expression, variables);
        if (result == null) {
            throw new IOException("Expression does not return anything after evaluation! Make sure 'return' is specified.");
        }
        return (Boolean) result;
    }

    public static Object executeExpression(Serializable expression, Map<String, Object> variables) {
        return MVEL.executeExpression(expression, variables);
    }

    public static double toDouble(Number number) {
        return BigDecimal.valueOf(number.doubleValue()).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    public static Double strNum(String numStr) {
        Matcher matcher = NUMBER_PATTERN.matcher(numStr);
        if (matcher.find()) {
            double val = Double.parseDouble(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.startsWith("k")) {
                return val * 1000;
            } else if (unit.startsWith("m")) {
                return val * 1000 * 1000;
            } else if (unit.startsWith("b")) {
                return val * 1000 * 1000 * 1000;
            } else {
                return val;
            }
        }
        throw new IllegalArgumentException("Given number is not in the correct format! [" + numStr + "]");
    }

    public static Time fromStr(String durationStr) {
        Matcher matcher = TIME_PATTERN.matcher(durationStr);
        if (matcher.find()) {
            int val = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2).toLowerCase();
            if (unit.startsWith("d")) {
                return Time.days(val);
            } else if (unit.startsWith("s")) {
                return Time.seconds(val);
            } else if (unit.startsWith("min")) {
                return Time.minutes(val);
            }else if (unit.startsWith("h")) {
                return Time.hours(val);
            } else if (unit.startsWith("m")) {
                return Time.days(val * 30L);
            }
        }
        throw new IllegalArgumentException("Given duration string found to be invalid format! [" + durationStr + "]");
    }

}

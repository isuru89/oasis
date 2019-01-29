package io.github.isuru.oasis.services.utils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Commons {

    public static boolean isNullOrEmpty(Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static double asDouble(Object val) {
        if (val == null) {
            return Double.NaN;
        } else if (val instanceof Number) {
            return ((Number) val).doubleValue();
        } else {
            try {
                return Double.parseDouble(val.toString());
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
    }

    public static String fixSearchQuery(String param) {
        if (param == null) {
            return null;
        }
        return param.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");
    }

    public static <T> T orDefault(T value, T defValue) {
        if (value == null) {
            return defValue;
        } else {
            return value;
        }
    }

    @Nullable
    public static <T> T firstNonNull(T... items) {
        if (items == null) {
            return null;
        }
        for (T item : items) {
            if (item != null) {
                return item;
            }
        }
        return null;
    }

    public static <T> Stream<List<T>> batches(List<T> source, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length must be a valid! [" + length + "]");
        }
        int size = source.size();
        if (size <= 0) {
            return Stream.empty();
        }
        int fullChunks = (size - 1) / length;
        return IntStream.range(0, fullChunks + 1).mapToObj(
                n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
    }
}

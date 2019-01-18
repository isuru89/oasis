package io.github.isuru.oasis.services.utils;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

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

    public static String fixSearchQuery(String param) {
        if (param == null) {
            return null;
        }
        return param.replace("!", "!!")
                .replace("%", "!%")
                .replace("_", "!_")
                .replace("[", "![");
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
}

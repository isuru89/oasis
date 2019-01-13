package io.github.isuru.oasis.services.utils;

import java.util.Collection;

public class Commons {

    public static boolean isNullOrEmpty(Collection<?> list) {
        return list == null || list.isEmpty();
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

}

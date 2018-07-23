package io.github.isuru.oasis.services.utils;

/**
 * @author iweerarathna
 */
public class Pojos {

    public static String compareWith(String latest, String prev) {
        if (latest != null) {
            return latest.equals(prev) ? prev : latest;
        } else {
            return prev;
        }
    }

    public static Integer compareWith(Integer latest, Integer prev) {
        if (latest != null) {
            return latest;
        } else {
            return prev;
        }
    }

}

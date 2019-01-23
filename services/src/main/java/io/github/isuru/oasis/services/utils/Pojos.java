package io.github.isuru.oasis.services.utils;

import java.io.*;

/**
 * @author iweerarathna
 */
public class Pojos {

    @SuppressWarnings("unchecked")
    public static <T> T deserialize(byte[] stateData) throws IOException, ClassNotFoundException {
        if (stateData == null || stateData.length == 0) {
            return null;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(stateData);
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            return (T) ois.readObject();
        }
    }

    public static byte[] serialize(Object check) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {

            oos.writeObject(check);
            oos.flush();

            return baos.toByteArray();
        }
    }

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

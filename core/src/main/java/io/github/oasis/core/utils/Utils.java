/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.utils;

import java.io.*;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Isuru Weerarathna
 */
public class Utils {

    public static Object firstNonNull(Object first, Object second) {
        if (first != null) {
            return first;
        } else {
            return second;
        }
    }

    public static String firstNonNullAsStr(Object first, Object second) {
        return String.valueOf(firstNonNull(first, second));
    }

    public static <T> T firstNonNullAsStr(T first, T second, T third) {
        if (first != null) {
            return first;
        } else if (second != null) {
            return second;
        } else {
            return third;
        }
    }

    public static String bytesToHex(byte[] digest) {
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }

    public static BigDecimal toBigDecimal(Object value) {
        if (value != null) {
            return new BigDecimal(value.toString());
        }
        return null;
    }

    public static byte[] getBytesFromUUID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());

        return bb.array();
    }

    public static boolean isNotEmpty(Collection<?> collection) {
        return collection != null && !collection.isEmpty();
    }

    public static boolean isEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T fromSerializedContent(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bis)) {

            return (T) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

    public static byte[] toSerializableContent(Object object) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            oos.writeObject(object);
            return bos.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deepClone(T object) {

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(bos);) {
            oos.writeObject(object);

            try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                 ObjectInputStream ois = new ObjectInputStream(bis)) {
                return (T) ois.readObject();
            }
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }

}

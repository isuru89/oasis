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

package io.github.oasis.services.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

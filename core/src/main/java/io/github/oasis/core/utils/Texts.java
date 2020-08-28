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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Isuru Weerarathna
 */
public class Texts {

    public static final String COLON = ":";
    private static final String EMPTY = "";

    public static String subStrPrefixAfter(String source, String prefix) {
        return source.substring(prefix.length());
    }

    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    public static boolean isNotEmpty(String text) {
        return !isEmpty(text);
    }

    public static String orDefault(String text) {
        return orDefault(text, EMPTY);
    }

    public static String orDefault(String text, String defaultValueIfEmpty) {
        return text == null ? defaultValueIfEmpty : text;
    }

    public static String md5Digest(String text) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            return Utils.bytesToHex(md5.digest(text.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 hash algorithm missing in JVM!", e);
        }
    }
}

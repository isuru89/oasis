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

package io.github.oasis.game.utils;

import java.io.Serializable;
import java.util.TreeMap;

/**
 * @author Isuru Weerarathna
 */
public class RangeMap<K, V> extends TreeMap<K, V> implements Serializable {

    public static void main(String[] args) {
        TreeMap<Integer, String> blame = new TreeMap<>();
        blame.put(1, "Jon");
        blame.put(5, "Tyrion");
        blame.put(8, "Arya");
        blame.put(12, "Jaime");
        blame.put(20, null);

        System.out.println(blame.higherEntry(6).getValue());
        System.out.println(blame.lowerEntry(6).getValue());
        System.out.println(blame.lowerEntry(5).getValue());
        System.out.println(blame.floorEntry(21).getValue());
    }


}

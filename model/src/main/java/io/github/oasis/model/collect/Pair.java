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

package io.github.oasis.model.collect;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class Pair<A extends Serializable, B extends Serializable> implements Serializable {

    private final A value0;
    private final B value1;

    private Pair(A value0, B value1) {
        this.value0 = value0;
        this.value1 = value1;
    }

    public static <A extends Serializable, B extends Serializable> Pair<A, B> of(A value0, B value1) {
        return new Pair<>(value0, value1);
    }

    public A getValue0() {
        return value0;
    }

    public B getValue1() {
        return value1;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "0=" + value0 +
                ", 1=" + value1 +
                '}';
    }
}

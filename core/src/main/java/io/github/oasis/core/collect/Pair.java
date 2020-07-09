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

package io.github.oasis.core.collect;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public class Pair<A extends Serializable, B extends Serializable> implements Serializable {

    private final A left;
    private final B right;

    private Pair(A left, B right) {
        this.left = left;
        this.right = right;
    }

    public static <A extends Serializable, B extends Serializable> Pair<A, B> of(A left, B right) {
        return new Pair<>(left, right);
    }

    public A getLeft() {
        return left;
    }

    public B getRight() {
        return right;
    }

    @Override
    public String toString() {
        return "Pair{" +
                "l=" + left +
                ", r=" + right +
                '}';
    }
}

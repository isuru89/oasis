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

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class CommonsUtils {

    @Test
    public void testAsDouble() {
        Assert.assertEquals(Double.NaN, Commons.asDouble(null), 0.01);
        Assert.assertEquals(3.0, Commons.asDouble(3L), 0.01);
        Assert.assertEquals(5.0, Commons.asDouble(5), 0.01);
        Assert.assertEquals(34.0, Commons.asDouble("34"), 0.01);
        Assert.assertEquals(Double.NaN, Commons.asDouble("abc"), 0.01);
    }

    @Test
    public void testNullOrEmpty() {
        Assert.assertTrue(Commons.isNullOrEmpty((Map)null));
        Assert.assertTrue(Commons.isNullOrEmpty(new HashMap<>()));
        Assert.assertFalse(Commons.isNullOrEmpty(Maps.create("a", 1)));

        Assert.assertTrue(Commons.isNullOrEmpty((List)null));
        Assert.assertTrue(Commons.isNullOrEmpty(new ArrayList<>()));
        Assert.assertFalse(Commons.isNullOrEmpty(Arrays.asList("a", "b", "c")));

        Assert.assertTrue(Commons.isNullOrEmpty((String) null));
        Assert.assertTrue(Commons.isNullOrEmpty(""));
        Assert.assertTrue(Commons.isNullOrEmpty("   "));
        Assert.assertTrue(Commons.isNullOrEmpty("\t"));
        Assert.assertFalse(Commons.isNullOrEmpty("A"));
    }

    @Test
    public void testFixParam() {
        Assert.assertNull(Commons.fixSearchQuery(null));
        Assert.assertEquals("abc", Commons.fixSearchQuery("abc"));
        Assert.assertEquals("a!!bc", Commons.fixSearchQuery("a!bc"));
        Assert.assertEquals("a!%bc", Commons.fixSearchQuery("a%bc"));
        Assert.assertEquals("ab!_c", Commons.fixSearchQuery("ab_c"));
        Assert.assertEquals("![abc", Commons.fixSearchQuery("[abc"));
    }

    @Test
    public void testOrDefault() {
        Assert.assertEquals("a", Commons.orDefault("a", "b"));
        Assert.assertEquals("b", Commons.orDefault(null, "b"));
        Assert.assertEquals("a", Commons.orDefault(null, "a"));
        Assert.assertNull(Commons.orDefault(null, null));
    }

    @Test
    public void testFirstNonNull() {
        Assert.assertEquals("a", Commons.firstNonNull("a"));
        Assert.assertEquals("a", Commons.firstNonNull(null, "a"));
        Assert.assertNull(Commons.firstNonNull(null, null));
        Assert.assertNull(Commons.firstNonNull(null));
    }

    @Test
    public void testBatches() {
        List<String> list = Arrays.asList("a", "b", "c", "d", "e", "f");
        List<String> emptyList = new ArrayList<>();

        Assertions.assertThat(Commons.batches(list, 2))
                .isNotNull().isNotEmpty()
                .hasSize(3)
                .contains(Arrays.asList("a", "b"),
                        Arrays.asList("c", "d"),
                        Arrays.asList("e", "f"));
        Assertions.assertThat(Commons.batches(list, 4))
                .isNotNull().isNotEmpty()
                .hasSize(2)
                .contains(Arrays.asList("a", "b", "c", "d"),
                        Arrays.asList("e", "f"));
        Assertions.assertThat(Commons.batches(list, 7))
                .isNotNull().isNotEmpty()
                .hasSize(1)
                .contains(Arrays.asList("a", "b", "c", "d", "e", "f"));

        Assertions.assertThat(Commons.batches(emptyList, 3))
                .isNotNull().isEmpty();

        Assertions.assertThatThrownBy(() -> Commons.batches(list, 0))
                .isInstanceOf(IllegalArgumentException.class);
        Assertions.assertThatThrownBy(() -> Commons.batches(list, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }

}

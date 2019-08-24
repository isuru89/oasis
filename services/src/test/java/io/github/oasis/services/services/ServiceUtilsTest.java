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

package io.github.oasis.services.services;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class ServiceUtilsTest {

    @Test
    public void testToList() {
        {
            List<String> list = Arrays.asList("a", "b", "c");
            List<String> result = ServiceUtils.toList(new HashSet<>(list));
            Assertions.assertThat(result)
                    .hasSize(list.size())
                    .containsAll(list);
        }
        {
            List<String> list = Arrays.asList("a", "b");
            List<String> result = ServiceUtils.toList(list);
            Assertions.assertThat(result)
                    .hasSize(list.size())
                    .containsAll(list);
        }
        {
            List<String> result = ServiceUtils.toList(null);
            Assertions.assertThat(result).isNotNull().isEmpty();
        }

    }

    @Test
    public void testOrDefault() {
        Assert.assertEquals(1, ServiceUtils.orDefault(new Integer(1), 3));
        Assert.assertEquals(3, ServiceUtils.orDefault((Integer) null, 3));

        Assert.assertEquals(5, ServiceUtils.orDefault(new Long(5), 3));
        Assert.assertEquals(3, ServiceUtils.orDefault((Long) null, 3));
    }

    @SuppressWarnings("boxing")
    @Test
    public void testIsValid() {
        Assert.assertTrue(ServiceUtils.isValid(Long.valueOf(123L)));
        Assert.assertTrue(ServiceUtils.isValid(new Long(1)));
        Assert.assertTrue(ServiceUtils.isValid(new Long(Long.MAX_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Long(Long.MIN_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Long(0)));

        Assert.assertTrue(ServiceUtils.isValid(new Integer(345)));
        Assert.assertTrue(ServiceUtils.isValid(new Integer(1)));
        Assert.assertTrue(ServiceUtils.isValid(new Integer(Integer.MAX_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Integer(Integer.MIN_VALUE)));
        Assert.assertFalse(ServiceUtils.isValid(new Integer(0)));

        Assert.assertFalse(ServiceUtils.isValid((Integer) null));
        Assert.assertFalse(ServiceUtils.isValid((Long) null));

        Assert.assertFalse(ServiceUtils.isValid((Boolean) null));
        Assert.assertTrue(ServiceUtils.isValid(true));
        Assert.assertTrue(ServiceUtils.isValid(false));
        Assert.assertTrue(ServiceUtils.isValid(Boolean.TRUE));
        Assert.assertTrue(ServiceUtils.isValid(Boolean.FALSE));
    }
}

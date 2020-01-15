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

package io.github.oasis.game;


import org.junit.Test;

/**
 * @author iweerarathna
 */
public class BadgesTest extends AbstractTest {

    @Test
    public void testBadges() throws Exception {
        beginTest("badge-test1", 2000);
    }

    @Test
    public void testTimeBadges() throws Exception {
        beginTest("badge-time-test", 1000);
    }

    @Test
    public void testBadgesFromPoints() throws Exception {
        beginTest("badge-test-points", 1000);
    }

    @Test
    public void testAggBadges() throws Exception {
        beginTest("badge-agg-points");
    }

    @Test
    public void testBadgeHisto() throws Exception {
        beginTest("badge-histogram");
    }

    @Test
    public void testBadgeHistoSep() throws Exception {
        beginTest("badge-histogram-sep");
    }
}

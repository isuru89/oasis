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

package io.github.oasis.engine.rules;

import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.processors.HistogramStreakN;
import io.github.oasis.engine.rules.signals.HistogramBadgeSignal;
import io.github.oasis.engine.rules.signals.Signal;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Histogram Count Streaks")
public class HistogramCountStreakTest extends AbstractRuleTest {

    public static final String EVT_A = "a";
    public static final String EVT_B = "b";
    private static int THRESHOLD_ONE = 1;
    private static long FIFTY = 50;

    @DisplayName("Rule should be able to create")
    @Test
    public void shouldNotBeAbleToSetValueResolver() {
        HistogramCountStreakNRule options = new HistogramCountStreakNRule("test.histogram.count");
        options.setStreaks(Arrays.asList(3,5));
        options.setConsecutive(true);
        options.setTimeUnit(FIFTY);
        options.setCondition(event -> (long) event.getFieldValue("value") >= 50);

        Assertions.assertEquals(BigDecimal.ONE, options.getThreshold());
        Assertions.assertThrows(IllegalStateException.class, () -> options.setValueResolver(event -> 0.0));
    }

    @DisplayName("Single streak: No matching event types")
    @Test
    public void testHistogramStreakNNoEventTypes() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_B, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_B, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_B, 88);
        TEvent e6 = TEvent.createKeyValue(205, EVT_B, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_B, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_B, 11);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Collections.singletonList(3), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak")
    @Test
    public void testHistogramStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e6 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_A, 11);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Collections.singletonList(3), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e7.getExternalId()));
    }

    @DisplayName("Multiple streaks")
    @Test
    public void testHistogramMultiStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91);
        TEvent e8 = TEvent.createKeyValue(312, EVT_A, 80);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Arrays.asList(3, 5), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 5, 100, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Out-of-order no affect for existing badges")
    @Test
    public void testBreakHistogramMultiStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96); // --
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91);
        TEvent e8 = TEvent.createKeyValue(312, EVT_A, 80); // --
        TEvent e9 = TEvent.createKeyValue(170, EVT_A, -88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Arrays.asList(3, 5), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 5, 100, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Out-of-order no affects for latest badge")
    @Test
    public void testBreakHistogramMultiStreakNOutOfOrderLower() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88);
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91);
        TEvent e8 = TEvent.createKeyValue(312, EVT_A, 80);
        TEvent e9 = TEvent.createKeyValue(275, EVT_A, -88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Arrays.asList(3, 5), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e6.getExternalId()),
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 5, 100, 300, e8.getExternalId()));
    }

    @DisplayName("Multiple streaks: Out-of-order no affect for former badge in multiple streaks")
    @Test
    public void testBreakHistogramMultiStreakNWithHoles() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63); // --
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 88); // --
        TEvent e5 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e6 = TEvent.createKeyValue(235, EVT_A, 96); // --
        TEvent e7 = TEvent.createKeyValue(265, EVT_A, 91); // --
        TEvent e9 = TEvent.createKeyValue(170, EVT_A, -88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Arrays.asList(3, 5), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e6.getExternalId()));
    }

    @DisplayName("Single streak: No streaks available yet")
    @Test
    public void testNoHistogramStreakN() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 7);
        TEvent e4 = TEvent.createKeyValue(187, EVT_A, 18);
        TEvent e6 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_A, 71);
        TEvent e9 = TEvent.createKeyValue(285, EVT_A, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Collections.singletonList(3), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @DisplayName("Single streak: No streaks due to non-existence buckets")
    @Test
    public void testNoHistogramStreakNWithHole() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e6 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e7 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e8 = TEvent.createKeyValue(265, EVT_A, 71);
        TEvent e9 = TEvent.createKeyValue(285, EVT_A, 21);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Collections.singletonList(3), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e6, e7, e8, e9);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(0, signals.size());
    }

    @DisplayName("Multiple streaks: Out-of-order affects former streak in multiple streaks")
    @Test
    public void testHistogramStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 57);
        TEvent e4 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e5 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e6 = TEvent.createKeyValue(265, EVT_A, 11);
        TEvent e7 = TEvent.createKeyValue(187, EVT_A, 88);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Collections.singletonList(3), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        Assert.assertEquals(1, signals.size());
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e7.getExternalId()));
    }

    @DisplayName("Single streak: Out-of-order no affect for only badge")
    @Test
    public void testBreakHistogramStreakNOutOfOrder() {
        TEvent e1 = TEvent.createKeyValue(110, EVT_A, 75);
        TEvent e2 = TEvent.createKeyValue(144, EVT_A, 63);
        TEvent e3 = TEvent.createKeyValue(156, EVT_A, 99);
        TEvent e4 = TEvent.createKeyValue(205, EVT_A, 26);
        TEvent e5 = TEvent.createKeyValue(235, EVT_A, 96);
        TEvent e6 = TEvent.createKeyValue(265, EVT_A, 11);
        TEvent e7 = TEvent.createKeyValue(187, EVT_A, -50);

        List<Signal> signalsRef = new ArrayList<>();
        RuleContext<HistogramStreakNRule> ruleContext = createOptions(Collections.singletonList(3), FIFTY, signalsRef::add);
        HistogramStreakN streakN = new HistogramStreakN(pool, ruleContext);
        submitOrder(streakN, e1, e2, e3, e4, e5, e6, e7);

        Set<Signal> signals = mergeSignals(signalsRef);
        System.out.println(signals);
        assertStrict(signals,
                new HistogramBadgeSignal(ruleContext.getRule().getId(), 3, 100, 200, e5.getExternalId()));
    }

    private RuleContext<HistogramStreakNRule> createOptions(List<Integer> streaks, long timeunit, Consumer<Signal> consumer) {
        return createOptions(streaks, timeunit, THRESHOLD_ONE, consumer);
    }

    private RuleContext<HistogramStreakNRule> createOptions(List<Integer> streaks, long timeunit, long threshold, Consumer<Signal> consumer) {
        HistogramCountStreakNRule options = new HistogramCountStreakNRule("test.histogram.count.streak");
        options.setForEvent(EVT_A);
        options.setStreaks(streaks);
        options.setConsecutive(true);
        options.setThreshold(BigDecimal.valueOf(threshold));
        options.setTimeUnit(timeunit);
        options.setCondition(event -> (long) event.getFieldValue("value") >= 50);
        return new RuleContext<>(options, consumer);
    }
}

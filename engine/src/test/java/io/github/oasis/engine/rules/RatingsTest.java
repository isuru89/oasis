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
import io.github.oasis.engine.processors.RatingProcessor;
import io.github.oasis.engine.rules.signals.RatingChangedSignal;
import io.github.oasis.engine.rules.signals.RatingPointsSignal;
import io.github.oasis.engine.rules.signals.Signal;
import io.github.oasis.engine.utils.Constants;
import io.github.oasis.model.Event;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Ratings")
public class RatingsTest extends AbstractRuleTest {

    private static final String EVT_A = "a";
    private static final String EVT_B = "b";

    private static final int DEF_RATING = 1;

    @DisplayName("No ratings")
    @Test
    public void testNoRatings() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(155, EVT_A, 75);
        TEvent e5 = TEvent.createKeyValue(160, EVT_A, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<RatingRule> ruleContext = createRule(signals);
        RatingRule rule = ruleContext.getRule();
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("No matched events")
    @Test
    public void testNoMatchedEvents() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_B, 57);
        TEvent e2 = TEvent.createKeyValue(105, EVT_B, 83);
        TEvent e3 = TEvent.createKeyValue(110, EVT_B, 34);
        TEvent e4 = TEvent.createKeyValue(155, EVT_B, 75);
        TEvent e5 = TEvent.createKeyValue(160, EVT_B, 64);

        List<Signal> signals = new ArrayList<>();
        RuleContext<RatingRule> ruleContext = createRule(signals,
                aRating(1, 1, checkGt(85), pointAward(1)),
                aRating(2, 2, checkGt(65), pointAward(2)),
                aRating(3, 3, checkGt(50), pointAward(3))
        );
        RatingRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4, e5);

        System.out.println(signals);
        Assertions.assertEquals(0, signals.size());
    }

    @DisplayName("Zero scores when rating points unspecified")
    @Test
    public void testZeroPointsUnspecified() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<RatingRule> ruleContext = createRule(signals,
                aRating(1, 3, checkGt(85), null),
                aRating(2, 2, checkGt(65), null),
                aRating(3, 1, checkGt(50), null)
        );
        RatingRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 2, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), 2, asDecimal(0), e2)
        );
    }

    @DisplayName("Rating go up")
    @Test
    public void testGoUpRating() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<RatingRule> ruleContext = createRule(signals,
                aRating(1, 3, checkGt(85), pointAward(3)),
                aRating(2, 2, checkGt(65), pointAward(2)),
                aRating(3, 1, checkGt(50), pointAward(1))
        );
        RatingRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 2, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), 2, asDecimal(10), e2)
        );
    }

    @DisplayName("Rating go down")
    @Test
    public void testGoDownRating() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        RuleContext<RatingRule> ruleContext = createRule(signals,
                aRating(1, 3, checkGt(85), pointAward(3)),
                aRating(2, 2, checkGt(65), pointAward(2)),
                aRating(3, 1, checkGt(50), pointAward(1))
        );
        RatingRule rule = ruleContext.getRule();
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 3, e1.getTimestamp(), e1),
                new RatingChangedSignal(rule.getId(), 3, 1, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), 3, asDecimal(20), e1),
                new RatingPointsSignal(rule.getId(), 1, asDecimal(-20), e2)
        );
    }

    private BigDecimal asDecimal(long val) {
        return BigDecimal.valueOf(val).setScale(Constants.SCALE, BigDecimal.ROUND_HALF_UP);
    }

    private Predicate<Event> checkGt(long margin) {
        return event1 -> (long) event1.getFieldValue("value") >= margin;
    }

    private Predicate<Event> checkLt(long margin) {
        return event1 -> (long) event1.getFieldValue("value") < margin;
    }

    private BigDecimal noPoints(Event event, int prevRating) {
        return BigDecimal.ZERO;
    }

    private BiFunction<Event, Integer, BigDecimal> pointAward(int currRating) {
        return (event, prevRating) -> BigDecimal.valueOf((currRating - prevRating) * 10.0);
    }

    private RatingRule.Rating aRating(int priority, int rating, Predicate<Event> criteria,
                                      BiFunction<Event, Integer, BigDecimal> pointDerive) {
        return new RatingRule.Rating(priority, rating, criteria, pointDerive);
    }

    private RuleContext<RatingRule> createRule(Collection<Signal> signals, RatingRule.Rating... ratings) {
        RatingRule rule = new RatingRule("test.rating.rule");
        rule.setForEvent(EVT_A);
        rule.setDefaultRating(DEF_RATING);
        rule.setRatings(Arrays.asList(ratings));
        return new RuleContext<>(rule, signals::add);
    }

}

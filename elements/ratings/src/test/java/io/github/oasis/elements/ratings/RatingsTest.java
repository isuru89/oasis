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

package io.github.oasis.elements.ratings;

import io.github.oasis.core.elements.RuleContext;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.elements.matchers.SingleEventTypeMatcher;
import io.github.oasis.core.utils.Constants;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
@DisplayName("Ratings")
public class RatingsTest extends AbstractRuleTest {

    private static final String EVT_A = "user.average";
    private static final String EVT_B = "unknown.event";

    private static final int DEF_RATING = 1;

    private static final String POINT_ID = "rating.points";

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
        RatingRule rule = loadRule("ratings.yml", "WITH_THREE_RATINGS");
        RuleContext<RatingRule> ruleContext = createRule(rule, signals);
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
        RatingRule rule = loadRule("ratings.yml", "WITH_THREE_RATINGS");
        RuleContext<RatingRule> ruleContext = createRule(rule, signals);
        rule.setRatings(rule.getRatings().stream().map(rating -> new RatingRule.Rating(
                rating.getPriority(),
                rating.getRating(),
                rating.getCriteria(),
                null,
                POINT_ID
        )).collect(Collectors.toList()));
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 2, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), POINT_ID, 2, asDecimal(0), e2)
        );
    }

    @DisplayName("Rating go up")
    @Test
    public void testGoUpRating() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);
        TEvent e4 = TEvent.createKeyValue(120, EVT_B, 66);

        List<Signal> signals = new ArrayList<>();
        RatingRule rule = loadRule("ratings.yml", "WITH_THREE_RATINGS");
        RuleContext<RatingRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3, e4);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 2, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), POINT_ID, 2, asDecimal(10), e2)
        );

        RatingChangedSignal signal = (RatingChangedSignal) signals.stream().filter(s -> s instanceof RatingChangedSignal).findFirst().orElse(null);
        Assertions.assertNotNull(signal);
        Assertions.assertEquals(RatingsSink.class, signal.sinkHandler());
    }

    @DisplayName("Rating stays after goes up")
    @Test
    public void testRatingStays() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 57);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 83);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 75);

        List<Signal> signals = new ArrayList<>();
        RatingRule rule = loadRule("ratings.yml", "WITH_THREE_RATINGS");
        RuleContext<RatingRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 2, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), POINT_ID, 2, asDecimal(10), e2)
        );
    }

    @DisplayName("Rating go down")
    @Test
    public void testGoDownRating() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 87);
        TEvent e2 = TEvent.createKeyValue(105, EVT_A, 53);
        TEvent e3 = TEvent.createKeyValue(110, EVT_A, 34);

        List<Signal> signals = new ArrayList<>();
        RatingRule rule = loadRule("ratings.yml", "WITH_THREE_RATINGS");
        RuleContext<RatingRule> ruleContext = createRule(rule, signals);
        Assertions.assertEquals(3, rule.getRatings().size());
        Assertions.assertEquals(DEF_RATING, rule.getDefaultRating());
        RatingProcessor processor = new RatingProcessor(pool, ruleContext);
        submitOrder(processor, e1, e2, e3);

        System.out.println(signals);
        assertStrict(signals,
                new RatingChangedSignal(rule.getId(), DEF_RATING, 3, e1.getTimestamp(), e1),
                new RatingChangedSignal(rule.getId(), 3, 1, e2.getTimestamp(), e2),
                new RatingPointsSignal(rule.getId(), POINT_ID, 3, asDecimal(20), e1),
                new RatingPointsSignal(rule.getId(), POINT_ID, 1, asDecimal(-20), e2)
        );
    }

    @DisplayName("No point event when empty point id")
    @Test
    public void noPointEventIfEmptyPointId() {
        TEvent e1 = TEvent.createKeyValue(100, EVT_A, 57);
        RatingRule rule = loadRule("ratings.yml", "WITH_THREE_RATINGS");
        RatingPointsSignal withPointId = new RatingPointsSignal(rule.getId(), POINT_ID, 3, asDecimal(20), e1);
        Assertions.assertTrue(withPointId.generateEvent().isPresent());
        RatingPointsSignal noPointId = new RatingPointsSignal(rule.getId(), null, 3, asDecimal(20), e1);
        Assertions.assertFalse(noPointId.generateEvent().isPresent());
    }

    private BigDecimal asDecimal(long val) {
        return BigDecimal.valueOf(val).setScale(Constants.SCALE, RoundingMode.HALF_UP);
    }

    private RuleContext<RatingRule> createRule(Collection<Signal> signals, RatingRule.Rating... ratings) {
        RatingRule rule = new RatingRule("test.rating.rule");
        rule.setEventTypeMatcher(new SingleEventTypeMatcher(EVT_A));
        rule.setDefaultRating(DEF_RATING);
        rule.setRatings(Arrays.asList(ratings));
        return new RuleContext<>(rule, fromConsumer(signals::add));
    }

    private RuleContext<RatingRule> createRule(RatingRule rule, Collection<Signal> signals) {
        return new RuleContext<>(rule, fromConsumer(signals::add));
    }

}

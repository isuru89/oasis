/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.elements.ratings;

import io.github.oasis.core.EventJson;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.spec.AwardDef;
import io.github.oasis.core.elements.spec.PointAwardDef;
import io.github.oasis.core.elements.spec.SelectorDef;
import io.github.oasis.elements.ratings.spec.ARatingDef;
import io.github.oasis.elements.ratings.spec.RatingSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class RatingParserTest {

    private RatingParser parser;

    @BeforeEach
    void beforeEach() {
        parser = new RatingParser();
    }

    @Test
    void parse() {
    }

    @Test
    void convert() {
        RatingDef def = new RatingDef();
        def.setId("RATING00001");
        def.setName("rating-1");
        def.setType("core:rating");
        def.setSpec(new RatingSpecification());
        def.getSpec().setSelector(new SelectorDef());
        def.getSpec().getSelector().setMatchEvent("event.a");
        def.getSpec().setDefaultRating(1);
        def.getSpec().setRatings(List.of(
                aRatingDef(1, 1, "p1", BigDecimal.valueOf(20), "e.data.value > 100"),
                aRatingDef(3, 3, "p3", BigDecimal.valueOf(-20), "e.data.value < 0"),
                aRatingDef(2, 2, "p2", BigDecimal.valueOf(10), "e.data.value > 0 && e.data.value < 100"),
                aRatingDef(4, 4, "p4", BigDecimal.ZERO, "e.data.value < -100"),
                aRatingDef(5, 5, "p5", "previousRating * 2", "e.data.value > 10000")
        ));

        AbstractRule abstractRule = parser.convert(def);

        Assertions.assertTrue(abstractRule instanceof RatingRule);
        RatingRule rule = (RatingRule) abstractRule;
        Assertions.assertEquals(def.getSpec().getDefaultRating(), rule.getDefaultRating());
        Assertions.assertEquals(def.getSpec().getRatings().size(), rule.getRatings().size());
        // ratings must be ordered by priority
        Assertions.assertEquals(5, rule.getRatings().stream()
                .map(RatingRule.Rating::getPriority)
                .reduce(0, (val1, val2) -> {
                    Assertions.assertTrue(val1 < val2);
                    return val2;
                })
                .intValue());

        RatingRule.Rating secondRating = rule.getRatings().stream().filter(rating -> rating.getPriority() == 2).findFirst().orElse(null);
        Assertions.assertNotNull(secondRating);
        Assertions.assertEquals(2, secondRating.getRating());
        Assertions.assertEquals("p2", secondRating.getPointId());
        Assertions.assertNotNull(secondRating.getCriteria());
        Assertions.assertEquals(new BigDecimal("10"), secondRating.getPointAwards().resolve(null, 0));
        Assertions.assertEquals(new BigDecimal("10"), secondRating.getPointAwards().resolve(null, 1));


        RatingRule.Rating fourthRating = rule.getRatings().stream().filter(rating -> rating.getPriority() == 4).findFirst().orElse(null);
        Assertions.assertNotNull(fourthRating);
        Assertions.assertNotNull(fourthRating.getPointAwards());
        Assertions.assertEquals(BigDecimal.ZERO, fourthRating.getPointAwards().resolve(null, 0));
        Assertions.assertEquals(BigDecimal.ZERO, fourthRating.getPointAwards().resolve(null, 1));


        RatingRule.Rating fifthRating = rule.getRatings().stream().filter(rating -> rating.getPriority() == 5).findFirst().orElse(null);
        Assertions.assertNotNull(fifthRating);
        Assertions.assertNotNull(fifthRating.getPointAwards());
        EventJson eventJson = new EventJson();
        Assertions.assertEquals(new BigDecimal("4.0"), fifthRating.getPointAwards().resolve(eventJson, 2));
        Assertions.assertEquals(new BigDecimal("20.0"), fifthRating.getPointAwards().resolve(eventJson, 10));
    }

    private ARatingDef aRatingDef(int priority, int rating, String pointId, BigDecimal award, String criteria) {
        ARatingDef def = new ARatingDef();
        def.setPriority(priority);
        def.setRating(rating);
        def.setCondition(criteria);
        def.setRewards(new AwardDef());
        def.getRewards().setPoints(new PointAwardDef());
        def.getRewards().getPoints().setId(pointId);
        def.getRewards().getPoints().setAmount(award);
        return def;
    }

    private ARatingDef aRatingDef(int priority, int rating, String pointId, String award, String criteria) {
        ARatingDef def = new ARatingDef();
        def.setPriority(priority);
        def.setRating(rating);
        def.setCondition(criteria);
        def.setRewards(new AwardDef());
        def.getRewards().setPoints(new PointAwardDef());
        def.getRewards().getPoints().setId(pointId);
        def.getRewards().getPoints().setExpression(award);
        return def;
    }
}
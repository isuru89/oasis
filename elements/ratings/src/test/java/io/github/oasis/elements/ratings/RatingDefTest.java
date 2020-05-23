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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
class RatingDefTest {

    @Test
    void testUniqueIDGenerationRatingId() {
        RatingDef def1 = new RatingDef();
        def1.setDefaultRating(1);

        RatingDef def2 = new RatingDef();
        def2.setDefaultRating(1);

        RatingDef def3 = new RatingDef();
        def3.setDefaultRating(2);

        Assertions.assertEquals(def1.getDefaultRating(), def2.getDefaultRating());
        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.getDefaultRating(), def3.getDefaultRating());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    @Test
    void testUniqueIDGenerationRatings() {
        RatingDef def1 = new RatingDef();
        def1.setRatings(List.of(
            aRatingDef(1, 1),
            aRatingDef(2, 2)
        ));

        RatingDef def2 = new RatingDef();
        def2.setRatings(List.of(
                aRatingDef(1, 1),
                aRatingDef(2, 2)
        ));

        RatingDef def3 = new RatingDef();
        def3.setRatings(List.of(
                aRatingDef(1, 1),
                aRatingDef(2, 3)
        ));

        Assertions.assertEquals(def1.generateUniqueHash(), def2.generateUniqueHash());
        Assertions.assertNotEquals(def1.generateUniqueHash(), def3.generateUniqueHash());
    }

    private RatingDef.ARatingDef aRatingDef(int priority, int rating) {
        RatingDef.ARatingDef ratingDef = new RatingDef.ARatingDef();
        ratingDef.setPriority(priority);
        ratingDef.setRating(rating);

        Assertions.assertEquals(priority, ratingDef.getPriority());
        Assertions.assertEquals(rating, ratingDef.getRating());
        Assertions.assertNull(ratingDef.getPointId());
        Assertions.assertNull(ratingDef.getAward());
        Assertions.assertNull(ratingDef.getCriteria());
        return ratingDef;
    }
}
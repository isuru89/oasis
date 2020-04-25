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

import io.github.oasis.core.elements.AbstractDef;

import java.io.Serializable;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class RatingDef extends AbstractDef {

    private int defaultRating;
    private List<ARatingDef> ratings;

//    @Override
//    public AbstractRule toRule() {
//        RatingRule rule = new RatingRule(generateUniqueHash());
//        super.toRule(rule);
//        rule.setDefaultRating(defaultRating);
//        rule.setRatings(ratings.stream().map(r -> {
//            EventExecutionFilter criteria = EventExecutionFilterFactory.create(r.criteria);
//            return new RatingRule.Rating(r.priority,
//                    r.rating,
//                    criteria,
//                    r.awardResolver(),
//                    r.pointId);
//        }).collect(Collectors.toList()));
//        return rule;
//    }

    public List<ARatingDef> getRatings() {
        return ratings;
    }

    public void setRatings(List<ARatingDef> ratings) {
        this.ratings = ratings;
    }

    public int getDefaultRating() {
        return defaultRating;
    }

    public void setDefaultRating(int defaultRating) {
        this.defaultRating = defaultRating;
    }

    public static class ARatingDef {
        private int priority;
        private int rating;
        private String pointId;
        private Serializable criteria;
        private Serializable award;

//        public EventValueResolver<Integer> awardResolver() {
//            if (Objects.isNull(award)) {
//                return null;
//            }
//            if (award instanceof Number) {
//                return (event, input) -> BigDecimal.valueOf(((Number) award).doubleValue());
//            } else {
//                return Scripting.create((String)award, "previousRating");
//            }
//        }

        public int getPriority() {
            return priority;
        }

        public void setPriority(int priority) {
            this.priority = priority;
        }

        public int getRating() {
            return rating;
        }

        public void setRating(int rating) {
            this.rating = rating;
        }

        public String getPointId() {
            return pointId;
        }

        public void setPointId(String pointId) {
            this.pointId = pointId;
        }

        public Serializable getCriteria() {
            return criteria;
        }

        public void setCriteria(Serializable criteria) {
            this.criteria = criteria;
        }

        public Serializable getAward() {
            return award;
        }

        public void setAward(Serializable award) {
            this.award = award;
        }
    }
}

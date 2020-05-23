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
import io.github.oasis.core.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RatingDef extends AbstractDef {

    private int defaultRating;
    private List<ARatingDef> ratings;

    @Override
    protected List<String> getSensitiveAttributes() {
        List<String> base = new ArrayList<>(super.getSensitiveAttributes());
        base.add(String.valueOf(defaultRating));
        if (Objects.nonNull(ratings)) {
            base.addAll(ratings.stream()
                    .sorted(Comparator.comparingInt(o -> o.priority))
                    .flatMap(r -> r.getSensitiveAttributes().stream())
                    .collect(Collectors.toList()));
        }
        return base;
    }

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
        private Object criteria;
        private Object award;

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

        List<String> getSensitiveAttributes() {
            return List.of(
                    String.valueOf(priority),
                    String.valueOf(rating),
                    Utils.firstNonNullAsStr(criteria, EMPTY),
                    Utils.firstNonNullAsStr(award, EMPTY)
            );
        }

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

        public Object getCriteria() {
            return criteria;
        }

        public void setCriteria(Object criteria) {
            this.criteria = criteria;
        }

        public Object getAward() {
            return award;
        }

        public void setAward(Object award) {
            this.award = award;
        }
    }
}

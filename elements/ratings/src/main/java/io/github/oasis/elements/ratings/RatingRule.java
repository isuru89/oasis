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

import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventValueResolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class RatingRule extends AbstractRule {

    private int defaultRating;
    private List<Rating> ratings;

    public RatingRule(String id) {
        super(id);
    }

    public int getDefaultRating() {
        return defaultRating;
    }

    public void setDefaultRating(int defaultRating) {
        this.defaultRating = defaultRating;
    }

    public List<Rating> getRatings() {
        return ratings;
    }

    public void setRatings(List<Rating> ratings) {
        this.ratings = new ArrayList<>(ratings);
        Collections.sort(this.ratings);
    }

    public static class Rating implements Comparable<Rating> {
        private int priority;
        private int rating;
        private EventExecutionFilter criteria;
        private String pointId;
        private EventValueResolver<Integer> pointAwards;

        public Rating(int priority, int rating, EventExecutionFilter criteria, EventValueResolver<Integer> pointAwards,
                      String pointId) {
            this.priority = priority;
            this.rating = rating;
            this.criteria = criteria;
            this.pointAwards = pointAwards;
            this.pointId = pointId;
        }

        public String getPointId() {
            return pointId;
        }

        public EventValueResolver<Integer> getPointAwards() {
            return pointAwards;
        }

        public int getPriority() {
            return priority;
        }

        public int getRating() {
            return rating;
        }

        public EventExecutionFilter getCriteria() {
            return criteria;
        }

        @Override
        public int compareTo(Rating o) {
            return Integer.compare(priority, o.getPriority());
        }
    }

}

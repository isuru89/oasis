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
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Definition for rating rule.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
public class RatingDef extends AbstractDef {

    /**
     * Default rating to award, when no criteria is satisfied.
     *
     * Note: by default, the default rating will not be set to all users unless
     * at least one event is processed against a user.
     */
    private int defaultRating;

    /**
     * What to award.
     */
    private Object award;

    /**
     * List of ratings.
     */
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

    @Getter
    @Setter
    public static class ARatingDef {
        private int priority;
        private int rating;
        private String pointId;
        private Object criteria;
        private Object award;

        List<String> getSensitiveAttributes() {
            return List.of(
                    String.valueOf(priority),
                    String.valueOf(rating),
                    Utils.firstNonNullAsStr(criteria, EMPTY),
                    Utils.firstNonNullAsStr(award, EMPTY)
            );
        }
    }
}

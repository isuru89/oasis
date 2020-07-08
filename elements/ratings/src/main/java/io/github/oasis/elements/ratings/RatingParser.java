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

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractElementParser;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilter;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.EventValueResolver;
import io.github.oasis.core.elements.Scripting;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.utils.Utils;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RatingParser extends AbstractElementParser {

    private static final EventValueResolver<Integer> ZERO_AWARD = (event, prevRating) -> BigDecimal.ZERO;

    @Override
    public AbstractDef parse(PersistedDef persistedObj) {
        return loadFrom(persistedObj, RatingDef.class);
    }

    @Override
    public AbstractRule convert(AbstractDef definition) {
        if (definition instanceof RatingDef) {
            return toRule((RatingDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    private RatingRule toRule(RatingDef def) {
        String id = Utils.firstNonNullAsStr(def.getId(), def.generateUniqueHash());
        RatingRule rule = new RatingRule(id);
        AbstractDef.defToRule(def, rule);
        rule.setDefaultRating(def.getDefaultRating());
        if (Objects.nonNull(def.getRatings())) {
            rule.setRatings(def.getRatings().stream()
                    .map(rating -> {
                        EventExecutionFilter criteria = EventExecutionFilterFactory.create(rating.getCriteria());

                        return new RatingRule.Rating(rating.getPriority(),
                                rating.getRating(),
                                criteria,
                                deriveAward(rating.getAward()),
                                rating.getPointId());

                    }).collect(Collectors.toList()));
        }
        return rule;
    }

    private EventValueResolver<Integer> deriveAward(Object award) {
        if (Objects.isNull(award)) {
            return ZERO_AWARD;
        }

        if (award instanceof Number) {
            return (event, input) -> BigDecimal.valueOf(((Number) award).doubleValue());
        } else {
            return Scripting.create((String) award, Constants.VAR_RATING_AWARD_PREV_RATING);
        }
    }
}

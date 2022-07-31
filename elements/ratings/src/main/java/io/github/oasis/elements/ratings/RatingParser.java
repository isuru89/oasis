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

import io.github.oasis.core.elements.*;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.elements.spec.PointAwardDef;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.elements.ratings.spec.RatingSpecification;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class RatingParser extends AbstractElementParser {

    private static final EventValueResolver<Integer> ZERO_AWARD = (event, prevRating) -> BigDecimal.ZERO;

    @Override
    public AbstractDef<? extends BaseSpecification> parse(EngineMessage persistedObj) {
        RatingDef def = loadFrom(persistedObj, RatingDef.class);
        def.validate();
        return def;
    }

    @Override
    public AbstractRule convert(AbstractDef<? extends BaseSpecification> definition) {
        if (definition instanceof RatingDef) {
            return toRule((RatingDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    @Override
    public AcceptedDefinitions getAcceptingDefinitions() {
        return new AcceptedDefinitions().addAcceptingDefinition(
                RatingsModule.ID,
                new AcceptedDefinition()
                        .setDefinitionClz(RatingDef.class)
                        .setSpecificationClz(RatingSpecification.class)
        );
    }

    private RatingRule toRule(RatingDef def) {
        def.validate();

        RatingRule rule = new RatingRule(def.getId());
        AbstractDef.defToRule(def, rule);

        rule.setDefaultRating(def.getSpec().getDefaultRating());
        if (Utils.isNotEmpty(def.getSpec().getRatings())) {
            rule.setRatings(def.getSpec().getRatings().stream()
                    .map(rating -> {
                        EventExecutionFilter criteria = EventExecutionFilterFactory.create(rating.getCondition());

                        return new RatingRule.Rating(rating.getPriority(),
                                rating.getRating(),
                                criteria,
                                deriveAward(rating.getRewards().getPoints()),
                                rating.getRewards().getPoints().getId());

                    }).collect(Collectors.toList()));
        }
        return rule;
    }

    private EventValueResolver<Integer> deriveAward(PointAwardDef award) {
        if (Objects.isNull(award)) {
            return ZERO_AWARD;
        }

        if (award.getAmount() != null) {
            return (event, input) -> award.getAmount();
        } else {
            return Scripting.create(award.getExpression(), Constants.VAR_RATING_AWARD_PREV_RATING);
        }
    }
}

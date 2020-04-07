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

package io.github.oasis.engine.processors;

import io.github.oasis.engine.model.ID;
import io.github.oasis.engine.model.RuleContext;
import io.github.oasis.engine.rules.RatingRule;
import io.github.oasis.engine.rules.signals.AbstractRatingSignal;
import io.github.oasis.engine.rules.signals.RatingChangedSignal;
import io.github.oasis.engine.rules.signals.RatingPointsSignal;
import io.github.oasis.engine.storage.Db;
import io.github.oasis.engine.storage.DbContext;
import io.github.oasis.engine.storage.Mapped;
import io.github.oasis.engine.utils.Constants;
import io.github.oasis.engine.utils.Utils;
import io.github.oasis.model.Event;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static io.github.oasis.engine.utils.Numbers.asInt;

/**
 * @author Isuru Weerarathna
 */
public class RatingProcessor extends AbstractProcessor<RatingRule, AbstractRatingSignal> {

    public RatingProcessor(Db dbPool, RuleContext<RatingRule> ruleCtx) {
        super(dbPool, ruleCtx);
    }

    @Override
    protected void beforeEmit(AbstractRatingSignal signal, Event event, RatingRule rule, DbContext db) {
    }

    @Override
    public List<AbstractRatingSignal> process(Event event, RatingRule rule, DbContext db) {
        Mapped ratingsMap = db.MAP(ID.getGameRatingKey(event.getGameId(), rule.getId()));
        String subRatingKey = String.valueOf(event.getUser());
        String userCurrentRating = ratingsMap.getValue(subRatingKey);
        int currRating = rule.getDefaultRating();
        if (userCurrentRating != null) {
            String[] parts = userCurrentRating.split(":");
            currRating = asInt(parts[0]);
        }

        for (RatingRule.Rating rating : rule.getRatings()) {
            int newRating = rating.getRating();
            if (rating.getCriteria().test(event) && newRating != currRating) {
                long ts = event.getTimestamp();
                String id = event.getExternalId();
                BigDecimal score = deriveAwardedPoints(event, currRating, rating).setScale(Constants.SCALE, BigDecimal.ROUND_HALF_UP);
                ratingsMap.setValue(subRatingKey, String.format("%d:%d:%s", newRating, ts, id));
                Event copiedEvent = Utils.deepClone(event);
                return Arrays.asList(
                        new RatingChangedSignal(rule.getId(), currRating, newRating, ts, event),
                        new RatingPointsSignal(rule.getId(), rating.getPointId(), newRating, score, copiedEvent)
                );
            }
        }
        return null;
    }

    private BigDecimal deriveAwardedPoints(Event event, int prevRating, RatingRule.Rating rating) {
        if (rating.getPointAwards() != null) {
            return rating.getPointAwards().apply(event, prevRating);
        }
        return BigDecimal.ZERO;
    }
}

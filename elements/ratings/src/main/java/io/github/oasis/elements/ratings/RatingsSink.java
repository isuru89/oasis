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

import io.github.oasis.core.EventScope;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.DbContext;
import io.github.oasis.core.external.Sorted;
import io.github.oasis.core.context.ExecutionContext;
import io.github.oasis.core.ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Isuru Weerarathna
 */
public class RatingsSink extends AbstractSink {

    private static final Logger LOG = LoggerFactory.getLogger(RatingsSink.class);

    public RatingsSink(Db dbPool) {
        super(dbPool);
    }

    @Override
    public void consume(Signal ratingSignal, AbstractRule ratingRule, ExecutionContext context) {
        try (DbContext db = dbPool.createContext()) {
            RatingChangedSignal signal = (RatingChangedSignal) ratingSignal;

            EventScope eventScope = ratingSignal.getEventScope();
            int gameId = eventScope.getGameId();
            long userId = eventScope.getUserId();
            Sorted sorted = db.SORTED(ID.getGameUserRatingsLog(gameId, userId));

            String member = signal.getRuleId() + COLON
                    + signal.getPreviousRating() + COLON
                    + signal.getCurrentRating() + COLON
                    + signal.getChangedEvent();
            sorted.add(member, signal.getOccurredTimestamp());

        } catch (IOException e) {
            LOG.error("Error persisting rating metrics!", e);
        }
    }
}

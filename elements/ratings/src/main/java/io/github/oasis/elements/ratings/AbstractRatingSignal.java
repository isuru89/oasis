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
import io.github.oasis.core.elements.AbstractSink;
import io.github.oasis.core.elements.Signal;
import lombok.ToString;

/**
 * @author Isuru Weerarathna
 */
@ToString
public abstract class AbstractRatingSignal extends Signal {

    private final int currentRating;

    public AbstractRatingSignal(String ruleId, EventScope eventScope, long occurredTs, int currentRating) {
        super(ruleId, eventScope, occurredTs);
        this.currentRating = currentRating;
    }

    @Override
    public Class<? extends AbstractSink> sinkHandler() {
        return RatingsSink.class;
    }

    public int getCurrentRating() {
        return currentRating;
    }

}

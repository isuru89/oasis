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

package io.github.oasis.engine.rules.signals;

import io.github.oasis.model.EventScope;

/**
 * @author Isuru Weerarathna
 */
public abstract class AbstractRatingSignal extends Signal {

    private int currentRating;

    public AbstractRatingSignal(String ruleId, EventScope eventScope, int currentRating) {
        super(ruleId, eventScope);
        this.currentRating = currentRating;
    }

    public int getCurrentRating() {
        return currentRating;
    }

    @Override
    public int compareTo(Signal o) {
        return 0;
    }
}

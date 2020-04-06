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

import java.util.Comparator;
import java.util.Objects;

/**
 * Indicates a challenge has been completed and no more winners accepted.
 *
 * @author Isuru Weerarathna
 */
public class ChallengeOverSignal extends AbstractChallengeSignal {

    private CompletionType completionType;

    public ChallengeOverSignal(String ruleId, EventScope eventScope, CompletionType completionType) {
        super(ruleId, eventScope);
        this.completionType = completionType;
    }

    public CompletionType getCompletionType() {
        return completionType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeOverSignal that = (ChallengeOverSignal) o;
        return getRuleId().equals(((ChallengeOverSignal) o).getRuleId()) && completionType == that.completionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), completionType);
    }

    @Override
    public int compareTo(Signal o) {
        if (o instanceof ChallengeOverSignal) {
            return Comparator.comparing(ChallengeOverSignal::getRuleId)
                        .thenComparing(Signal::getEventScope)
                        .thenComparing(ChallengeOverSignal::getCompletionType)
                        .compare(this, (ChallengeOverSignal) o);
        }
        return -1;
    }

    public enum CompletionType {
        TIME_EXPIRED,
        ALL_WINNERS_FOUND
    }

}

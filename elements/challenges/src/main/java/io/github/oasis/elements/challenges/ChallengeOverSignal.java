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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.EventScope;
import io.github.oasis.core.elements.Signal;

import java.util.Comparator;
import java.util.Objects;

/**
 * Indicates a challenge has been completed and no more winners accepted.
 *
 * @author Isuru Weerarathna
 */
public class ChallengeOverSignal extends AbstractChallengeSignal {

    private CompletionReason completionReason;

    public ChallengeOverSignal(String ruleId, EventScope eventScope, long completedTime, CompletionReason completionReason) {
        super(ruleId, eventScope, completedTime);
        this.completionReason = completionReason;
    }

    public CompletionReason getCompletionType() {
        return completionReason;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChallengeOverSignal that = (ChallengeOverSignal) o;
        return getRuleId().equals(((ChallengeOverSignal) o).getRuleId()) && completionReason == that.completionReason;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRuleId(), completionReason);
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

    @Override
    public String toString() {
        return "ChallengeOverSignal{" +
                "ruleId=" + getRuleId() + ", " +
                "completionType=" + completionReason +
                '}';
    }

    public enum CompletionReason {
        TIME_EXPIRED,
        ALL_WINNERS_FOUND
    }

}

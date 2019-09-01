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

package io.github.oasis.game.persist;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;

public class NoneOutputHandlerTest {

    @Test
    public void testHandler() {
        {
            NoneOutputHandler.NoneHandler mock = Mockito.mock(NoneOutputHandler.NoneHandler.class);
            NoneOutputHandler outputHandler = new NoneOutputHandler(mock);

            Assertions.assertThat(outputHandler.getBadgeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getChallengeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getMilestoneHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getPointsHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getRatingsHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);

            outputHandler.getBadgeHandler().badgeReceived(null);
            outputHandler.getChallengeHandler().addChallengeWinner(null);
            outputHandler.getMilestoneHandler()
                    .addMilestoneCurrState(null, null, 0, 0L);
            outputHandler.getMilestoneHandler().addMilestoneCurrState(null, null, 0.0, 0.0);
            outputHandler.getMilestoneHandler().milestoneReached(null);
            outputHandler.getPointsHandler().pointsScored(null);
            outputHandler.getRatingsHandler().handleRatingChange(null);

            Mockito.verify(mock).addChallengeWinner(null);
            Mockito.verify(mock).badgeReceived(null);
            Mockito.verify(mock).addMilestoneCurrState(null, null, 0, 0L);
            Mockito.verify(mock).addMilestoneCurrState(null, null, 0.0, 0.0);
            Mockito.verify(mock).milestoneReached(null);
            Mockito.verify(mock).pointsScored(null);
            Mockito.verify(mock).handleRatingChange(null);
        }

        {
            NoneOutputHandler outputHandler = new NoneOutputHandler();

            Assertions.assertThat(outputHandler.getBadgeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getChallengeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getMilestoneHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getPointsHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getRatingsHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);

            outputHandler.getBadgeHandler().badgeReceived(null);
            outputHandler.getMilestoneHandler().milestoneReached(null);
            outputHandler.getChallengeHandler().addChallengeWinner(null);
            outputHandler.getMilestoneHandler()
                    .addMilestoneCurrState(null, null, 0, 0L);
            outputHandler.getMilestoneHandler().addMilestoneCurrState(null, null, 0.0, 0.0);
            outputHandler.getPointsHandler().pointsScored(null);
            outputHandler.getRatingsHandler().handleRatingChange(null);
        }
    }

}

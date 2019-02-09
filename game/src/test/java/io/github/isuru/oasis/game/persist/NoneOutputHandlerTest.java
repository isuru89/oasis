package io.github.isuru.oasis.game.persist;

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
            Assertions.assertThat(outputHandler.getStatesHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);

            outputHandler.getBadgeHandler().badgeReceived(null);
            outputHandler.getChallengeHandler().addChallengeWinner(null);
            outputHandler.getMilestoneHandler()
                    .addMilestoneCurrState(null, null, 0, 0L);
            outputHandler.getMilestoneHandler().addMilestoneCurrState(null, null, 0.0, 0.0);
            outputHandler.getMilestoneHandler().milestoneReached(null);
            outputHandler.getPointsHandler().pointsScored(null);
            outputHandler.getStatesHandler().handleStateChange(null);

            Mockito.verify(mock).addChallengeWinner(null);
            Mockito.verify(mock).badgeReceived(null);
            Mockito.verify(mock).addMilestoneCurrState(null, null, 0, 0L);
            Mockito.verify(mock).addMilestoneCurrState(null, null, 0.0, 0.0);
            Mockito.verify(mock).milestoneReached(null);
            Mockito.verify(mock).pointsScored(null);
            Mockito.verify(mock).handleStateChange(null);
        }

        {
            NoneOutputHandler outputHandler = new NoneOutputHandler();

            Assertions.assertThat(outputHandler.getBadgeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getChallengeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getMilestoneHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getPointsHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
            Assertions.assertThat(outputHandler.getStatesHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);

            outputHandler.getBadgeHandler().badgeReceived(null);
            outputHandler.getMilestoneHandler().milestoneReached(null);
            outputHandler.getChallengeHandler().addChallengeWinner(null);
            outputHandler.getMilestoneHandler()
                    .addMilestoneCurrState(null, null, 0, 0L);
            outputHandler.getMilestoneHandler().addMilestoneCurrState(null, null, 0.0, 0.0);
            outputHandler.getPointsHandler().pointsScored(null);
            outputHandler.getStatesHandler().handleStateChange(null);
        }
    }

}

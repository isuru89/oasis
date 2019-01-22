package io.github.isuru.oasis.game.persist;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class NoneOutputHandlerTest {

    @Test
    public void testHandler() {
        NoneOutputHandler outputHandler = new NoneOutputHandler();

        Assertions.assertThat(outputHandler.getBadgeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
        Assertions.assertThat(outputHandler.getChallengeHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
        Assertions.assertThat(outputHandler.getMilestoneHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
        Assertions.assertThat(outputHandler.getPointsHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
        Assertions.assertThat(outputHandler.getStatesHandler()).isInstanceOf(NoneOutputHandler.NoneHandler.class);
    }

}

package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.events.ChallengeEvent;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IChallengeHandler extends Serializable {

    void addChallengeWinner(ChallengeEvent challengeEvent);

}

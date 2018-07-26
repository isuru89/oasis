package io.github.isuru.oasis.model.handlers;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IOutputHandler extends Serializable {

    IPointHandler getPointsHandler();

    IBadgeHandler getBadgeHandler();

    IMilestoneHandler getMilestoneHandler();

    default IChallengeHandler getChallengeHandler() {
        return null;
    }
}

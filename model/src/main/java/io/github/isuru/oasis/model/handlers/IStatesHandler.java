package io.github.isuru.oasis.model.handlers;

import java.io.Serializable;

public interface IStatesHandler extends Serializable {

    void handleStateChange(RatingNotification stateNotification);

}

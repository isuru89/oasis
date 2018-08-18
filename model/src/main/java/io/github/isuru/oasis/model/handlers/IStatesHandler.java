package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.events.OStateEvent;

import java.io.Serializable;

public interface IStatesHandler extends Serializable {

    void handleStateChange(OStateNotification stateNotification);

}

package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.events.RaceEvent;

import java.io.Serializable;

public interface IRaceHandler extends Serializable {

    void addRaceWinner(RaceEvent raceEvent);

}

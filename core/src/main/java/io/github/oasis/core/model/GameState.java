package io.github.oasis.core.model;

import java.util.Arrays;
import java.util.Optional;

public enum GameState {

    DRAFT(1),

    CREATED(2),

    STARTED(5),

    PAUSED(7),

    STOPPED(9),

    UNKNOWN(-1);

    private final int id;

    GameState(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public static Optional<GameState> of(int id) {
        return Arrays.stream(GameState.values()).filter(it -> it.getId() == id).findFirst();
    }
}

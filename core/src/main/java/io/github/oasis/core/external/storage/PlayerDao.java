package io.github.oasis.core.external.storage;

import io.github.oasis.core.model.PlayerObject;

public interface PlayerDao {

    PlayerObject readPlayer(long userId);
    PlayerObject readPlayer(String email);
    PlayerObject addPlayer(PlayerObject newUser);
    boolean existsPlayer(String email);
    boolean existsPlayer(long userId);
    PlayerObject updatePlayer(long userId, PlayerObject updatedUser);
    PlayerObject deletePlayer(long userId);


}

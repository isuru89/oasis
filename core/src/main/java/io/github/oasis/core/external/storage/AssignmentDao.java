package io.github.oasis.core.external.storage;

import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;

import java.util.List;

public interface AssignmentDao {

    void removePlayerFromTeam(long userId, int gameId, int teamId);
    void addPlayerToTeam(long userId, int gameId, int teamId);
    List<TeamObject> getPlayerTeams(long userId);
    List<PlayerObject> getTeamPlayers(int teamId);


}

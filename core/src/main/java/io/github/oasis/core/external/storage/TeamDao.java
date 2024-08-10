package io.github.oasis.core.external.storage;

import io.github.oasis.core.TeamMetadata;
import io.github.oasis.core.external.PaginatedResult;
import io.github.oasis.core.model.TeamObject;

public interface TeamDao {


    TeamObject addTeam(TeamObject teamObject);
    TeamObject readTeam(int teamId);
    TeamObject readTeam(String teamName);
    TeamObject updateTeam(int teamId, TeamObject updatedTeam);
    boolean existsTeam(String teamName);
    boolean existsTeam(int teamId);
    PaginatedResult<TeamMetadata> searchTeam(String teamName, String offset, int maxRecords);


}

package io.github.oasis.core.services.api.infrastructure.beans.jpa;

import io.github.oasis.core.external.storage.ElementDao;
import io.github.oasis.core.external.storage.EventSourceDao;
import io.github.oasis.core.external.storage.GameDao;
import io.github.oasis.core.external.storage.OasisDao;
import io.github.oasis.core.external.storage.PlayerDao;
import io.github.oasis.core.external.storage.RankDao;
import io.github.oasis.core.external.storage.AssignmentDao;
import io.github.oasis.core.external.storage.TeamDao;
import org.springframework.stereotype.Component;

@Component("jpa")
public class JpaBasedDao implements OasisDao {

    private final JpaElementDao elementDao;
    private final JpaTeamDao teamDao;
    private final JpaGameDao gameDao;
    private final JpaPlayerDao playerDao;
    private final JpaRankDao rankDao;
    private final JpaAssignmentDao registrationDao;
    private final JpaEventSourceDao eventSourceDao;

    JpaBasedDao(JpaElementDao elementDao,
                JpaTeamDao teamDao,
                JpaGameDao gameDao,
                JpaPlayerDao playerDao,
                JpaRankDao rankDao,
                JpaAssignmentDao registrationDao,
                JpaEventSourceDao eventSourceDao) {
        this.elementDao = elementDao;
        this.teamDao = teamDao;
        this.gameDao = gameDao;
        this.playerDao = playerDao;
        this.rankDao = rankDao;
        this.registrationDao = registrationDao;
        this.eventSourceDao = eventSourceDao;
    }

    @Override
    public GameDao getGameDao() {
        return gameDao;
    }

    @Override
    public EventSourceDao getEventSourceDao() {
        return eventSourceDao;
    }

    @Override
    public PlayerDao getPlayerDao() {
        return playerDao;
    }

    @Override
    public TeamDao getTeamDao() {
        return teamDao;
    }

    @Override
    public AssignmentDao getAssignmentDao() {
        return registrationDao;
    }

    @Override
    public ElementDao getElementDao() {
        return elementDao;
    }

    @Override
    public RankDao getRankDao() {
        return rankDao;
    }

}

package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.injector.BaseConsumer;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.defs.RaceDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.util.*;

abstract class BaseScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(BaseScheduler.class);

    List<RaceDef> readRaces(int gameId, IOasisDao dao, String timePeriod) throws Exception {
        List<DefWrapper> defWrappers = dao.getDefinitionDao().listDefinitionsOfGame(gameId, OasisDefinition.RACE.getTypeId());
        List<RaceDef> raceDefList = new LinkedList<>();
        for (DefWrapper wrapper : defWrappers) {
            try {
                RaceDef raceDef = BaseConsumer.MAPPER.readValue(wrapper.getContent(), RaceDef.class);
                if (timePeriod.equalsIgnoreCase(raceDef.getTimewindow())) {
                    raceDefList.add(raceDef);
                }
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        return raceDefList;
    }

    boolean validateTeamAndScope(RaceDef raceDef, IOasisDao dao) throws Exception {
        Integer maxN = Math.max(raceDef.getTop() == null ? 0 : raceDef.getTop(),
                raceDef.getBottom() == null ? 0 : raceDef.getBottom());

        if ("teamScope".equalsIgnoreCase(raceDef.getFromScope())) {
            return true;
        } else if ("team".equalsIgnoreCase(raceDef.getFromScope())) {
            Map<String, Object> map = new HashMap<>();
            map.put("teamId", 0);
            map.put("offset", 0);
            map.put("limit", maxN);
            Iterable<Map<String, Object>> usersIt = dao.executeQuery("profile/listUsersOfTeam", map);
            Iterator<Map<String, Object>> it = usersIt.iterator();
            int size = 0;
            while (it.hasNext()) {
                size++;
                it.next();
            }
            if (size < maxN) {
                // not enough members in team
                // @TODO handle one team member vs. many team members
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    protected abstract Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId);

}

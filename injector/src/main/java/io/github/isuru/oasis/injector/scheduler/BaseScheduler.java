package io.github.isuru.oasis.injector.scheduler;

import io.github.isuru.oasis.injector.BaseConsumer;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.DefWrapper;
import io.github.isuru.oasis.model.defs.OasisDefinition;
import io.github.isuru.oasis.model.defs.RaceDef;

import java.io.IOException;
import java.time.ZoneId;
import java.util.LinkedList;
import java.util.List;

abstract class BaseScheduler {

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
                e.printStackTrace();
            }
        }
        return raceDefList;
    }

    protected abstract Pair<Long, Long> deriveTimeRange(long ms, ZoneId zoneId);

}

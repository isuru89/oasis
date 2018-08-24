package io.github.isuru.oasis.services.utils.local.sinks;

import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author iweerarathna
 */
public class LocalSinks {

    static final String SQ_BADGES = "badges";
    static final String SQ_POINTS = "points";
    static final String SQ_MILESTONES = "milestones";
    static final String SQ_MILESTONE_STATES = "milestone-states";
    static final String SQ_STATES = "states";
    static final String SQ_CHALLENGES = "challenges";

    private final List<BaseLocalSink> localSinks = new LinkedList<>();

    public void stop() {
        for (BaseLocalSink localSink : localSinks) {
            localSink.stop();
        }
    }

    public static LocalSinks applySinks(ExecutorService service, DbSink dbSink, IOasisDao dao, long gameId) {
        LocalSinks sinks = new LocalSinks();
        sinks.localSinks.add(new PointsSink(dao, gameId));
        sinks.localSinks.add(new BadgeSink(dao, gameId));
        sinks.localSinks.add(new MilestoneSink(dao, gameId));
        sinks.localSinks.add(new MilestoneStateSink(dao, gameId));
        sinks.localSinks.add(new StateSink(dao, gameId));
        sinks.localSinks.add(new ChallengeSink(dao, gameId));

        for (BaseLocalSink localSink : sinks.localSinks) {
            service.submit(localSink);
        }
        return sinks;
    }

}

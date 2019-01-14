package io.github.isuru.oasis.services.services.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.NotificationUtils;
import io.github.isuru.oasis.services.utils.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
@Component
class LocalChallengeProcessor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LocalChallengeProcessor.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final BlockingQueue<Event> events = new LinkedBlockingDeque<>();
    private final Map<Long, ChallengeDef> challengeDefMap = new ConcurrentHashMap<>();
    private final Map<Long, OasisSink> sinkMap = new ConcurrentHashMap<>();
    private boolean stop = false;

    private final IOasisDao dao;

    @Autowired
    public LocalChallengeProcessor(IOasisDao dao) {
        this.dao = dao;
    }

    public boolean containChallenge(long id) {
        return challengeDefMap.containsKey(id);
    }

    public void submitChallenge(ChallengeDef challengeDef, OasisSink sink) {
        sinkMap.put(challengeDef.getId(), sink);
        challengeDefMap.put(challengeDef.getId(), challengeDef);
    }

    public void stopChallenge(long challengeId) throws Exception {
        dao.executeCommand("jobs/stopJobByDefId", Maps.create("defId", challengeId));

        challengeDefMap.remove(challengeId);
    }

    void stopChallengesOfGame(long gameId) throws Exception {
        List<Long> ids = new LinkedList<>();
        for (Map.Entry<Long, ChallengeDef> entry : challengeDefMap.entrySet()) {
            Long gid = entry.getValue().getGameId();
            if (gameId == gid) {
                ids.add(gameId);
            }
        }

        for (Long id : ids) {
            stopChallenge(id);
        }
    }

    public void submitEvent(Map<String, Object> event) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.putAll(event);
        events.add(jsonEvent);
    }

    private void processEvent(Event event) {
        List<Long> toRemove = new LinkedList<>();

        for (Map.Entry<Long, ChallengeDef> entry : challengeDefMap.entrySet()) {
            ChallengeDef challengeDef = entry.getValue();
            ChallengeFilterResult result = isChallengeSatisfied(event, challengeDef);
            if (result.isSatisfied()) {
                OasisSink oasisSink = sinkMap.get(entry.getKey());
                if (oasisSink != null) {
                    // send out a winner
                    ChallengeEvent challengeEvent = new ChallengeEvent(event, challengeDef);
                    Map<String, Object> winnerInfo = NotificationUtils.mapChallenge(challengeEvent);
                    try {
                        oasisSink.createChallengeSink().invoke(mapper.writeValueAsString(winnerInfo), null);
                    } catch (Exception e) {
                        LOG.error("Error sending challenge winner notification!", e);
                    }
                } else {
                    LOG.warn("Challenge winner will not be notified, " +
                            "since the game might have already stopped or completed!");
                }
            }

            if (!result.isContinue()) {
                // remove challenge
                toRemove.add(entry.getKey());
            }
        }

        if (!toRemove.isEmpty()) {
            toRemove.forEach(id -> {
                try {
                    stopChallenge(id);
                } catch (Exception e) {
                    LOG.error("Error stopping challenge '" + id + "'!", e);
                }
            });
        }
    }

    private ChallengeFilterResult isChallengeSatisfied(Event event, ChallengeDef challengeDef) {
        return new ChallengeFilterResult(false, true);
    }

    @Override
    public void run() {
        while (!stop) {

            try {
                Event event = events.poll(5, TimeUnit.SECONDS);
                if (event != null) {
                    if (event instanceof LocalEndEvent) {
                        break;
                    }

                    processEvent(event);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    void setStop() {
        events.add(new LocalEndEvent());
        stop = true;
    }

    private static class ChallengeFilterResult {
        private final boolean satisfied;
        private final boolean isContinue;

        private ChallengeFilterResult(boolean satisfied, boolean isContinue) {
            this.satisfied = satisfied;
            this.isContinue = isContinue;
        }

        public boolean isSatisfied() {
            return satisfied;
        }

        public boolean isContinue() {
            return isContinue;
        }
    }
}

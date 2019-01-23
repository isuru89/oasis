package io.github.isuru.oasis.services.services.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.game.persist.OasisSink;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.defs.ChallengeDef;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.events.JsonEvent;
import io.github.isuru.oasis.model.handlers.NotificationUtils;
import io.github.isuru.oasis.services.model.SubmittedJob;
import io.github.isuru.oasis.services.services.IJobService;
import io.github.isuru.oasis.services.utils.Pojos;
import org.apache.flink.shaded.netty4.io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;
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

    private static final int TIMEOUT = 5;

    private final ObjectMapper mapper = new ObjectMapper();

    private final BlockingQueue<Event> events = new LinkedBlockingDeque<>();
    private final Map<Long, ChallengeDef> challengeDefMap = new ConcurrentHashMap<>();
    private final Map<Long, ChallengeCheck> challengeCheckMap = new ConcurrentHashMap<>();
    private final Map<Long, OasisSink> sinkMap = new ConcurrentHashMap<>();
    private boolean stop = false;
    private final Set<String> refEvents = new ConcurrentSet<>();

    private final IJobService jobService;

    @Autowired
    LocalChallengeProcessor(IJobService jobService) {
        this.jobService = jobService;
    }

    void submitChallenge(ChallengeDef challengeDef, OasisSink sink) throws Exception {
        SubmittedJob job = readJobState(challengeDef);
        ChallengeCheck challengeCheck = Pojos.deserialize(job.getStateData());
        if (challengeCheck == null) {
            challengeCheck = new ChallengeCheck(challengeDef);
        }

        refEvents.addAll(challengeDef.getForEvents());

        sinkMap.put(challengeDef.getId(), sink);
        challengeCheckMap.put(challengeDef.getId(), challengeCheck);
        challengeDefMap.put(challengeDef.getId(), challengeDef);
    }

    private void stopChallenge(long challengeId) throws Exception {
        if (jobService.stopJobByDef(challengeId)) {

            challengeDefMap.remove(challengeId);
            challengeCheckMap.remove(challengeId);
            sinkMap.remove(challengeId);
        } else {
            throw new IllegalStateException("Cannot stop challenge #" + challengeId + " in db!");
        }
    }

    void stopChallenge(ChallengeDef challengeDef) throws Exception {
        long challengeId = challengeDef.getId();

        refEvents.removeAll(challengeDef.getForEvents());

        stopChallenge(challengeId);
    }

    void stopChallengesOfGame(long gameId) throws Exception {
        List<ChallengeDef> defs = new LinkedList<>();
        for (Map.Entry<Long, ChallengeDef> entry : challengeDefMap.entrySet()) {
            Long gid = entry.getValue().getGameId();
            if (gameId == gid) {
                defs.add(entry.getValue());
            }
        }

        for (ChallengeDef def : defs) {
            stopChallenge(def);
        }
    }

    public void submitEvent(Map<String, Object> event) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.putAll(event);
        events.add(jsonEvent);
    }

    private synchronized void processEvent(Event event) {
        // unnecessary events will be ignored...
        if (!refEvents.contains(event.getEventType())) {
            return;
        }

        List<ChallengeDef> toRemove = new LinkedList<>();

        for (Map.Entry<Long, ChallengeDef> entry : challengeDefMap.entrySet()) {
            long challengeId = entry.getKey();
            ChallengeDef challenge = entry.getValue();
            ChallengeCheck challengeChecker = challengeCheckMap.get(challengeId);

            ChallengeCheck.ChallengeFilterResult result = challengeChecker.check(event);
            if (result.isSatisfied()) {
                OasisSink oasisSink = sinkMap.get(challengeId);
                if (oasisSink != null) {
                    // send out a winner
                    ChallengeEvent challengeEvent = new ChallengeEvent(event, challenge);
                    Map<String, Object> winnerInfo = NotificationUtils.mapChallenge(challengeEvent);
                    try {
                        // persist checker change to db
                        byte[] dataState = Pojos.serialize(challengeChecker);
                        if (jobService.updateJobState(challengeId, dataState)) {
                            // when success, notify sink
                            oasisSink.createChallengeSink().invoke(mapper.writeValueAsString(winnerInfo), null);
                        } else {
                            // @TODO do something when we can't save state to db
                            LOG.error("Cannot update job state in persistent storage.");
                        }

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
                toRemove.add(challenge);
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

    private SubmittedJob readJobState(ChallengeDef challengeDef) throws Exception {
        // add a job to db, if not exists
        Long id = challengeDef.getId();
        SubmittedJob submittedJob = jobService.readJob(id);
        if (submittedJob != null) {
            return submittedJob;
        } else {
            SubmittedJob job = new SubmittedJob();
            job.setDefId(id);
            job.setJobId(UUID.randomUUID().toString());
            job.setToBeFinishedAt(challengeDef.getExpireAfter());
            job.setActive(true);

            if (jobService.submitJob(job) > 0) {
                return jobService.readJob(id);
            } else {
                throw new IOException("Cannot add a job for the challenge '" + id + "'!");
            }
        }
    }

    @Override
    public void run() {
        while (!stop) {

            try {
                Event event = events.poll(TIMEOUT, TimeUnit.SECONDS);
                if (event != null) {
                    if (event instanceof LocalEndEvent) {
                        break;
                    }

                    processEvent(event);
                }

            } catch (InterruptedException e) {
                LOG.error(e.getMessage(), e);
            }
        }
        LOG.debug("Local challenge processor terminated. [Stop: {}]", stop);
    }

    void setStop() {
        LOG.debug("Stopping signal sent for local challenge processor...");
        events.add(new LocalEndEvent());
        stop = true;
    }

}

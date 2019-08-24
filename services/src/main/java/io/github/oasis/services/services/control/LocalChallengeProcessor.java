/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.services.control;

import io.github.oasis.model.Constants;
import io.github.oasis.model.Event;
import io.github.oasis.model.defs.ChallengeDef;
import io.github.oasis.model.events.ChallengeEvent;
import io.github.oasis.model.events.JsonEvent;
import io.github.oasis.services.model.IEventDispatcher;
import io.github.oasis.services.model.SubmittedJob;
import io.github.oasis.services.services.IJobService;
import io.github.oasis.services.utils.Pojos;
import org.apache.flink.shaded.netty4.io.netty.util.internal.ConcurrentSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
class LocalChallengeProcessor implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LocalChallengeProcessor.class);

    private static final String FIELD_TOKEN = "source.token";

    private static final int TIMEOUT = 5;

    private final BlockingQueue<Event> events = new LinkedBlockingDeque<>();
    private final Map<Long, ChallengeDef> challengeDefMap = new ConcurrentHashMap<>();
    private final Map<Long, ChallengeCheck> challengeCheckMap = new ConcurrentHashMap<>();
    private boolean stop = false;
    private boolean stopped = false;
    private final Set<String> refEvents = new ConcurrentSet<>();

    private final IJobService jobService;

    private final IEventDispatcher eventDispatcher;

    LocalChallengeProcessor(IJobService jobService, IEventDispatcher eventDispatcher) {
        this.jobService = jobService;
        this.eventDispatcher = eventDispatcher;
    }

    void submitChallenge(ChallengeDef challengeDef) throws Exception {
        SubmittedJob job = readJobState(challengeDef);
        ChallengeCheck challengeCheck = Pojos.deserialize(job.getStateData());
        if (challengeCheck == null) {
            challengeCheck = new ChallengeCheck(challengeDef);
        }

        refEvents.addAll(challengeDef.getForEvents());

        challengeCheckMap.put(challengeDef.getId(), challengeCheck);
        challengeDefMap.put(challengeDef.getId(), challengeDef);
    }

    private void stopChallenge(long challengeId) throws Exception {
        if (jobService.stopJobByDef(challengeId)) {

            challengeDefMap.remove(challengeId);
            challengeCheckMap.remove(challengeId);
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

    public void submitEvent(String token, Map<String, Object> event) {
        JsonEvent jsonEvent = new JsonEvent();
        jsonEvent.putAll(event);
        jsonEvent.setFieldValue(FIELD_TOKEN, token);
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
                // send out a winner
                ChallengeEvent challengeEvent = new ChallengeEvent(event, challenge);
                challengeEvent.setFieldValue(ChallengeEvent.KEY_WIN_NO, result.getWinNumber());

                // send challenge winner as event
                Map<String, Object> winnerEvent = mapChallenge(challengeEvent);
                try {
                    // persist checker change to db
                    byte[] dataState = Pojos.serialize(challengeChecker);
                    if (jobService.updateJobState(challengeId, dataState)) {
                        // when success, notify as an event
                        eventDispatcher.dispatch(challenge.getGameId(), winnerEvent);
                    } else {
                        // @TODO do something when we can't save state to db
                        LOG.error("Cannot update job state in persistent storage.");
                    }

                } catch (Exception e) {
                    LOG.error("Error sending challenge winner notification!", e);
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

    static Map<String, Object> mapChallenge(ChallengeEvent value) {
        Map<String, Object> data = new HashMap<>();
        data.put(Constants.FIELD_TEAM, value.getTeam());
        data.put(Constants.FIELD_SCOPE, value.getTeamScope());
        data.put(Constants.FIELD_USER, value.getUser());
        data.put(Constants.FIELD_ID, value.getExternalId());
        data.put(Constants.FIELD_TIMESTAMP, value.getTimestamp());
        data.put(Constants.FIELD_SOURCE, value.getSource());
        data.put(Constants.FIELD_GAME_ID, value.getGameId());
        data.put(ChallengeEvent.KEY_DEF_ID, value.getChallengeId());
        data.put(ChallengeEvent.KEY_POINTS, value.getPoints());
        data.put(ChallengeEvent.KEY_WIN_NO, value.getWinNo());
        return data;
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
        LOG.warn("Local challenge processor terminated. [Stop: {}]", stop);
        stopped = true;
    }

    void setStop() {
        LOG.warn("Stopping signal sent for local challenge processor...");
        events.add(new LocalEndEvent());
        stop = true;
    }

    boolean isStopped() {
        return stopped;
    }
}

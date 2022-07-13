/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.services.feeds.services;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.oasis.core.Game;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.model.EventSource;
import io.github.oasis.core.model.PlayerObject;
import io.github.oasis.core.model.TeamObject;

import java.time.Duration;

import static io.github.oasis.services.feeds.Constants.*;

public class InMemoryCachedDataService implements DataService {


    private final DataService delegate;

    private LoadingCache<Integer, Game> cachedGames;
    private LoadingCache<Integer, EventSource> cachedEventSources;
    private LoadingCache<Long, TeamObject> cachedTeams;
    private LoadingCache<Long, PlayerObject> cachedPlayers;

    public InMemoryCachedDataService(DataService delegate) {
        this.delegate = delegate;
    }

    @Override
    public void init(OasisConfigs configs) {
        delegate.init(configs);

        int expireAfter = configs.getInt(OASIS_CACHE_CONFIGS_EXPIRE_AFTER, DEF_EXPIRE_DURATION);
        Duration duration = Duration.ofSeconds(expireAfter);

        cachedGames = Caffeine.newBuilder()
                .maximumSize(readCacheSize(configs, GAMES, DEF_GAME_CACHE_SIZE))
                .expireAfterWrite(duration)
                .build(delegate::getGame);
        cachedEventSources = Caffeine.newBuilder()
                .maximumSize(readCacheSize(configs, EVENT_SOURCES, DEF_SOURCE_CACHE_SIZE))
                .expireAfterWrite(duration)
                .build(delegate::getEventSource);
        cachedTeams = Caffeine.newBuilder()
                .maximumSize(readCacheSize(configs, TEAMS, DEF_TEAMS_CACHE_SIZE))
                .expireAfterWrite(duration)
                .build(delegate::getTeam);
        cachedPlayers = Caffeine.newBuilder()
                .maximumSize(readCacheSize(configs, PLAYERS, DEF_PLAYERS_CACHE_SIZE))
                .expireAfterWrite(duration)
                .build(delegate::getPlayer);
    }

    private int readCacheSize(OasisConfigs configs, String key, int defSize) {
        return configs.getInt(OASIS_CACHE_CONFIGS_MAX_ENTRIES + key, defSize);
    }

    @Override
    public PlayerObject getPlayer(long playerId) {
        return cachedPlayers.get(playerId);
    }

    @Override
    public TeamObject getTeam(long teamId) {
        return cachedTeams.get(teamId);
    }

    @Override
    public Game getGame(int gameId) {
        return cachedGames.get(gameId);
    }

    @Override
    public EventSource getEventSource(int sourceId) {
        return cachedEventSources.get(sourceId);
    }
}

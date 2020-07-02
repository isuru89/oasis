/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.engine.ext;


import akka.actor.Extension;
import io.github.oasis.core.Event;
import io.github.oasis.core.elements.AbstractRule;

import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Allows accessing rules by actors.
 *
 * @author Isuru Weerarathna
 */
public final class RulesImpl implements Extension {

    private final ConcurrentHashMap<Integer, GameRules> ruleReferences = new ConcurrentHashMap<>();

    public GameRules forGame(int gameId) {
        return ruleReferences.get(gameId);
    }

    public GameRules createIfNotExists(int gameId) {
        return ruleReferences.computeIfAbsent(gameId, newGameId -> new GameRules());
    }

    public static class GameRules {

        private final Map<String, AbstractRule> ruleMap = new ConcurrentHashMap<>();

        public void addRule(AbstractRule rule) {
            ruleMap.put(rule.getId(), rule);
        }

        public void updateRule(AbstractRule rule) {
            addRule(rule);
        }

        public void removeRule(String ruleId) {
            ruleMap.remove(ruleId);
        }

        public void activateRule(String ruleId) {
            AbstractRule rule = ruleMap.get(ruleId);
            if (Objects.nonNull(rule)) {
                rule.setActive(true);
            }
        }

        public void deactivateRule(String ruleId) {
            AbstractRule rule = ruleMap.get(ruleId);
            if (Objects.nonNull(rule)) {
                rule.setActive(false);
            }
        }

        public Iterator<AbstractRule> getAllRulesForEvent(Event event) {
            return ruleMap.values().stream().filter(AbstractRule::isActive).iterator();
        }
    }
}

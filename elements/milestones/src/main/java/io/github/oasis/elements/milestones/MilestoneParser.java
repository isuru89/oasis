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

package io.github.oasis.elements.milestones;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractElementParser;
import io.github.oasis.core.elements.Scripting;
import io.github.oasis.core.events.BasePointEvent;
import io.github.oasis.core.external.messages.PersistedDef;

import java.math.BigDecimal;
import java.util.stream.Collectors;

import static io.github.oasis.core.VariableNames.CONTEXT_VAR;
import static io.github.oasis.core.VariableNames.RULE_VAR;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneParser extends AbstractElementParser {
    @Override
    public MilestoneDef parse(PersistedDef persistedObj) {
        return loadFrom(persistedObj, MilestoneDef.class);
    }

    @Override
    public MilestoneRule convert(AbstractDef definition) {
        if (definition instanceof MilestoneDef) {
            return toRule((MilestoneDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    private MilestoneRule toRule(MilestoneDef def) {
        def.initialize();

        String id = def.generateUniqueHash();
        MilestoneRule rule = new MilestoneRule(id);
        AbstractDef.defToRule(def, rule);

        if (def.isPointBased()) {
            rule.setValueExtractor((event, input, otherInput) -> {
                if (event instanceof BasePointEvent) {
                    return ((BasePointEvent) event).getPoints();
                }
                return BigDecimal.ZERO;
            });
        } else {
            rule.setValueExtractor(Scripting.create(def.getValueExtractor(), RULE_VAR, CONTEXT_VAR));
        }

        rule.setLevels(def.getLevels().stream()
            .map(l -> new MilestoneRule.Level(l.getLevel(), l.getMilestone()))
            .collect(Collectors.toList()));

        return rule;
    }
}

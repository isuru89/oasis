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
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.events.BasePointEvent;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.elements.milestones.spec.ValueExtractorDef;

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
        MilestoneDef def = loadFrom(persistedObj, MilestoneDef.class);
        def.validate();
        return def;
    }

    @Override
    public MilestoneRule convert(AbstractDef<? extends BaseSpecification> definition) {
        if (definition instanceof MilestoneDef) {
            return toRule((MilestoneDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    private MilestoneRule toRule(MilestoneDef def) {
        def.validate();

        MilestoneRule rule = new MilestoneRule(def.getId());
        AbstractDef.defToRule(def, rule);


        ValueExtractorDef valueExtractor = def.getSpec().getValueExtractor();
        if (valueExtractor != null) {
            if (Texts.isNotEmpty(valueExtractor.getExpression())) {
                rule.setValueExtractor(Scripting.create(valueExtractor.getExpression(), RULE_VAR, CONTEXT_VAR));
            } else if (valueExtractor.getAmount() != null) {
                rule.setValueExtractor((event, input, otherInput) -> valueExtractor.getAmount());
            } else {
                rule.setValueExtractor((event, input, otherInput) -> BigDecimal.ONE);
            }
        } else if (def.isPointBased()) {
            rule.setValueExtractor((event, input, otherInput) -> {
                if (event instanceof BasePointEvent) {
                    return ((BasePointEvent) event).getPoints();
                }
                return BigDecimal.ZERO;
            });
        }

        rule.setLevels(def.getSpec().getLevels().stream()
            .map(l -> new MilestoneRule.Level(l.getLevel(), l.getMilestone()))
            .collect(Collectors.toList()));

        return rule;
    }
}

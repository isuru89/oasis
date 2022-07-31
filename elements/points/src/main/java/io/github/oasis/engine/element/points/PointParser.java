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

package io.github.oasis.engine.element.points;

import io.github.oasis.core.elements.*;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.core.utils.Utils;
import io.github.oasis.engine.element.points.spec.PointRewardDef;
import io.github.oasis.engine.element.points.spec.PointSpecification;

import java.util.List;
import java.util.Objects;

import static io.github.oasis.core.VariableNames.CONTEXT_VAR;

/**
 * @author Isuru Weerarathna
 */
public class PointParser extends AbstractElementParser {

    @Override
    public PointDef parse(EngineMessage persistedObj) {
        PointDef def = loadFrom(persistedObj, PointDef.class);
        def.validate();
        return def;
    }

    @Override
    public AbstractRule convert(AbstractDef<? extends BaseSpecification> definition) {
        if (definition instanceof PointDef) {
            return toRule((PointDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    @Override
    public AcceptedDefinitions getAcceptingDefinitions() {
        return new AcceptedDefinitions().addAcceptingDefinition(
                PointsModule.ID,
                new AcceptedDefinition()
                        .setDefinitionClz(PointDef.class)
                        .setSpecificationClz(PointSpecification.class)
        );
    }

    private AbstractRule toRule(PointDef def) {
        def.validate();

        PointRule rule = new PointRule(def.getId());
        AbstractDef.defToRule(def, rule);
        rule.setPointId(Utils.firstNonNullAsStr(def.getSpec().getReward().getPointId(), def.getName()));
        rule.setCriteria(EventExecutionFilterFactory.ALWAYS_TRUE);

        PointRewardDef award = def.getSpec().getReward();
        if (Objects.nonNull(award.getAmount())) {
            rule.setAmountToAward(award.getAmount());
        } else if (Objects.nonNull(award.getExpression())) {
            rule.setAmountExpression(Scripting.create(award.getExpression(), CONTEXT_VAR));
        }

        if (Objects.nonNull(def.getSpec().getCap())) {
            rule.setCapDuration(def.getSpec().getCap().getDuration());
            rule.setCapLimit(def.getSpec().getCap().getLimit());
        }
        return rule;
    }

}

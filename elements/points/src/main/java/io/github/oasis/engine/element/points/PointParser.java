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

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractElementParser;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.core.elements.EventExecutionFilterFactory;
import io.github.oasis.core.elements.Scripting;
import io.github.oasis.core.external.messages.PersistedDef;
import io.github.oasis.core.utils.Utils;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Objects;

import static io.github.oasis.core.VariableNames.CONTEXT_VAR;

/**
 * @author Isuru Weerarathna
 */
public class PointParser extends AbstractElementParser {

    @Override
    public PointDef parse(PersistedDef persistedObj) {
        return loadFrom(persistedObj, PointDef.class);
    }

    @Override
    public AbstractRule convert(AbstractDef definition) {
        if (definition instanceof PointDef) {
            return toRule((PointDef) definition);
        }
        throw new IllegalArgumentException("Unknown definition type! " + definition);
    }

    private AbstractRule toRule(PointDef def) {
        String id = def.generateUniqueHash();
        PointRule rule = new PointRule(id);
        AbstractDef.defToRule(def, rule);
        rule.setPointId(Utils.firstNonNullAsStr(def.getPointId(), def.getName()));
        rule.setCriteria(EventExecutionFilterFactory.ALWAYS_TRUE);

        Object award = def.getAward();
        if (award instanceof Number) {
            rule.setAmountToAward(BigDecimal.valueOf(((Number) award).doubleValue()));
        } else {
            rule.setAmountExpression(Scripting.create((String) award, CONTEXT_VAR));
        }

        appendLimit(rule, def);
        return rule;
    }

    @SuppressWarnings("unchecked")
    private void appendLimit(PointRule rule, PointDef def) {
        if (Objects.nonNull(def.getLimit())) {
            Map<String, Object> limit = (Map<String, Object>) def.getLimit();
            limit.forEach((unit, value) -> {
                rule.setCapDuration(unit);
                rule.setCapLimit(new BigDecimal(value.toString()));
            });
        }
    }

}

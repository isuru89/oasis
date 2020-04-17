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

package io.github.oasis.engine.elements.milestones;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.elements.AbstractRule;
import io.github.oasis.engine.factory.Scripting;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class MilestoneDef extends AbstractDef {

    private String valueExtractor;
    private List<MilestoneLevel> levels;

    @Override
    public AbstractRule toRule() {
        MilestoneRule rule = new MilestoneRule(generateUniqueHash());
        super.toRule(rule);

        rule.setValueExtractor(Scripting.create(valueExtractor,
                Scripting.RULE_VAR, Scripting.CONTEXT_VAR));
        rule.setLevels(levels.stream()
            .map(l -> new MilestoneRule.Level(l.level, l.milestone))
            .collect(Collectors.toList()));
        return rule;
    }

    public String getValueExtractor() {
        return valueExtractor;
    }

    public void setValueExtractor(String valueExtractor) {
        this.valueExtractor = valueExtractor;
    }

    public List<MilestoneLevel> getLevels() {
        return levels;
    }

    public void setLevels(List<MilestoneLevel> levels) {
        this.levels = levels;
    }

    public static class MilestoneLevel implements Serializable {
        private int level;
        private BigDecimal milestone;

        public int getLevel() {
            return level;
        }

        public void setLevel(int level) {
            this.level = level;
        }

        public BigDecimal getMilestone() {
            return milestone;
        }

        public void setMilestone(BigDecimal milestone) {
            this.milestone = milestone;
        }
    }
}

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

package io.github.oasis.model.defs;

import java.util.List;

/**
 * @author iweerarathna
 */
public class RatingDef extends BaseDef {

    private String event;
    private String condition;
    private Integer defaultState;
    private String stateValueExpression;
    private boolean currency = true;

    private List<RatingState> states;

    public static class RatingState {
        private Integer id;
        private String name;
        private String condition;
        private Double points;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCondition() {
            return condition;
        }

        public void setCondition(String condition) {
            this.condition = condition;
        }

        public Double getPoints() {
            return points;
        }

        public void setPoints(Double points) {
            this.points = points;
        }
    }

    public boolean isCurrency() {
        return currency;
    }

    public void setCurrency(boolean currency) {
        this.currency = currency;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getStateValueExpression() {
        return stateValueExpression;
    }

    public void setStateValueExpression(String stateValueExpression) {
        this.stateValueExpression = stateValueExpression;
    }

    public List<RatingState> getStates() {
        return states;
    }

    public void setStates(List<RatingState> states) {
        this.states = states;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Integer getDefaultState() {
        return defaultState;
    }

    public void setDefaultState(Integer defaultState) {
        this.defaultState = defaultState;
    }
}

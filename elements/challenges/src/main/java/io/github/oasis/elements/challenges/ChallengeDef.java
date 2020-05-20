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

package io.github.oasis.elements.challenges;

import io.github.oasis.core.elements.AbstractDef;
import io.github.oasis.core.utils.Utils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Isuru Weerarathna
 */
public class ChallengeDef extends AbstractDef {

    private Long startAt;
    private Long expireAt;

    private Integer winnerCount;

    private Map<String, Object> scope;

    private Object criteria;

    private String pointId;
    private Object pointAwards;

    @Override
    protected List<String> getSensitiveAttributes() {
        List<String> base = new ArrayList<>(super.getSensitiveAttributes());
        base.add(Utils.firstNonNullAsStr(startAt, EMPTY));
        base.add(Utils.firstNonNullAsStr(expireAt, EMPTY));
        base.add(Utils.firstNonNullAsStr(winnerCount, EMPTY));
        base.add(Utils.firstNonNullAsStr(criteria, EMPTY));
        base.add(Utils.firstNonNullAsStr(pointId, EMPTY));
        base.add(Utils.firstNonNullAsStr(pointAwards, EMPTY));
        base.add(Utils.firstNonNullAsStr(scope, EMPTY));
        return base;
    }

    public Long getStartAt() {
        return startAt;
    }

    public void setStartAt(Long startAt) {
        this.startAt = startAt;
    }

    public Long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(Long expireAt) {
        this.expireAt = expireAt;
    }

    public Integer getWinnerCount() {
        return winnerCount;
    }

    public void setWinnerCount(Integer winnerCount) {
        this.winnerCount = winnerCount;
    }

    public Map<String, Object> getScope() {
        return scope;
    }

    public void setScope(Map<String, Object> scope) {
        this.scope = scope;
    }

    public Object getCriteria() {
        return criteria;
    }

    public void setCriteria(Object criteria) {
        this.criteria = criteria;
    }

    public String getPointId() {
        return pointId;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public Object getPointAwards() {
        return pointAwards;
    }

    public void setPointAwards(Object pointAwards) {
        this.pointAwards = pointAwards;
    }
}

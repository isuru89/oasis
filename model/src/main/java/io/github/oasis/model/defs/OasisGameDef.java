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

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class OasisGameDef implements Serializable {

    private GameDef game;

    private List<KpiDef> kpis;
    private List<PointDef> points;
    private List<BadgeDef> badges;
    private List<MilestoneDef> milestones;
    private List<RatingDef> states;

    public List<RatingDef> getStates() {
        return states;
    }

    public void setStates(List<RatingDef> states) {
        this.states = states;
    }

    public GameDef getGame() {
        return game;
    }

    public void setGame(GameDef game) {
        this.game = game;
    }

    public List<KpiDef> getKpis() {
        return kpis;
    }

    public void setKpis(List<KpiDef> kpis) {
        this.kpis = kpis;
    }

    public List<PointDef> getPoints() {
        return points;
    }

    public void setPoints(List<PointDef> points) {
        this.points = points;
    }

    public List<BadgeDef> getBadges() {
        return badges;
    }

    public void setBadges(List<BadgeDef> badges) {
        this.badges = badges;
    }

    public List<MilestoneDef> getMilestones() {
        return milestones;
    }

    public void setMilestones(List<MilestoneDef> milestones) {
        this.milestones = milestones;
    }
}

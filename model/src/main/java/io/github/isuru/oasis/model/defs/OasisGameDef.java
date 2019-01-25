package io.github.isuru.oasis.model.defs;

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
    private List<StateDef> states;

    public List<StateDef> getStates() {
        return states;
    }

    public void setStates(List<StateDef> states) {
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

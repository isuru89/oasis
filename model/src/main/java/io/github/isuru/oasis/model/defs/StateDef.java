package io.github.isuru.oasis.model.defs;

import java.io.Serializable;
import java.util.List;

/**
 * @author iweerarathna
 */
public class StateDef extends BaseDef {

    private String event;
    private String condition;
    private Integer defaultState;
    private String stateValueExpression;

    private List<State> states;
    private List<StateChangeAwards> stateChangeAwards;

    public static class StateChangeAwards implements Serializable {
        private Integer from;
        private Integer to;
        private Double points;

        public Integer getFrom() {
            return from;
        }

        public void setFrom(Integer from) {
            this.from = from;
        }

        public Integer getTo() {
            return to;
        }

        public void setTo(Integer to) {
            this.to = to;
        }

        public Double getPoints() {
            return points;
        }

        public void setPoints(Double points) {
            this.points = points;
        }
    }

    public static class State {
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

    public List<StateChangeAwards> getStateChangeAwards() {
        return stateChangeAwards;
    }

    public void setStateChangeAwards(List<StateChangeAwards> stateChangeAwards) {
        this.stateChangeAwards = stateChangeAwards;
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

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
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

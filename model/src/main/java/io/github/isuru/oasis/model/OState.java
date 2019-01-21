package io.github.isuru.oasis.model;

import java.io.Serializable;
import java.util.List;

public class OState implements Serializable {

    private long id;
    private String name;
    private String displayName;

    private String event;
    private Serializable condition;
    private Integer defaultState;
    private Serializable stateValueExpression;
    private boolean currency;

    private List<OAState> states;
    private List<OAStateChangeAwards> stateChangeAwards;

    public static class OAStateChangeAwards implements Serializable {
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

    public static class OAState implements Serializable {
        private Integer id;
        private String name;
        private Serializable condition;
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

        public Serializable getCondition() {
            return condition;
        }

        public void setCondition(Serializable condition) {
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

    public List<OAStateChangeAwards> getStateChangeAwards() {
        return stateChangeAwards;
    }

    public void setStateChangeAwards(List<OAStateChangeAwards> stateChangeAwards) {
        this.stateChangeAwards = stateChangeAwards;
    }

    public Serializable getCondition() {
        return condition;
    }

    public void setCondition(Serializable condition) {
        this.condition = condition;
    }

    public Serializable getStateValueExpression() {
        return stateValueExpression;
    }

    public void setStateValueExpression(Serializable stateValueExpression) {
        this.stateValueExpression = stateValueExpression;
    }

    public List<OAState> getStates() {
        return states;
    }

    public void setStates(List<OAState> states) {
        this.states = states;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

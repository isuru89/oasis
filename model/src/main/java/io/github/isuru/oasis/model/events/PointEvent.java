package io.github.isuru.oasis.model.events;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.collect.Pair;
import io.github.isuru.oasis.model.rules.PointRule;

import java.util.HashMap;
import java.util.Map;

/**
 * @author iweerarathna
 */
public class PointEvent implements Event {

    private final Map<String, Pair<Double, PointRule>> receivedPoints = new HashMap<>();
    private double totalScore = 0.0;
    private Event refEvent;

    public PointEvent(Event event) {
        refEvent = event;
    }

    public Map<String, Pair<Double, PointRule>> getReceivedPoints() {
        return receivedPoints;
    }

    public void setPointEvents(Map<String, Pair<Double, PointRule>> pointEvents) {
        for (Map.Entry<String, Pair<Double, PointRule>> entry : pointEvents.entrySet()) {
            totalScore += entry.getValue().getValue0();
            receivedPoints.put(entry.getKey(), entry.getValue());
        }
    }

    public double getTotalScore() {
        return totalScore;
    }

    public Pair<Double, PointRule> getPointScore(String pointId) {
        return getReceivedPoints().get(pointId);
    }

    public boolean containsPoint(String pointEventId) {
        return receivedPoints.containsKey(pointEventId);
    }

    public Event getRefEvent() {
        return refEvent;
    }

    @Override
    public Map<String, Object> getAllFieldValues() {
        return refEvent.getAllFieldValues();
    }

    @Override
    public void setFieldValue(String fieldName, Object value) {
        // not supported
    }

    @Override
    public Object getFieldValue(String fieldName) {
        Object fieldValue = refEvent.getFieldValue(fieldName);
        if (fieldValue != null) {
            return fieldValue;
        }
        return receivedPoints.get(fieldName);
    }

    @Override
    public String getEventType() {
        return refEvent.getEventType();
    }

    @Override
    public long getTimestamp() {
        return refEvent.getTimestamp();
    }

    @Override
    public long getUser() {
        if (refEvent != null) {
            return refEvent.getUser();
        } else {
            return -1L;
        }
    }

    @Override
    public String getExternalId() {
        return refEvent.getExternalId();
    }

    @Override
    public Long getUserId(String fieldName) {
        return refEvent.getUserId(fieldName);
    }

    @Override
    public Long getTeam() {
        return refEvent.getTeam();
    }

    @Override
    public Long getTeamScope() {
        return refEvent.getTeamScope();
    }

    @Override
    public Integer getSource() {
        return refEvent.getSource();
    }

    @Override
    public Integer getGameId() {
        return refEvent.getGameId();
    }

    @Override
    public String toString() {
        return "PointEvent{" +
                "receivedPoints=" + receivedPoints +
                ", refEvent=" + refEvent +
                '}';
    }
}

package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.PointEvent;
import io.github.isuru.oasis.model.rules.PointRule;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IPointHandler extends Serializable {

    void pointsScored(Long userId, PointNotification pointNotification);

    default void beforeAllPointsNotified(PointEvent event) {
        // do nothing
    }

    default void afterAllPointsNotifier(PointEvent event) {
        // do nothing
    }

    default void onPointError(Throwable ex, Event e, PointRule rule) {}
}

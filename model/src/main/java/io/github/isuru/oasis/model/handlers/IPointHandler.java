package io.github.isuru.oasis.model.handlers;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IPointHandler extends Serializable {

    void pointsScored(PointNotification pointNotification);
}

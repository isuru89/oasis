package io.github.isuru.oasis.model.handlers;

import io.github.isuru.oasis.Event;
import io.github.isuru.oasis.model.Milestone;

import java.io.Serializable;

/**
 * @author iweerarathna
 */
public interface IMilestoneHandler extends IErrorHandler<Milestone>, Serializable {

    void milestoneReached(Long user, int level, Event event, Milestone milestone);

}

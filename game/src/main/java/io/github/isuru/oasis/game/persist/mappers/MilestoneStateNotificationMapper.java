package io.github.isuru.oasis.game.persist.mappers;

import io.github.isuru.oasis.model.events.MilestoneStateEvent;
import io.github.isuru.oasis.model.handlers.output.MilestoneStateModel;

/**
 * @author iweerarathna
 */
public class MilestoneStateNotificationMapper extends BaseNotificationMapper<MilestoneStateEvent, MilestoneStateModel> {

    @Override
    MilestoneStateModel create(MilestoneStateEvent notification) throws Exception {
        MilestoneStateModel model = new MilestoneStateModel();
        model.setUserId(notification.getUserId());
        model.setValue(notification.getValue());
        model.setValueInt(notification.getValueInt());
        model.setNextValue(notification.getNextValue());
        model.setNextValueInt(notification.getNextValueInt());
        model.setLossUpdate(notification.isLossUpdate());
        model.setLossValue(notification.getLossValue());
        model.setLossValueInt(notification.getLossValueInt());
        model.setMilestoneId(notification.getMilestone().getId());
        return model;
    }
}

package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Collections;

public class ChallengeProcess extends ProcessFunction<Event, ChallengeEvent> {

    private OutputTag<PointNotification> pointOutputTag;
    private PointRule challengePointRule;

    public ChallengeProcess(PointRule challengePointRule, OutputTag<PointNotification> pointOutputTag) {
        this.pointOutputTag = pointOutputTag;
        this.challengePointRule = challengePointRule;
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<ChallengeEvent> out) {
        ChallengeEvent challengeEvent = new ChallengeEvent(event, null);
        double points = Utils.asDouble(event.getFieldValue(ChallengeEvent.KEY_POINTS));
        challengeEvent.setFieldValue(ChallengeEvent.KEY_DEF_ID,
                Utils.asLong(event.getFieldValue(ChallengeEvent.KEY_DEF_ID)));
        challengeEvent.setFieldValue(ChallengeEvent.KEY_POINTS, points);
        out.collect(challengeEvent);

        PointNotification pointNotification = new PointNotification(event.getUser(),
                Collections.singletonList(event),
                challengePointRule,
                points);
        ctx.output(pointOutputTag, pointNotification);
    }


}

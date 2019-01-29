package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.RaceEvent;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.util.Collections;

public class RaceProcess extends ProcessFunction<Event, RaceEvent> {

    private final PointRule racePointRule;
    private final OutputTag<PointNotification> pointOutput;

    public RaceProcess(PointRule racePointRule, OutputTag<PointNotification> pointOutput) {
        this.racePointRule = racePointRule;
        this.pointOutput = pointOutput;
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<RaceEvent> out) throws Exception {
        RaceEvent raceEvent = new RaceEvent(event);
        out.collect(raceEvent);

        double points = Utils.asDouble(event.getFieldValue(RaceEvent.KEY_POINTS));
        PointNotification pointNotification = new PointNotification(event.getUser(),
                Collections.singletonList(event),
                racePointRule,
                points);
        ctx.output(pointOutput, pointNotification);
    }
}

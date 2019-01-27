package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.events.ChallengeEvent;
import io.github.isuru.oasis.model.handlers.PointNotification;
import io.github.isuru.oasis.model.rules.PointRule;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;

import java.util.Collections;

public class RaceProcess extends ProcessFunction<Event, PointNotification> {

    private final PointRule racePointRule;

    public RaceProcess(PointRule racePointRule) {
        this.racePointRule = racePointRule;
    }

    @Override
    public void processElement(Event event, Context ctx, Collector<PointNotification> out) {
        double points = Utils.asDouble(event.getFieldValue(ChallengeEvent.KEY_POINTS));
        out.collect(new PointNotification(event.getUser(),
                Collections.singletonList(event),
                racePointRule,
                points));
    }
}

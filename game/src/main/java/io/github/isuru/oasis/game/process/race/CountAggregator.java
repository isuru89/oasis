package io.github.isuru.oasis.game.process.race;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Race;

public class CountAggregator extends BaseAggregator {
    public CountAggregator(Race race) {
        super(race);
    }

    @Override
    public AggregatedBucket add(Event value, AggregatedBucket accumulator) {
        if (!accumulator.isFilled()) {
            copyAttrs(accumulator, value);
        }

        if (eval(value)) {
            if (accumulator.getAggValue() == null) {
                accumulator.setAggValue(1.0);
            } else {
                accumulator.setAggValue(accumulator.getAggValue() + 1);
            }
        }
        return accumulator;
    }

}

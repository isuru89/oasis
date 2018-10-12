package io.github.isuru.oasis.game.process.race;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Race;

public class MaxAggregator extends BaseAggregator {
    public MaxAggregator(Race race) {
        super(race);
    }

    @Override
    public AggregatedBucket createAccumulator() {
        return new AggregatedBucket();
    }

    @Override
    public AggregatedBucket add(Event value, AggregatedBucket accumulator) {
        if (!accumulator.isFilled()) {
            copyAttrs(accumulator, value);
        }

        if (eval(value)) {
            double val = asDouble(value);
            if (accumulator.getAggValue() == null) {
                accumulator.setAggValue(val);
            } else {
                accumulator.setAggValue(Math.max(accumulator.getAggValue(), val));
            }
        }
        return accumulator;
    }

    @Override
    public AggregatedBucket getResult(AggregatedBucket accumulator) {
        return null;
    }

    @Override
    public AggregatedBucket merge(AggregatedBucket a, AggregatedBucket b) {
        return null;
    }
}

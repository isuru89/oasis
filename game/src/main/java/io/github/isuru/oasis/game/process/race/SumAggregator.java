package io.github.isuru.oasis.game.process.race;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Race;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

public class SumAggregator extends BaseAggregator {

    public SumAggregator(Race race) {
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
            accumulator.setAggValue(accumulator.getAggValue() + val);
        }
        return accumulator;
    }

}

package io.github.isuru.oasis.game.process.race;

import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Race;
import org.apache.flink.api.java.tuple.Tuple2;

public class AvgAggregator extends BaseAggregator {
    public AvgAggregator(Race race) {
        super(race);
    }

    @Override
    public AggregatedBucket createAccumulator() {
        AggregatedBucket bucket = super.createAccumulator();
        bucket.setSupportValues(Tuple2.of(getRace().getInitialValue(), 0L));
        return bucket;
    }

    @Override
    public AggregatedBucket add(Event value, AggregatedBucket accumulator) {
        if (!accumulator.isFilled()) {
            copyAttrs(accumulator, value);
        }

        if (eval(value)) {
            double val = asDouble(value);
            accumulator.setSupportValues(Tuple2.of(accumulator.getSupportValues().f0 + val,
                    accumulator.getSupportValues().f1 + 1L));
            accumulator.setAggValue(accumulator.getSupportValues().f0 / accumulator.getSupportValues().f1);
        }
        return accumulator;
    }

    @Override
    public AggregatedBucket getResult(AggregatedBucket accumulator) {
        AggregatedBucket bucket = super.getResult(accumulator);
        bucket.setSupportValues(Tuple2.of(accumulator.getSupportValues().f0,
                accumulator.getSupportValues().f1));
        return bucket;
    }

    @Override
    public AggregatedBucket merge(AggregatedBucket a, AggregatedBucket b) {
        AggregatedBucket bucket = cloneBucket(a);
        Tuple2<Double, Long> tuple = Tuple2.of(a.getSupportValues().f0 + b.getSupportValues().f0,
                a.getSupportValues().f1 + b.getSupportValues().f1);
        bucket.setAggValue(tuple.f0 / tuple.f1);
        return bucket;
    }
}

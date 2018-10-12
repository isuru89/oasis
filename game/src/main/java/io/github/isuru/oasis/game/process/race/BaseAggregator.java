package io.github.isuru.oasis.game.process.race;

import io.github.isuru.oasis.game.utils.Utils;
import io.github.isuru.oasis.model.Event;
import io.github.isuru.oasis.model.Race;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.mvel2.MVEL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;

abstract class BaseAggregator implements AggregateFunction<Event, AggregatedBucket, AggregatedBucket> {

    private static final Logger LOG = LoggerFactory.getLogger(BaseAggregator.class);

    private final Race race;

    BaseAggregator(Race race) {
        this.race = race;
    }

    public Race getRace() {
        return race;
    }

    @Override
    public AggregatedBucket createAccumulator() {
        AggregatedBucket bucket = new AggregatedBucket();
        bucket.setAggValue(getRace().getInitialValue());
        return bucket;
    }

    @Override
    public AggregatedBucket getResult(AggregatedBucket accumulator) {
        return cloneBucket(accumulator);
    }

    @Override
    public AggregatedBucket merge(AggregatedBucket a, AggregatedBucket b) {
        AggregatedBucket bucket = cloneBucket(a);
        bucket.setAggValue(a.getAggValue() + b.getAggValue());
        return bucket;
    }

    AggregatedBucket cloneBucket(AggregatedBucket b) {
        AggregatedBucket bucket = new AggregatedBucket();
        bucket.setUserId(b.getUserId());
        bucket.setTeamId(b.getTeamId());
        bucket.setTeamScopeId(b.getTeamScopeId());
        return bucket;
    }

    void copyAttrs(AggregatedBucket accumulator, Event value) {
        accumulator.setUserId(value.getUser());
        accumulator.setTeamId(value.getTeam());
        accumulator.setTeamScopeId(value.getTeamScope());
    }

    boolean eval(Event event) {
        try {
            return Utils.evaluateCondition(race.getCondition(), event.getAllFieldValues());
        } catch (IOException e) {
            LOG.error("Failed to evaluate race sum aggregator for event " + event.getExternalId(), e);
            return false;
        }
    }

    double asDouble(Event event) {
        Serializable expr = race.getAccumulator();
        if (expr != null) {
            Object o = MVEL.executeExpression(expr, event.getAllFieldValues());
            if (o instanceof Number) {
                return ((Number) o).doubleValue();
            }
        }
        return 0.0;
    }
}

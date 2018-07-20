package io.github.isuru.oasis.game.process;

import io.github.isuru.oasis.model.events.ErrorPointEvent;
import io.github.isuru.oasis.model.events.PointEvent;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
public class PointErrorSplitter implements OutputSelector<PointEvent> {

    public static final String NAME_ERROR = "ERROR";
    public static final String NAME_POINT = "POINT";

    private static final Iterable<String> ERROR_LIST = Collections.singletonList(NAME_ERROR);
    private static final Iterable<String> POINT_LIST = Collections.singletonList(NAME_POINT);

    @Override
    public Iterable<String> select(PointEvent value) {
        if (value instanceof ErrorPointEvent) {
            return ERROR_LIST;
        } else {
            return POINT_LIST;
        }
    }
}

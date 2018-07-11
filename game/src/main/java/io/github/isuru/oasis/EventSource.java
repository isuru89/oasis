package io.github.isuru.oasis;

import io.github.isuru.oasis.model.Event;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

public interface EventSource<E extends Event> extends SourceFunction<E> {

}

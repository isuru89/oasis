package io.github.isuru.oasis;

import org.apache.flink.streaming.api.functions.source.SourceFunction;

public interface EventSource<E extends Event> extends SourceFunction<E> {

}

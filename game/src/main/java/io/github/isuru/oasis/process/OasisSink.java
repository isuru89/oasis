package io.github.isuru.oasis.process;

import org.apache.flink.streaming.api.functions.sink.SinkFunction;

/**
 * @author iweerarathna
 */
public class OasisSink<E> implements SinkFunction<E> {

    @Override
    public void invoke(E value, Context context) {

    }
}

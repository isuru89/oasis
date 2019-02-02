package io.github.isuru.oasis.injector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

public class BufferedRecords implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(BufferedRecords.class);

    private static final int MAX_SIZE = 20;
    private static final int BATCH_SIZE = 100;

    private final PeriodicTimer timer = new PeriodicTimer(5000, this::flush);

    private final LinkedBlockingQueue<ElementRecord> queue = new LinkedBlockingQueue<>();

    private final Consumer<List<ElementRecord>> recordConsumer;

    public BufferedRecords(Consumer<List<ElementRecord>> recordConsumer) {
        this.recordConsumer = recordConsumer;
    }

    public void init(ExecutorService pool) {
        pool.submit(timer);
    }

    public void push(ElementRecord map) {
        queue.offer(map);
        if (queue.size() == MAX_SIZE) {
            flush();
        }
    }

    private synchronized void flush() {
        if (queue.isEmpty()) {
            return;
        }

        List<ElementRecord> records = new LinkedList<>();
        for (int i = 0; i < BATCH_SIZE; i++) {
            if (queue.peek() == null) {
                break;
            }
            records.add(queue.poll());
        }

        if (records.size() > 0) {
            recordConsumer.accept(records);
        }
    }

    public void flushNow() {
        flush();
    }

    @Override
    public void close() {
        LOG.warn("Shutdown signal received for buffer.");
        timer.doStop();
    }

    public static class ElementRecord {
        private Map<String, Object> data;
        private long deliveryTag;

        public ElementRecord(Map<String, Object> data, long deliveryTag) {
            this.data = data;
            this.deliveryTag = deliveryTag;
        }

        public Map<String, Object> getData() {
            return data;
        }

        long getDeliveryTag() {
            return deliveryTag;
        }
    }

    private static class PeriodicTimer implements Runnable {

        private final long interval;
        private final Runnable method;

        private volatile boolean stop = false;

        private PeriodicTimer(long interval, Runnable method) {
            this.interval = interval;
            this.method = method;
        }

        @Override
        public void run() {
            while (!stop) {
                try {
                    Thread.sleep(interval);
                    this.method.run();
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        private void doStop() {
            stop = true;
        }
    }

}

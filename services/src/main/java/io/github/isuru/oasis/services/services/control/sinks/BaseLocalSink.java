package io.github.isuru.oasis.services.services.control.sinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.isuru.oasis.model.db.IOasisDao;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author iweerarathna
 */
public abstract class BaseLocalSink implements Runnable {

    protected static final ObjectMapper mapper = new ObjectMapper();

    protected IOasisDao dao;
    private final long gameId;
    private boolean cancel = false;
    private final String qName;

    BaseLocalSink(IOasisDao dao, long gameId, String qName) {
        this.dao = dao;
        this.gameId = gameId;
        this.qName = qName;
    }

    @Override
    public void run() {
        try {
            LinkedBlockingQueue<String> queue = SinkData.get().poll(gameId, qName);
            while (!cancel) {
                try {
                    String item = queue.poll(5, TimeUnit.SECONDS);
                    if (item != null) {
                        System.out.println("Notification recieved!");
                        handle(item);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to handle sink " + this.getClass().getName() + "!", e);
                }
            }
        } finally {
            System.out.println(String.format("Thread completed for %s in %d!", this.getClass().getSimpleName() ,gameId));
        }
    }

    protected abstract void handle(String value) throws Exception;

    public void stop() {
        cancel = true;
    }

    long getGameId() {
        return gameId;
    }
}

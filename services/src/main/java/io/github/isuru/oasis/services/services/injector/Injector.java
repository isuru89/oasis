package io.github.isuru.oasis.services.services.injector;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.services.injector.consumers.BaseConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Vector;

@Component
public class Injector implements Closeable {

    private static final Logger LOG = LoggerFactory.getLogger(Injector.class);

    private final List<BaseConsumer> consumerList = new Vector<>();

    private final IOasisDao dao;
    private final ResultFetcherManager fetcherManager;

    @Autowired
    public Injector(IOasisDao dao, ResultFetcherManager fetcherManager) {
        this.dao = dao;
        this.fetcherManager = fetcherManager;
    }

    public void start() throws Exception {
        ResultFetcher resultFetcher = fetcherManager.getResultFetcher();

        resultFetcher.start(dao);
        consumerList.clear();
        consumerList.addAll(resultFetcher.getConsumers());
    }

    @Override
    public void close() throws IOException {
        LOG.warn("Closing injector...");
        for (BaseConsumer consumer : consumerList) {
            consumer.close();
        }

        fetcherManager.getResultFetcher().close();
    }
}

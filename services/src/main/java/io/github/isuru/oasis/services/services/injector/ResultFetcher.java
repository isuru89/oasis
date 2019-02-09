package io.github.isuru.oasis.services.services.injector;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.services.injector.consumers.BaseConsumer;

import java.io.Closeable;
import java.util.List;

public interface ResultFetcher extends Closeable {

    void start(IOasisDao dao) throws Exception;

    List<BaseConsumer> getConsumers() throws Exception;
}

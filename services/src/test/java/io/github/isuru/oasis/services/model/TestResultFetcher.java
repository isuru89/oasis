package io.github.isuru.oasis.services.model;

import io.github.isuru.oasis.model.db.IOasisDao;
import io.github.isuru.oasis.services.services.injector.ResultFetcher;
import io.github.isuru.oasis.services.services.injector.consumers.BaseConsumer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component("fetcherTest")
public class TestResultFetcher implements ResultFetcher {
    @Override
    public void start(IOasisDao dao) throws Exception {

    }

    @Override
    public List<BaseConsumer> getConsumers() throws Exception {
        return new ArrayList<>();
    }

    @Override
    public void close() throws IOException {

    }
}

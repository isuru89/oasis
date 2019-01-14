package io.github.isuru.oasis.services.services.dispatchers;

import io.github.isuru.oasis.services.model.IEventDispatcher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component("dispatcherLocal")
public class LocalDispatcher implements IEventDispatcher {

    @Override
    public void init() throws IOException {

    }

    @Override
    public void dispatch(long gameId, Map<String, Object> data) throws IOException {

    }

    @Override
    public void close() throws IOException {

    }
}

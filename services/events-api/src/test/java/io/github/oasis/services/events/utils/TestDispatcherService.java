package io.github.oasis.services.events.utils;

import io.github.oasis.services.events.dispatcher.EventDispatcherService;
import io.github.oasis.services.events.model.EventProxy;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

/**
 * @author Isuru Weerarathna
 */
public class TestDispatcherService implements EventDispatcherService {
    @Override
    public EventDispatcherService push(EventProxy event, Handler<AsyncResult<JsonObject>> result) {
        result.handle(Future.succeededFuture(new JsonObject()));
        return this;
    }

    @Override
    public void close() {

    }
}

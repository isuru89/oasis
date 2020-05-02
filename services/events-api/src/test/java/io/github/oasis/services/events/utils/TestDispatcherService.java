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

    private boolean returnSuccess = true;

    public TestDispatcherService() {
    }

    @Override
    public EventDispatcherService push(EventProxy event, Handler<AsyncResult<JsonObject>> result) {
        if (returnSuccess) {
            result.handle(Future.succeededFuture(new JsonObject()
                    .put("success", true)
                    .put("eventId", event.getExternalId())));
        } else {
            result.handle(Future.failedFuture("Failed"));
        }
        return this;
    }

    public void setReturnSuccess(boolean returnSuccess) {
        this.returnSuccess = returnSuccess;
    }

}

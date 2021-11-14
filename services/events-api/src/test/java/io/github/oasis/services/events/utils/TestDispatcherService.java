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
    public EventDispatcherService pushEvent(EventProxy event, Handler<AsyncResult<JsonObject>> result) {
        if (returnSuccess) {
            System.out.println(">>> pushing: " + event);
            result.handle(Future.succeededFuture(new JsonObject()
                    .put("success", true)
                    .put("eventId", event.getExternalId())));
        } else {
            result.handle(Future.failedFuture("Failed"));
        }
        return this;
    }

    @Override
    public EventDispatcherService push(JsonObject message, Handler<AsyncResult<JsonObject>> handler) {
        return null;
    }

    @Override
    public EventDispatcherService broadcast(JsonObject obj, Handler<AsyncResult<JsonObject>> handler) {
        return null;
    }

    public void setReturnSuccess(boolean returnSuccess) {
        this.returnSuccess = returnSuccess;
    }

}

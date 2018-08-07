package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.EventPushDto;
import spark.Spark;

/**
 * @author iweerarathna
 */
public class EventsRouter extends BaseRouters {

    public EventsRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        post("/event", (req, res) -> {
            checkAuth(req);

            EventPushDto eventPushDto = bodyAs(req, EventPushDto.class);
            if (eventPushDto.getEvent() != null) {
                getApiService().getEventService().submitEvent(eventPushDto.getEvent());
            } else if (eventPushDto.getEvents() != null) {
                getApiService().getEventService().submitEvents(eventPushDto.getEvents());
            } else {
                return Spark.halt(400, "No events have been defined!");
            }
            return null;
        });
    }
}

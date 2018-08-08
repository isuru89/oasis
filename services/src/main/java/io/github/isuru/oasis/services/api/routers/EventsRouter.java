package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.EventPushDto;
import io.github.isuru.oasis.services.exception.InputValidationException;

/**
 * @author iweerarathna
 */
public class EventsRouter extends BaseRouters {

    EventsRouter(IOasisApiService apiService) {
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
                throw new InputValidationException("No events have been defined in this call!");
            }
            return null;
        });
    }
}

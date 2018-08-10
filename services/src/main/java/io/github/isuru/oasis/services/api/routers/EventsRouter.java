package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.EventPushDto;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import spark.Request;

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
            String token = extractToken(req);

            EventPushDto eventPushDto = bodyAs(req, EventPushDto.class);
            if (eventPushDto.getEvent() != null) {
                getApiService().getEventService().submitEvent(token, eventPushDto.getEvent());
            } else if (eventPushDto.getEvents() != null) {
                getApiService().getEventService().submitEvents(token, eventPushDto.getEvents());
            } else {
                throw new InputValidationException("No events have been defined in this call!");
            }
            return null;
        });
    }

    private String extractToken(Request request) throws ApiAuthException {
        String auth = request.headers(AUTHORIZATION);
        if (auth != null) {
            if (auth.startsWith("Bearer ")) {
                return auth.substring("Bearer ".length());
            }
        }
        throw new ApiAuthException("The token is not found with the event.");
    }
}

package io.github.isuru.oasis.services.api.routers;

import io.github.isuru.oasis.model.Constants;
import io.github.isuru.oasis.services.api.IEventsService;
import io.github.isuru.oasis.services.api.IOasisApiService;
import io.github.isuru.oasis.services.api.dto.EventPushDto;
import io.github.isuru.oasis.services.exception.ApiAuthException;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.UserRole;
import spark.Request;
import spark.Spark;

import java.util.Map;

/**
 * @author iweerarathna
 */
public class EventsRouter extends BaseRouters {

    EventsRouter(IOasisApiService apiService) {
        super(apiService);
    }

    @Override
    public void register() {
        IEventsService es = getApiService().getEventService();

        post("/submit", (req, res) -> {
            String token = extractToken(req);

            EventPushDto eventPushDto = bodyAs(req, EventPushDto.class);
            Long gid = eventPushDto.getMeta() == null || !eventPushDto.getMeta().containsKey("gameId")
                    ? null
                    : Long.parseLong(eventPushDto.getMeta().get("gameId").toString());

            if (eventPushDto.getEvent() != null) {
                Map<String, Object> event = eventPushDto.getEvent();
                event.put(Constants.FIELD_GAME_ID, gid);
                es.submitEvent(token, eventPushDto.getEvent());
            } else if (eventPushDto.getEvents() != null) {
                eventPushDto.getEvents().forEach(et -> et.put(Constants.FIELD_GAME_ID, gid));
                es.submitEvents(token, eventPushDto.getEvents());
            } else {
                throw new InputValidationException("No events have been defined in this call!");
            }
            return asResBool(true);
        });

        Spark.path("/source", () -> {
            post("/add", (req, res) -> es.addEventSource(bodyAs(req, EventSourceToken.class)),
                    UserRole.ADMIN);
            post("/list", (req, res) -> es.listAllEventSources(), UserRole.ADMIN)
            .delete("/:srcId",
                    (req, res) -> es.disableEventSource(asPInt(req, "srcId")), UserRole.ADMIN);
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

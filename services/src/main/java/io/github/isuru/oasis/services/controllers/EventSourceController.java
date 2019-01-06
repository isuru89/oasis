package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.api.dto.EventSourceDto;
import io.github.isuru.oasis.services.dto.DeleteResponse;
import io.github.isuru.oasis.services.exception.InputValidationException;
import io.github.isuru.oasis.services.services.IEventsService;
import io.github.isuru.oasis.services.utils.Checks;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import io.github.isuru.oasis.services.utils.UserRole;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
@SuppressWarnings("unused")
@RequestMapping("/event")
public class EventSourceController {

    private static final Logger LOG = LoggerFactory.getLogger(EventSourceController.class);

    @Autowired
    private IEventsService eventsService;

    @Secured(UserRole.ROLE_ADMIN)
    @PostMapping("/source")
    @ResponseBody
    public EventSourceDto addEventSource(@RequestBody EventSourceToken eventSourceToken) throws Exception {
        // duplicate events source names are ignored.
        if (eventsService.listAllEventSources().stream()
                .anyMatch(e -> e.getDisplayName().equalsIgnoreCase(eventSourceToken.getDisplayName()))) {
            throw new InputValidationException(
                    "There is already an event token exist with name '" + eventSourceToken.getDisplayName() + "'!");
        }

        EventSourceToken insertedToken = eventsService.addEventSource(eventSourceToken);
        return EventSourceDto.from(insertedToken);
    }

    @GetMapping("/source/list")
    @ResponseBody
    public List<EventSourceDto> listEventSource() throws Exception {
        return eventsService.listAllEventSources()
                .stream()
                .map(EventSourceDto::from)
                .collect(Collectors.toList());
    }

    @PostMapping(value = "/source/{id}/downloadKey", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @ResponseBody
    public void downloadEventSourceKey(@PathVariable("id") int sourceId, HttpServletResponse response) throws Exception {
        Checks.greaterThanZero(sourceId, "id");

        try {
            Optional<EventSourceToken> optionalToken = eventsService.makeDownloadSourceKey(sourceId);
            if (optionalToken.isPresent()) {
                EventSourceToken eventSourceToken = optionalToken.get();
                String sourceName = String.format("key-%s", eventSourceToken.getSourceName());

                response.addHeader("Content-Disposition", String.format("attachment; filename=key-%s.key", sourceName));
                response.addHeader("Access-Control-Expose-Headers", "Content-Disposition");

                // get your file as InputStream
                try (ByteArrayInputStream is = new ByteArrayInputStream(eventSourceToken.getSecretKey())) {
                    // copy it to response's OutputStream
                    IOUtils.copy(is, response.getOutputStream());
                    response.flushBuffer();
                }
            } else {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "The key has been already downloaded! Cannot download again.");
            }

        } catch (IOException ex) {
            LOG.info("Error writing buffer to output stream. Event source id was '{}'", sourceId, ex);
            throw new RuntimeException("IOError writing file to output buffer!");
        }
    }

    @Secured(UserRole.ROLE_ADMIN)
    @DeleteMapping("/source/{id}")
    @ResponseBody
    public DeleteResponse deleteEventSource(@PathVariable("id") int sourceId) throws Exception {
        boolean success = eventsService.disableEventSource(sourceId);
        return new DeleteResponse("eventSource", success);
    }

}

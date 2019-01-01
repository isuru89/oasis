package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.api.dto.EventSourceDto;
import io.github.isuru.oasis.services.utils.EventSourceToken;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Controller
public class EventSourceController {

    private static final Logger LOG = LoggerFactory.getLogger(EventSourceController.class);

    @PostMapping("/event/source")
    @ResponseBody
    public void addEventSource(@RequestBody EventSourceToken eventSourceToken) {

    }

    @GetMapping("/event/source/list")
    @ResponseBody
    public List<EventSourceDto> listEventSource() {
        return null;
    }

    @PostMapping("/event/source/{id}/downloadKey")
    @ResponseBody
    public void downloadEventSourceKey(@PathVariable("id") int sourceId, HttpServletResponse response) {
        try {
            // get your file as InputStream
            InputStream is = null;
            // copy it to response's OutputStream
            IOUtils.copy(is, response.getOutputStream());
            response.flushBuffer();
        } catch (IOException ex) {
            LOG.info("Error writing buffer to output stream. Event source id was '{}'", sourceId, ex);
            throw new RuntimeException("IOError writing file to output buffer!");
        }
    }

    @DeleteMapping("/event/source/{id}")
    @ResponseBody
    public void deleteEventSource(@PathVariable("id") int sourceId) {

    }

}

package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.api.dto.EventPushDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class EventsController {

    @PostMapping("/event/submit")
    @ResponseBody
    public void submitEvent(@RequestBody EventPushDto eventData) {

    }

}

package com.websitestudios.controller.v1;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class ErrorTriggerController {

    @GetMapping("/trigger-error")
    public void triggerError() {
        throw new RuntimeException("Triggered test exception");
    }
}

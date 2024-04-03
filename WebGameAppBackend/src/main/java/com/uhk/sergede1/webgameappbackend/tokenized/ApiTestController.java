package com.uhk.sergede1.webgameappbackend.tokenized;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiTestController {

    @GetMapping(path = "/api-test")
    public String basicAuthCheck() {
        return "Success";
    }
}

package com.uhk.sergede1.webgameappbackend.rest.free.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingTestController {
    @GetMapping(path = "/ping", produces = "application/json")
    public ApiResponse pingResponse() {
        return new ApiResponse(200);
    }

    public static class ApiResponse {
        private int response;

        public ApiResponse(int response) {
            this.response = response;
        }

        public int getResponse() {
            return response;
        }

        public void setResponse(int response) {
            this.response = response;
        }
    }
}
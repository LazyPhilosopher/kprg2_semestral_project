package com.uhk.sergede1.webgameappbackend.rest.tokenized.ping;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiTestController {

    @GetMapping(path = "/api/ping", produces = "application/json")
    @ResponseBody
    public ApiResponse pingResponse() {
        return new ApiTestController.ApiResponse(200);
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
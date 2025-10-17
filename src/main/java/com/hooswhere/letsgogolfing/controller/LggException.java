package com.hooswhere.letsgogolfing.controller;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

public class LggException extends ResponseStatusException {

    public LggException(HttpStatusCode code, String message, Throwable cause) {
        super(code, message, cause);
    }
    public LggException(HttpStatusCode status) {
        super(status);
    }

    public LggException(HttpStatusCode status, String reason) {
        super(status, reason);
    }
}

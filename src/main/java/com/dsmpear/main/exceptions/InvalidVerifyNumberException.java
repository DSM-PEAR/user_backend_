package com.dsmpear.main.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED, reason = "Invalid verify number exception")
public class InvalidVerifyNumberException extends RuntimeException {
    public InvalidVerifyNumberException() {
        super("Invalid verify number Exception!!");
    }
}

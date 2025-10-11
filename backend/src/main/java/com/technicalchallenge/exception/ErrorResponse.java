package com.technicalchallenge.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ErrorResponse {

    private String message;
    private int status;
    private String error;
    private String path;
    private final String timestamp = LocalDateTime.now().toString();
}



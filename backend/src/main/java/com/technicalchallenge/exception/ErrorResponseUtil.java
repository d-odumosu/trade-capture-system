package com.technicalchallenge.exception;

import org.springframework.http.HttpStatus;

public class ErrorResponseUtil {

        public static ErrorResponse buildErrorResponse(Exception ex, HttpStatus status, String path) {
            return new ErrorResponse(
                    ex.getMessage(),
                    status.value(),
                    status.getReasonPhrase(),
                    path);
        }
    }

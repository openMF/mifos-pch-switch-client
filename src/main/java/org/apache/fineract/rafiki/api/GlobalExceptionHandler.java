package org.apache.fineract.rafiki.api;

import org.apache.fineract.rafiki.exception.RafikiConnectorException;
import org.apache.fineract.rafiki.exception.WebhookSignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(WebhookSignatureException.class)
    public ResponseEntity<Map<String, String>> handleSignature(WebhookSignatureException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(RafikiConnectorException.class)
    public ResponseEntity<Map<String, String>> handleConnector(RafikiConnectorException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", ex.getMessage() != null ? ex.getMessage() : "Internal error"));
    }
}

package org.apache.fineract.rafiki.exception;

public class RafikiConnectorException extends RuntimeException {

    public RafikiConnectorException(String message) {
        super(message);
    }

    public RafikiConnectorException(String message, Throwable cause) {
        super(message, cause);
    }
}

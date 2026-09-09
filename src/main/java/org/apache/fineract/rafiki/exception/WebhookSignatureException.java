package org.apache.fineract.rafiki.exception;

public class WebhookSignatureException extends RafikiConnectorException {

    public WebhookSignatureException(String message) {
        super(message);
    }
}

package com.tallerproxy.proxy;

public class ServiceCallException extends RuntimeException {

    private final String requestId;

    public ServiceCallException(String requestId, RuntimeException cause) {
        super(cause.getMessage(), cause);
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }
}

package com.tallerproxy.proxy;

public interface MicroserviceProxy<T> {

    T execute(String operation, Object... params);
}

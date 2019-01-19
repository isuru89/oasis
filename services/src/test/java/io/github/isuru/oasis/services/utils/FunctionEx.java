package io.github.isuru.oasis.services.utils;

@FunctionalInterface
public interface FunctionEx<T, R> {

    R apply(T input) throws Exception;

}

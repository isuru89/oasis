package io.github.isuru.oasis.services.utils;

import io.github.isuru.oasis.services.exception.InputValidationException;

/**
 * @author iweerarathna
 */
public final class Preconditions {

    public static void checkParams(boolean condition, String msg) throws InputValidationException {
        if (!condition) {
            throw new InputValidationException(msg);
        }
    }

}

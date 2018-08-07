package io.github.isuru.oasis.services.exception;

/**
 * @author iweerarathna
 */
public class ApiAuthException extends Exception {

    public ApiAuthException(String message) {
        super(message);
    }

    public ApiAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}

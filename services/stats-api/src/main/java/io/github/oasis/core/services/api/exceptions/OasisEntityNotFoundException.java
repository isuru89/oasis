package io.github.oasis.core.services.api.exceptions;

import org.springframework.http.HttpStatus;

public class OasisEntityNotFoundException extends OasisApiRuntimeException  {
    public OasisEntityNotFoundException(String errorCode) {
        super(errorCode, HttpStatus.NOT_FOUND);
    }
}

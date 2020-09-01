/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.core.services.api.handlers;

import io.github.oasis.core.services.api.configs.ErrorMessages;
import io.github.oasis.core.services.exceptions.OasisApiException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@ControllerAdvice
public class OasisErrorHandler extends ResponseEntityExceptionHandler {

    private static final HttpHeaders ERROR_HEADERS = new HttpHeaders();

    private final ErrorMessages errorMessages;

    static {
        ERROR_HEADERS.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
    }

    public OasisErrorHandler(ErrorMessages errorMessages) {
        this.errorMessages = errorMessages;
    }

    @ExceptionHandler(value = { OasisApiException.class })
    protected ResponseEntity<Object> handleValidationError(OasisApiException ex, WebRequest request) {
        Map<String, Object> data = new HashMap<>();
        data.put("timestamp", Instant.now().toString());
        data.put("status", ex.getStatusCode());
        data.put("errorCode", ex.getErrorCode());
        data.put("errorCodeDescription", errorMessages.getErrorMessage(ex.getErrorCode()));
        data.put("message", ex.getMessage());
        if (request instanceof ServletWebRequest) {
            data.put("path", ((ServletWebRequest) request).getRequest().getServletPath());
        } else {
            data.put("path", request.getContextPath());
        }
        return handleExceptionInternal(ex, data, ERROR_HEADERS, HttpStatus.valueOf(ex.getStatusCode()), request);
    }

}

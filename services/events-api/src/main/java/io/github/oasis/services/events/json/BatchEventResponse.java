/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.services.events.json;

import io.github.oasis.services.events.internal.exceptions.EventSubmissionException;

import java.io.Serializable;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
public class BatchEventResponse implements Serializable {

    private Map<String, EventResponse> successResponses;
    private Map<String, ErrorResponse> errorResponses;

    public BatchEventResponse addSuccessResponse(String eventId, EventResponse response) {
        successResponses.put(eventId, response);
        return this;
    };

    public BatchEventResponse addFailureResponse(String eventId, EventSubmissionException ex) {
        errorResponses.put(eventId, new ErrorResponse(ex.getMessage()));
        return this;
    }

    public static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }
    }
}

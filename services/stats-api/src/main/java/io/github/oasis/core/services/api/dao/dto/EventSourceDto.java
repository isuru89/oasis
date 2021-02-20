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

package io.github.oasis.core.services.api.dao.dto;

import io.github.oasis.core.model.EventSource;
import lombok.*;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSourceDto implements Serializable {

    private Integer id;
    private String token;
    private String name;

    private EventSourceSecretsDto secrets;
    private boolean active;

    public static EventSourceDto from(EventSource eventSource) {
        EventSourceDtoBuilder dto = EventSourceDto.builder()
                .id(eventSource.getId())
                .active(eventSource.isActive())
                .name(eventSource.getName())
                .token(eventSource.getToken());
        if (eventSource.getSecrets() != null) {
            dto.secrets(EventSourceSecretsDto.from(eventSource.getSecrets()));
        }
        return dto.build();
    }

    public EventSource toEventSource() {
        EventSource.EventSourceBuilder dto = EventSource.builder()
                .name(name)
                .id(id)
                .token(token)
                .active(this.active);
        if (secrets != null) {
            dto.secrets(secrets.toSecrets());
        }
        return dto.build();
    }

}

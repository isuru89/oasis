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

package io.github.oasis.services.events.internal;

import io.github.oasis.services.events.json.NewEvent;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

/**
 * @author Isuru Weerarathna
 */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class OasisTypeMatcher implements InvalidEventTypeMatcher {

    private final AntPathMatcher matcher = new AntPathMatcher(".");
    private static final String OASIS_EVENT_PATTERN = "oasis.**";

    @Override
    public boolean valid(NewEvent event) {
        return !matcher.matchStart(OASIS_EVENT_PATTERN, event.getEventType().toLowerCase());
    }
}

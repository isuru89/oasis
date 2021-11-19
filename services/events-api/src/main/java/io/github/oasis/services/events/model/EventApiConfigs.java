/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 *
 */

package io.github.oasis.services.events.model;

import io.vertx.core.json.JsonObject;

import java.io.Serializable;

/**
 * Stored configurations for event api related processings.
 *
 * @author Isuru Weerarathna
 */
public class EventApiConfigs implements Serializable {

    private boolean skipEventIntegrityCheck;

    public static EventApiConfigs create(JsonObject configRef) {
        EventApiConfigs configs = new EventApiConfigs();
        configs.setSkipEventIntegrityCheck(configRef.getBoolean("skipEventIntegrityCheck", false));

        return configs;
    }

    public boolean isSkipEventIntegrityCheck() {
        return skipEventIntegrityCheck;
    }

    public void setSkipEventIntegrityCheck(boolean skipEventIntegrityCheck) {
        this.skipEventIntegrityCheck = skipEventIntegrityCheck;
    }
}

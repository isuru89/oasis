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

package io.github.oasis.engine.elements.badges.rules;

/**
 * @author Isuru Weerarathna
 */

public class BadgeFirstEventRule extends BadgeRule {

    public static final int DEFAULT_ATTRIBUTE = 1;


    private final String eventName;
    private final int attributeId;

    public BadgeFirstEventRule(String id, String eventName) {
        this(id, eventName, DEFAULT_ATTRIBUTE);
    }

    public BadgeFirstEventRule(String id, String eventName, int attributeId) {
        super(id);

        this.eventName = eventName;
        this.attributeId = attributeId;
    }

    public String getEventName() {
        return eventName;
    }

    public int getAttributeId() {
        return attributeId;
    }
}

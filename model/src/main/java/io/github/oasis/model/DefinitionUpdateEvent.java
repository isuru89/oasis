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

package io.github.oasis.model;

import io.github.oasis.model.defs.BaseDef;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
public class DefinitionUpdateEvent implements Serializable {

    private DefinitionUpdateType type;
    private BaseDef baseDef;

    public DefinitionUpdateEvent() {
    }

    public DefinitionUpdateEvent(DefinitionUpdateType type, BaseDef baseDef) {
        this.type = type;
        this.baseDef = baseDef;
    }

    public DefinitionUpdateType getType() {
        return type;
    }

    public void setType(DefinitionUpdateType type) {
        this.type = type;
    }

    public BaseDef getBaseDef() {
        return baseDef;
    }

    public void setBaseDef(BaseDef baseDef) {
        this.baseDef = baseDef;
    }

    public static DefinitionUpdateEvent create(DefinitionUpdateType type, BaseDef def) {
        return new DefinitionUpdateEvent(type, def);
    }
}

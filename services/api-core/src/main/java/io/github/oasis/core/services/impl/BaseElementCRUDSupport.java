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

package io.github.oasis.core.services.impl;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.exception.OasisException;
import io.github.oasis.core.services.ElementCRUDSupport;

/**
 * @author Isuru Weerarathna
 */
public class BaseElementCRUDSupport implements ElementCRUDSupport {

    private final String elementType;

    public BaseElementCRUDSupport(String elementType) {
        this.elementType = elementType;
    }

    @Override
    public ElementDef readElementDefinition(int gameId, String id) throws OasisException {
        return null;
    }

    @Override
    public ElementDef updateElementDefinition(int gameId, String id, ElementDef replacement) throws OasisException {
        return null;
    }

    @Override
    public ElementDef addElementDefinition(int gameId, String id, ElementDef replacement) throws OasisException {
        return null;
    }

    @Override
    public ElementDef deleteElementDefinition(int gameId, String id) throws OasisException {
        return null;
    }
}

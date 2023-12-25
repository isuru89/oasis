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

package io.github.oasis.core.services.api.services;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.services.api.to.ElementCreateRequest;
import io.github.oasis.core.services.api.to.ElementUpdateRequest;
import io.github.oasis.core.services.exceptions.OasisApiException;
import io.github.oasis.core.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Base element services which allows CRUD operations related to all game rules.
 *
 * @author Isuru Weerarathna
 */
public interface IElementService {

    ElementDef readElement(int gameId, String elementId, boolean withData) throws OasisApiException;

    ElementDef addElement(int gameId, ElementCreateRequest request) throws OasisParseException;

    ElementDef updateElement(int gameId, String elementId, ElementUpdateRequest updateRequest);

    ElementDef deleteElement(int gameId, String elementId);

    default List<ElementDef> listElementsByTypes(int gameId, Set<String> types) {
        List<ElementDef> elements = new ArrayList<>();

        if (Utils.isNotEmpty(types)) {
            for (String type : types) {
                elements.addAll(listElementsByType(gameId, type));
            }
        }  else {
            return listElementsFromGameId(gameId);
        }

        return elements;
    }

    List<ElementDef> listElementsByType(int gameId, String type);

    List<ElementDef> listElementsFromGameId(int gameId);


}

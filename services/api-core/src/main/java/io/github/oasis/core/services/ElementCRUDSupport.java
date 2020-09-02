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

package io.github.oasis.core.services;

import io.github.oasis.core.elements.ElementDef;
import io.github.oasis.core.exception.OasisException;

/**
 * @author Isuru Weerarathna
 */
public interface ElementCRUDSupport {

    /**
     * Reads a single element definition scoped to the game.
     *
     * @param gameId game id to insert this element.
     * @param id element unique id.
     * @return read element. Always non-null.
     * @throws OasisException when no such element exists or db access exception.
     */
    ElementDef readElementDefinition(int gameId, String id) throws OasisException;

    /**
     * Updated the specified element by id and replaces it with given one.
     *
     * @param gameId game id to update this element.
     * @param id element unique id.
     * @param replacement new element instance to persist.
     * @return updated element definition.
     * @throws OasisException when no such element exists by id or db access exception
     */
    ElementDef updateElementDefinition(int gameId, String id, ElementDef replacement) throws OasisException;

    /**
     * Adds a new element definition to the game.
     *
     * @param gameId game id to insert this element.
     * @param id new element unique id.
     * @param replacement new element instance to add.
     * @return added element definition.
     * @throws OasisException when an element is already exist by id or db access exception.
     */
    ElementDef addElementDefinition(int gameId, String id, ElementDef replacement) throws OasisException;

    /**
     * Deletes the element definition in the game.
     *
     * @param gameId game id to insert this element.
     * @param id element to delete.
     * @return removed element definition.
     * @throws OasisException no such element exist by given id or db access exception.
     */
    ElementDef deleteElementDefinition(int gameId, String id) throws OasisException;

}

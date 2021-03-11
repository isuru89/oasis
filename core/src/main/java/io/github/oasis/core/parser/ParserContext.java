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

package io.github.oasis.core.parser;

import io.github.oasis.core.exception.OasisParseException;

import java.io.InputStream;

/**
 * Interface for storing parser related context.
 *
 * @author Isuru Weerarathna
 */
public interface ParserContext {

    /**
     * Reads the given path relative to the context specification.
     *
     * @param fullPath path to load. Path will be provided as full path relevant to the context.
     * @return parsed object.
     * @throws OasisParseException any exception thrown while parsing.
     */
    InputStream loadPath(String fullPath) throws OasisParseException;

    /**
     * Sets the current parsing location to the stack
     *
     * @param currentLocation current location
     */
    void pushCurrentLocation(String currentLocation);

    /**
     * Removes the current location from stack.
     */
    void popCurrentLocation();

    /**
     * Returns full path for the given relative path by combining it with
     * the already set current path.
     *
     * @param relativePath given relative path to the current path
     * @return full absolute path that can be load from
     * @throws OasisParseException if paths are incorrect or cannot be derived
     */
    String manipulateFullPath(String relativePath) throws OasisParseException;

}


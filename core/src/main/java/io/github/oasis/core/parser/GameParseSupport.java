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

import io.github.oasis.core.elements.GameDef;
import io.github.oasis.core.exception.OasisParseException;

import java.io.InputStream;

/**
 * Parses a game file and returns a game engine compatible instance.
 *
 * The version based parsing must be taken care by the corresponding implementation itself.
 *
 * @author Isuru Weerarathna
 */
public interface GameParseSupport {

    /**
     * Parses the given input stream and converts to a game definition instance.
     *
     * @param input input stream to read
     * @param parserContext parser context.
     * @return parsed game definition object.
     * @throws OasisParseException throws when any error occurred while parsing.
     */
    GameDef parse(InputStream input, ParserContext parserContext) throws OasisParseException;

}

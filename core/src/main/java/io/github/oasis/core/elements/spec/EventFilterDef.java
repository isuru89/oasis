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

package io.github.oasis.core.elements.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Texts;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
public class EventFilterDef implements Validator, Serializable {

    @DefinitionDetails(description = "Expression as a script.")
    private String expression;

    private String className;

    @Override
    public void validate() throws OasisParseException {
        if (Texts.isEmpty(expression) && Texts.isEmpty(className)) {
            throw new OasisParseException("Either 'expression' or 'className' must be specified in filter!");
        }
    }

    public static EventFilterDef withExpression(String expression) {
        EventFilterDef def = new EventFilterDef();
        def.setExpression(expression);
        return def;
    }
}

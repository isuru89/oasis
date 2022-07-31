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

package io.github.oasis.elements.milestones.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Texts;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
@Data
public class ValueExtractorDef implements Validator, Serializable {


    @DefinitionDetails(description = "Expression to extract aggregating value from event data.")
    private String expression;

    private String className;

    @DefinitionDetails(description = "Fixed amount of value to be aggregated when the event is matched.")
    private BigDecimal amount;

    @Override
    public void validate() throws OasisParseException {
        if (Texts.isEmpty(expression) && Texts.isEmpty(className) && amount == null) {
            throw new OasisParseException("At least one of 'expression', 'className' or 'amount' field must be specified!");
        }
    }
}

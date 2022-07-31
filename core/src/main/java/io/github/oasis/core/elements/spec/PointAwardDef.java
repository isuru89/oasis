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
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Isuru Weerarathna
 */
@Data
@NoArgsConstructor
public class PointAwardDef implements Validator, Serializable {

    @DefinitionDetails(description = "Referenced point id")
    private String id;

    @DefinitionDetails(description = "Amount of points to reward")
    private BigDecimal amount;

    @DefinitionDetails(description = "Scripted expression to derive rewarding points based on event data")
    private String expression;

    @Override
    public void validate() throws OasisParseException {
        Validate.notEmpty(id, "Mandatory field 'id' is missing!");

        if (amount == null && Texts.isEmpty(expression)) {
            throw new OasisParseException("Either 'amount' or 'expression' field must be specified!");
        }
    }
}

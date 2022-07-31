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

package io.github.oasis.engine.element.points.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class CappedDef implements Validator, Serializable {

    @DefinitionDetails(description = "Fixed amount of points to be rewarded.",
        valueSet = {"daily", "weekly", "monthly", "quarterly", "annually"})
    private String duration;

    @DefinitionDetails(description = "Maximum number of points can be rewarded to a player for the specified duration.")
    private BigDecimal limit;

    @Override
    public void validate() throws OasisParseException {
        Validate.notNull(limit, "Field 'limit' must be specified!");
        Validate.notEmpty(duration, "Field 'duration' must be specified!");
    }
}

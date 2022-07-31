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
import io.github.oasis.core.utils.Utils;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@Data
public class AcceptsWithinDef implements Validator, Serializable {

    @DefinitionDetails(
            description = "Filters an event if any of time range matched",
            parameterizedType = TimeRangeDef.class)
    private List<TimeRangeDef> anyOf;

    @DefinitionDetails(
            description = "Filters an event only when all of the given time ranges matched",
            parameterizedType = TimeRangeDef.class)
    private List<TimeRangeDef> allOf;

    @Override
    public void validate() throws OasisParseException {
        if (Utils.isEmpty(anyOf) && Utils.isEmpty(allOf)) {
            throw new OasisParseException("Either 'allOf' or 'anyOf' must be specified under accepted time ranges!");
        }

        if (Utils.isNotEmpty(anyOf) && Utils.isNotEmpty(allOf)) {
            throw new OasisParseException("Not allowed to specify both 'allOf' and 'anyOf' together! Only one can be specified!");
        }

        if (Utils.isNotEmpty(anyOf)) {
            anyOf.forEach(TimeRangeDef::validate);
        }
        if (Utils.isNotEmpty(allOf)) {
            allOf.forEach(TimeRangeDef::validate);
        }
    }
}

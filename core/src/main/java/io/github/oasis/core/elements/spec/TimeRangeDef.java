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

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
@NoArgsConstructor
public class TimeRangeDef implements Validator, Serializable {

    @DefinitionDetails(valueSet = {"seasonal", "time", "weekly", "custom", "absolute"},
            description = "Time range type")
    private String type;
    @DefinitionDetails(possibleTypes = {"string", "number"},
    description = "Time range start as a string or in epoch milliseconds")
    private Object from;
    @DefinitionDetails(possibleTypes = {"string", "number"},
            description = "Time range end as a string or in epoch milliseconds")
    private Object to;

    @DefinitionDetails(possibleTypes = {"string"},
            description = "Comma separated weekdays when 'weekly' type is specified")
    private Object when;

    @DefinitionDetails(description =
            "Custom script to filter out by event epoch millisecond timestamp which will be provided as 'ts' variable.")
    private Object expression;

    public TimeRangeDef(String type, Object from, Object to) {
        this.type = type;
        this.from = from;
        this.to = to;
    }

    @Override
    public void validate() throws OasisParseException {
        if (Texts.isEmpty(type)) {
            throw new OasisParseException("Field 'type' cannot be empty!");
        }
    }
}

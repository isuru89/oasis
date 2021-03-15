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

import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Texts;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Getter
@Setter
@NoArgsConstructor
public class TimeRangeDef implements Validator, Serializable {
    private String type;
    private Object from;
    private Object to;
    private Object when;
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

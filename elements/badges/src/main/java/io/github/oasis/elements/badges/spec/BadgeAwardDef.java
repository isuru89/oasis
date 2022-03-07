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

package io.github.oasis.elements.badges.spec;

import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.exception.OasisParseException;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
public class BadgeAwardDef implements Validator, Serializable {

    /**
     * A custom badge id to be used instead of rule id.
     */
    private String id;

    /**
     * Maximum how many time should a user be awarded this badge.
     */
    private Integer maxAwardTimes;

    /**
     * Badge attribute id (like, Gold, Silver or Bronze)
     */
    private Integer attribute;

    @Override
    public void validate() throws OasisParseException {
        if (maxAwardTimes == null && attribute == null) {
            throw new OasisParseException("Either one of 'maxAwardTimes' or 'attributes' field must be specified!");
        }
    }
}

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

package io.github.oasis.elements.challenges.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.Validator;
import io.github.oasis.core.elements.spec.PointAwardDef;
import io.github.oasis.core.exception.OasisParseException;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
public class ChallengeRewardDef implements Validator, Serializable {

    /**
     * When a user wins, the type of point id to be awarded.
     * When not specified, challenge id will be used.
     */
    @DefinitionDetails(description = "Points to be rewarded for a winning player")
    private PointAwardDef points;

    @Override
    public void validate() throws OasisParseException {

    }
}

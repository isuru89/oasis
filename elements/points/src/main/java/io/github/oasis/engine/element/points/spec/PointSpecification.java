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
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.exception.OasisParseException;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PointSpecification extends BaseSpecification {

    @DefinitionDetails(description = "Indicates how many points should be awarded to the user once condition satisfies."
                    + "This can be a number or expression based on event data.")
    private PointRewardDef reward;

    @DefinitionDetails(description = "If specified, then this indicates how many maximum points can be earned "
            + "from this rule for the specified constrained time period.")
    private CappedDef cap;

    @Override
    public void validate() throws OasisParseException {
        super.validate();

        if (reward == null) {
            throw new OasisParseException("Field 'reward' must be specified!");
        }
        reward.validate();

        if (cap != null) {
            cap.validate();
        }
    }
}

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
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class MilestoneSpecification extends BaseSpecification {

    /**
     * Optional expression to extract accumulation value from events.
     * When pointIds are specified, this field will be ignored.
     */
    @DefinitionDetails(description = "Optional expression to extract accumulation value from events." +
            "When pointIds are specified, this field will be ignored.")
    private ValueExtractorDef valueExtractor;

    /**
     * Mandatory Level list for this milestone.
     */
    @DefinitionDetails(description = "Milestone levels", parameterizedType = MilestoneLevel.class)
    private List<MilestoneLevel> levels;

    @Override
    public void validate() throws OasisParseException {
        super.validate();

        if (Utils.isEmpty(levels)) {
            throw new OasisParseException("At least one level must be specified in milestone definition!");
        }
    }
}

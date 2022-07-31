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

package io.github.oasis.elements.ratings.spec;

import io.github.oasis.core.annotations.DefinitionDetails;
import io.github.oasis.core.elements.spec.BaseSpecification;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Utils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Validate;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RatingSpecification extends BaseSpecification {

    /**
     * Default rating to award, when no criteria are satisfied.
     *
     * Note: by default, the default rating will not be set to all users unless
     * at least one event is processed against a user.
     */
    @DefinitionDetails(description = "Default rating to award, when no criteria are satisfied.")
    private Integer defaultRating;

    /**
     * List of ratings.
     */
    @DefinitionDetails(description = "Set of ratings.", parameterizedType = ARatingDef.class)
    private List<ARatingDef> ratings;

    @Override
    public void validate() throws OasisParseException {
        super.validate();

        Validate.notNull(defaultRating, "Field 'defaultRating' must be specified!");
        if (Utils.isEmpty(ratings)) {
            throw new OasisParseException("At least one rating in 'ratings' field must be specified!");
        }

        ratings.forEach(ARatingDef::validate);
    }
}

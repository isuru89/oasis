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
import io.github.oasis.core.elements.spec.PointAwardDef;
import io.github.oasis.core.exception.OasisParseException;
import io.github.oasis.core.utils.Utils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.Validate;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class RewardDef implements Validator, Serializable {

    private BadgeAwardDef badge;

    private PointAwardDef points;

    @Override
    public void validate() throws OasisParseException {
        Validate.notNull(badge, "Mandatory field 'badge' is missing in rewards definition!");
        badge.validate();

        if (points != null) {
            points.validate();
        }
    }

    public static RewardDef merge(RewardDef to, RewardDef from) {
        if (to == null) {
            return from;
        } else if (from == null) {
            return to;
        }

        RewardDefBuilder builder = to.toBuilder();
        if (from.getBadge() != null) {
            builder.badge(merge(to.getBadge(), from.getBadge()));
        }
        if (from.getPoints() != null) {
            builder.points(merge(to.getPoints(), from.getPoints()));
        }
        return builder.build();
    }

    private static BadgeAwardDef merge(BadgeAwardDef to, BadgeAwardDef from) {
        BadgeAwardDef def = new BadgeAwardDef();
        def.setId(Utils.firstNonNull(from.getId(), to.getId()));
        def.setAttribute(Utils.firstNonNull(from.getAttribute(), to.getAttribute()));
        def.setMaxAwardTimes(Utils.firstNonNull(from.getMaxAwardTimes(), to.getMaxAwardTimes()));
        return def;
    }

    private static PointAwardDef merge(PointAwardDef to, PointAwardDef from) {
        PointAwardDef def = new PointAwardDef();
        def.setId(Utils.firstNonNull(from.getId(), to.getId()));
        def.setAmount(Utils.firstNonNull(from.getAmount(), to.getAmount()));
        def.setExpression(Utils.firstNonNull(from.getExpression(), to.getExpression()));
        return def;
    }

    public static RewardDef withAttribute(int attribute) {
        RewardDef def = new RewardDef();
        BadgeAwardDef awardDef = new BadgeAwardDef();
        awardDef.setAttribute(attribute);
        def.setBadge(awardDef);
        return def;
    }

    public static RewardDef attributeWithMax(int attribute, int maxAllowed) {
        RewardDef def = new RewardDef();
        BadgeAwardDef awardDef = new BadgeAwardDef();
        awardDef.setAttribute(attribute);
        awardDef.setMaxAwardTimes(maxAllowed);
        def.setBadge(awardDef);
        return def;
    }

    public static RewardDef withAll(String id, int attribute, int maxAllowed) {
        RewardDef def = new RewardDef();
        BadgeAwardDef awardDef = new BadgeAwardDef();
        awardDef.setId(id);
        awardDef.setAttribute(attribute);
        awardDef.setMaxAwardTimes(maxAllowed);
        def.setBadge(awardDef);
        return def;
    }
}

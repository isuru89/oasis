/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.oasis.game.parser;

import io.github.oasis.model.Milestone;
import io.github.oasis.model.Parsers;
import io.github.oasis.model.defs.MilestoneDef;
import io.github.oasis.model.defs.MilestonesDef;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class MilestoneParser {

    public static List<Milestone> parse(List<MilestoneDef> milestoneDefs) {
        List<Milestone> milestones = new LinkedList<>();
        for (MilestoneDef milestoneDef : milestoneDefs) {
            Milestone milestone = Parsers.parse(milestoneDef);
            milestones.add(milestone);
        }
        return milestones;
    }

    public static List<Milestone> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        MilestonesDef milestonesDef = yaml.loadAs(inputStream, MilestonesDef.class);

        return parse(milestonesDef.getMilestones());
    }

}

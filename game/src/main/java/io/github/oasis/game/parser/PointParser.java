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

import io.github.oasis.model.Parsers;
import io.github.oasis.model.defs.PointDef;
import io.github.oasis.model.defs.PointsDef;
import io.github.oasis.model.rules.PointRule;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class PointParser {

    public static List<PointRule> parse(List<PointDef> pointDefs) {
        List<PointRule> pointRules = new LinkedList<>();
        for (PointDef record : pointDefs) {
            PointRule pr = Parsers.parse(record);
            pointRules.add(pr);
        }
        return pointRules;
    }

    public static List<PointRule> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        PointsDef pointsDef = yaml.loadAs(inputStream, PointsDef.class);

        return parse(pointsDef.getPoints());
    }

}

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
import io.github.oasis.model.Rating;
import io.github.oasis.model.defs.RatingDef;
import io.github.oasis.model.defs.RatingsDef;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

public class RatingsParser {

    public static List<Rating> parse(List<RatingDef> stateDefs) throws IOException {
        if (stateDefs == null) {
            return new LinkedList<>();
        }
        // convert def to state
        List<Rating> states = new LinkedList<>();
        for (RatingDef def : stateDefs) {
            Rating oState = Parsers.parse(def);
            states.add(oState);
        }
        return states;
    }

    public static List<Rating> parse(InputStream inputStream) throws IOException {
        Yaml yaml = new Yaml();
        RatingsDef statesDef = yaml.loadAs(inputStream, RatingsDef.class);

        return parse(statesDef.getRatings());
    }

}

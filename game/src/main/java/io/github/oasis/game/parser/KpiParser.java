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

import io.github.oasis.model.FieldCalculator;
import io.github.oasis.model.Parsers;
import io.github.oasis.model.defs.KpiDef;
import io.github.oasis.model.defs.KpisDef;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author iweerarathna
 */
public class KpiParser {

    public static List<FieldCalculator> parse(List<KpiDef> calculations) {
        List<FieldCalculator> calculators = new LinkedList<>();
        for (KpiDef item: calculations) {
            FieldCalculator calculator = Parsers.parse(item);

            calculators.add(calculator);
        }
        return calculators;
    }

    public static List<FieldCalculator> parse(InputStream inputStream) {
        Yaml yaml = new Yaml();
        KpisDef calcs = yaml.loadAs(inputStream, KpisDef.class);
        List<KpiDef> calculations = calcs.getCalculations();

        return parse(calculations);
    }

}

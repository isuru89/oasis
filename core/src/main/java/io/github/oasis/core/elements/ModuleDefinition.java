/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.github.oasis.core.elements;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class ModuleDefinition implements Serializable {

    private String id;
    private Specs specs;

    @Data
    public static class Specs implements Serializable {
        private Map<String, Object> feeds;
        private Map<String, Object> rules;

        public void addRuleSpec(String ruleId, Object spec) {
            if (rules == null) {
                rules = new HashMap<>();
            }
            rules.put(ruleId, spec);
        }

        public void addFeedSpec(String id, Object spec) {
            if (feeds == null) {
                feeds = new HashMap<>();
            }
            feeds.put(id, spec);
        }

    }
}

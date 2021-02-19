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

package io.github.oasis.core.external.messages;

import io.github.oasis.core.elements.AbstractRule;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Base command class for all rule related changes.
 * See {@link RuleChangeType} for available list of rule activities.
 *
 * @author Isuru Weerarathna
 */
@Getter
@Setter
@ToString
public class RuleCommand implements OasisCommand {

    private int gameId;
    private RuleChangeType changeType;
    private AbstractRule rule;

    public enum RuleChangeType {
        ADD,
        UPDATE,
        REMOVE
    }

}

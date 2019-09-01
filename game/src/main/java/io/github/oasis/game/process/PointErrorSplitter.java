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

package io.github.oasis.game.process;

import io.github.oasis.model.events.ErrorPointEvent;
import io.github.oasis.model.events.PointEvent;
import org.apache.flink.streaming.api.collector.selector.OutputSelector;

import java.util.Collections;

/**
 * @author iweerarathna
 */
public class PointErrorSplitter implements OutputSelector<PointEvent> {

    public static final String NAME_ERROR = "ERROR";
    public static final String NAME_POINT = "POINT";

    private static final Iterable<String> ERROR_LIST = Collections.singletonList(NAME_ERROR);
    private static final Iterable<String> POINT_LIST = Collections.singletonList(NAME_POINT);

    @Override
    public Iterable<String> select(PointEvent value) {
        if (value instanceof ErrorPointEvent) {
            return ERROR_LIST;
        } else {
            return POINT_LIST;
        }
    }
}

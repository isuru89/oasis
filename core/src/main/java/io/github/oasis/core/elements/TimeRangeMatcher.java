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

package io.github.oasis.core.elements;

import java.io.Serializable;

/**
 * This interface provides a way to filter out events which belongs to
 * a certain time range, so that only those event will be passed to rule
 * execution.
 *
 * @author Isuru Weerarathna
 */
@FunctionalInterface
public interface TimeRangeMatcher extends Serializable {

    /**
     * Returns true if given time falls into any of ranges specified.
     * @param timeMs epoch time in milliseconds
     * @param timeZone timezone to check range for.
     * @return whether time falls to the range or not.
     */
    boolean isBetween(long timeMs, String timeZone);

}

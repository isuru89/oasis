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

package io.github.oasis.model;

/**
 * @author iweerarathna
 */
public enum AggregatorType {

    SUM(true),
    COUNT(true),

    AVG(true),
    MIN(false),
    MAX(false);

    AggregatorType(boolean multiAggregatable) {
        this.multiAggregatable = multiAggregatable;
    }

    // whether this aggregation type can handle multiple heterogeneous tables at once
    // for eg: for now OA_POINT, and OA_RATING
    private final boolean multiAggregatable;

    public static AggregatorType from(String text) {
        for (AggregatorType val : values()) {
            if (val.name().equalsIgnoreCase(text)) {
                return val;
            }
        }
        return null;
    }

    public boolean isMultiAggregatable() {
        return multiAggregatable;
    }}

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

package io.github.oasis.services.admin.internal.dto;

import java.util.List;

/**
 * @author Isuru Weerarathna
 */
public class ExtAppUpdateResult {

    private List<String> addedEventTypes;
    private List<Integer> addedGameIds;
    private List<String> removedEventTypes;
    private List<Integer> removedGameIds;

    private ExtAppUpdateResult(List<String> addedEventTypes,
                              List<Integer> addedGameIds,
                              List<String> removedEventTypes,
                              List<Integer> removedGameIds) {
        this.addedEventTypes = addedEventTypes;
        this.addedGameIds = addedGameIds;
        this.removedEventTypes = removedEventTypes;
        this.removedGameIds = removedGameIds;
    }

    public List<String> getAddedEventTypes() {
        return addedEventTypes;
    }

    public List<Integer> getAddedGameIds() {
        return addedGameIds;
    }

    public List<String> getRemovedEventTypes() {
        return removedEventTypes;
    }

    public List<Integer> getRemovedGameIds() {
        return removedGameIds;
    }

    public static class ExtAppUpdateResultBuilder {
        private List<String> addedEventTypes;
        private List<Integer> addedGameIds;
        private List<String> removedEventTypes;
        private List<Integer> removedGameIds;

        public ExtAppUpdateResultBuilder addedEventTypes(List<String> addedEventTypes) {
            this.addedEventTypes = addedEventTypes;
            return this;
        }

        public ExtAppUpdateResultBuilder addedGameIds(List<Integer> addedGameIds) {
            this.addedGameIds = addedGameIds;
            return this;
        }

        public ExtAppUpdateResultBuilder removedEventTypes(List<String> removedEventTypes) {
            this.removedEventTypes = removedEventTypes;
            return this;
        }

        public ExtAppUpdateResultBuilder removedGameIds(List<Integer> removedGameIds) {
            this.removedGameIds = removedGameIds;
            return this;
        }

        public ExtAppUpdateResult create() {
            return new ExtAppUpdateResult(addedEventTypes, addedGameIds, removedEventTypes, removedGameIds);
        }
    }
}

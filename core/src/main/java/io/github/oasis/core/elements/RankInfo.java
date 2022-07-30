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

package io.github.oasis.core.elements;

import lombok.*;

import java.io.Serializable;

/**
 * @author Isuru Weerarathna
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RankInfo implements Serializable {

    private Integer id;
    private String name;
    private int priority;
    private String colorCode;

    public RankInfo(Integer id, String name, Integer priority) {
        this(id, name, priority, null);
    }

}

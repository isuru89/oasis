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

package io.github.oasis.services.dto.game;

public class RankingRecord {

    private Integer rank;
    private Double myValue;
    private Long myCount;
    private Double nextValue;
    private Double topValue;

    public Double getMyValue() {
        return myValue;
    }

    public void setMyValue(Double myValue) {
        this.myValue = myValue;
    }

    public Long getMyCount() {
        return myCount;
    }

    public void setMyCount(Long myCount) {
        this.myCount = myCount;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Double getNextValue() {
        return nextValue;
    }

    public void setNextValue(Double nextValue) {
        this.nextValue = nextValue;
    }

    public Double getTopValue() {
        return topValue;
    }

    public void setTopValue(Double topValue) {
        this.topValue = topValue;
    }

}

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

package io.github.oasis.engine.element.points.stats.to;

import io.github.oasis.core.services.AbstractStatsApiResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class UserRankingSummary extends AbstractStatsApiResponse {

    private Long userId;

    private Map<String, RankInfo> rankings;

    public void addRankingDetail(String key, RankInfo value) {
        if (rankings == null) {
            rankings = new HashMap<>();
        }
        rankings.put(key, value);
    }

    @Data
    @NoArgsConstructor
    public static class RankInfo {
        private int rank;
        private BigDecimal score;
        private Long total;

        public RankInfo(int rank, BigDecimal score, Long total) {
            this.rank = rank;
            this.score = score;
            this.total = total;
        }
    }

}

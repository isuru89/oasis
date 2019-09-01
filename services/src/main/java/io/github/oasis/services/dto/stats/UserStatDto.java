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

package io.github.oasis.services.dto.stats;

/**
 * @author iweerarathna
 */
public class UserStatDto {

    private int userId;
    private String userName;
    private String userEmail;

    private double totalPoints;
    private long totalBadges;
    private long totalTrophies;
    private long totalItems;
    private double amountSpent;

    private double deltaPoints;
    private long deltaBadges;
    private long deltaTrophies;
    private long deltaItems;
    private double deltaAmountSpent;

    public static class StatRecord {
        private Long userId;
        private String userName;
        private String userEmail;
        private String type;
        private Long value_i;
        private Double value_f;

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public Long getValue_i() {
            return value_i;
        }

        public void setValue_i(Long value_i) {
            this.value_i = value_i;
        }

        public Double getValue_f() {
            return value_f;
        }

        public void setValue_f(Double value_f) {
            this.value_f = value_f;
        }
    }

    public double getDeltaAmountSpent() {
        return deltaAmountSpent;
    }

    public void setDeltaAmountSpent(double deltaAmountSpent) {
        this.deltaAmountSpent = deltaAmountSpent;
    }

    public double getAmountSpent() {
        return amountSpent;
    }

    public void setAmountSpent(double amountSpent) {
        this.amountSpent = amountSpent;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public double getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(double totalPoints) {
        this.totalPoints = totalPoints;
    }

    public long getTotalBadges() {
        return totalBadges;
    }

    public void setTotalBadges(long totalBadges) {
        this.totalBadges = totalBadges;
    }

    public long getTotalTrophies() {
        return totalTrophies;
    }

    public void setTotalTrophies(long totalTrophies) {
        this.totalTrophies = totalTrophies;
    }

    public long getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(long totalItems) {
        this.totalItems = totalItems;
    }

    public double getDeltaPoints() {
        return deltaPoints;
    }

    public void setDeltaPoints(double deltaPoints) {
        this.deltaPoints = deltaPoints;
    }

    public long getDeltaBadges() {
        return deltaBadges;
    }

    public void setDeltaBadges(long deltaBadges) {
        this.deltaBadges = deltaBadges;
    }

    public long getDeltaTrophies() {
        return deltaTrophies;
    }

    public void setDeltaTrophies(long deltaTrophies) {
        this.deltaTrophies = deltaTrophies;
    }

    public long getDeltaItems() {
        return deltaItems;
    }

    public void setDeltaItems(long deltaItems) {
        this.deltaItems = deltaItems;
    }

    @Override
    public String toString() {
        return "UserStatDto{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", totalPoints=" + totalPoints +
                ", totalBadges=" + totalBadges +
                ", totalTrophies=" + totalTrophies +
                ", totalItems=" + totalItems +
                ", amountSpent=" + amountSpent +
                ", deltaPoints=" + deltaPoints +
                ", deltaBadges=" + deltaBadges +
                ", deltaTrophies=" + deltaTrophies +
                ", deltaItems=" + deltaItems +
                ", deltaAmountSpent=" + deltaAmountSpent +
                '}';
    }
}

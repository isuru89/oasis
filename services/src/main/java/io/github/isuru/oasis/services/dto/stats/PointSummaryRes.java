package io.github.isuru.oasis.services.dto.stats;

import java.util.List;

public class PointSummaryRes {

    private long count;
    private List<PointSummaryRecord> records;

    public static class PointSummaryRecord {
        private Long userId;
        private Long teamId;
        private Long teamScopeId;

        private Integer pointId;
        private String pointName;
        private String pointDisplayName;

        private Double totalPoints;
        private Long occurrences;
        private Double minAchieved;
        private Double maxAchieved;
        private Long firstAchieved;
        private Long lastAchieved;

        public String getPointName() {
            return pointName;
        }

        public void setPointName(String pointName) {
            this.pointName = pointName;
        }

        public String getPointDisplayName() {
            return pointDisplayName;
        }

        public void setPointDisplayName(String pointDisplayName) {
            this.pointDisplayName = pointDisplayName;
        }

        public Long getTeamId() {
            return teamId;
        }

        public void setTeamId(Long teamId) {
            this.teamId = teamId;
        }

        public Long getTeamScopeId() {
            return teamScopeId;
        }

        public void setTeamScopeId(Long teamScopeId) {
            this.teamScopeId = teamScopeId;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public Integer getPointId() {
            return pointId;
        }

        public void setPointId(Integer pointId) {
            this.pointId = pointId;
        }

        public Double getTotalPoints() {
            return totalPoints;
        }

        public void setTotalPoints(Double totalPoints) {
            this.totalPoints = totalPoints;
        }

        public Long getOccurrences() {
            return occurrences;
        }

        public void setOccurrences(Long occurrences) {
            this.occurrences = occurrences;
        }

        public Double getMinAchieved() {
            return minAchieved;
        }

        public void setMinAchieved(Double minAchieved) {
            this.minAchieved = minAchieved;
        }

        public Double getMaxAchieved() {
            return maxAchieved;
        }

        public void setMaxAchieved(Double maxAchieved) {
            this.maxAchieved = maxAchieved;
        }

        public Long getFirstAchieved() {
            return firstAchieved;
        }

        public void setFirstAchieved(Long firstAchieved) {
            this.firstAchieved = firstAchieved;
        }

        public Long getLastAchieved() {
            return lastAchieved;
        }

        public void setLastAchieved(Long lastAchieved) {
            this.lastAchieved = lastAchieved;
        }
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public List<PointSummaryRecord> getRecords() {
        return records;
    }

    public void setRecords(List<PointSummaryRecord> records) {
        this.records = records;
    }
}

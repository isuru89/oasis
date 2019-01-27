package io.github.isuru.oasis.services.dto.stats;

public class UserCountStat {

    private long id;
    private long totalUsers;

    public long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}

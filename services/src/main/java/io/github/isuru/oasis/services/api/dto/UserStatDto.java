package io.github.isuru.oasis.services.api.dto;

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
        return "{" +
                "userId=" + userId +
                ", totalPoints=" + totalPoints +
                ", totalBadges=" + totalBadges +
                ", totalTrophies=" + totalTrophies +
                ", totalItems=" + totalItems +
                ", amountSpent=" + amountSpent +
                '}';
    }
}

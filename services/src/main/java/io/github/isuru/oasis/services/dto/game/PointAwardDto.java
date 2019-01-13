package io.github.isuru.oasis.services.dto.game;

/**
 * @author iweerarathna
 */
public class PointAwardDto {

    private long toUser;
    private float amount;
    private String associatedEventId;
    private Long ts;
    private Integer gameId;

    public String getAssociatedEventId() {
        return associatedEventId;
    }

    public void setAssociatedEventId(String associatedEventId) {
        this.associatedEventId = associatedEventId;
    }

    public long getToUser() {
        return toUser;
    }

    public void setToUser(long toUser) {
        this.toUser = toUser;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public Long getTs() {
        return ts;
    }

    public void setTs(Long ts) {
        this.ts = ts;
    }

    public Integer getGameId() {
        return gameId;
    }

    public void setGameId(Integer gameId) {
        this.gameId = gameId;
    }
}

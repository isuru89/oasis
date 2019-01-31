package io.github.isuru.oasis.services.dto.game;

public class RaceCalculationDto {

    private boolean doPersist = true;

    private long startTime;
    private long endTime;

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public boolean isDoPersist() {
        return doPersist;
    }

    public void setDoPersist(boolean doPersist) {
        this.doPersist = doPersist;
    }
}

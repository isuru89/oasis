package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class SubmittedJob {

    private Long id;
    private Long defId;
    private String jobId;
    private String jarId;
    private String snapshotDir;
    private boolean active;
    private long toBeFinishedAt;
    private byte[] stateData;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setToBeFinishedAt(long toBeFinishedAt) {
        this.toBeFinishedAt = toBeFinishedAt;
    }

    public byte[] getStateData() {
        return stateData;
    }

    public void setStateData(byte[] stateData) {
        this.stateData = stateData;
    }

    public Long getToBeFinishedAt() {
        return toBeFinishedAt;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getSnapshotDir() {
        return snapshotDir;
    }

    public void setSnapshotDir(String snapshotDir) {
        this.snapshotDir = snapshotDir;
    }

    public Long getDefId() {
        return defId;
    }

    public void setDefId(Long defId) {
        this.defId = defId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getJarId() {
        return jarId;
    }

    public void setJarId(String jarId) {
        this.jarId = jarId;
    }
}

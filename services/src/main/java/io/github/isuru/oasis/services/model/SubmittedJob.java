package io.github.isuru.oasis.services.model;

/**
 * @author iweerarathna
 */
public class SubmittedJob {

    private Integer defId;
    private String jobId;
    private String jarId;
    private String snapshotDir;
    private boolean active;
    private long toBeFinishedAt;

    public Long getToBeFinishedAt() {
        return toBeFinishedAt;
    }

    public void setToBeFinishedAt(Long toBeFinishedAt) {
        this.toBeFinishedAt = toBeFinishedAt;
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

    public Integer getDefId() {
        return defId;
    }

    public void setDefId(Integer defId) {
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

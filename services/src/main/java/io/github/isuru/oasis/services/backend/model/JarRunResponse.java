package io.github.isuru.oasis.services.backend.model;

public class JarRunResponse {

    private String jobid;

    public String getJobid() {
        return jobid;
    }

    public void setJobid(String jobid) {
        this.jobid = jobid;
    }

    @Override
    public String toString() {
        return "JarRunResponse{" +
                "jobid='" + jobid + '\'' +
                '}';
    }
}

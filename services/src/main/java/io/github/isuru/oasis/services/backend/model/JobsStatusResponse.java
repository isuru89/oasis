package io.github.isuru.oasis.services.backend.model;

import java.util.List;

/**
 * @author iweerarathna
 */
public class JobsStatusResponse {

    private List<JobStatus> jobs;

    public List<JobStatus> getJobs() {
        return jobs;
    }

    public void setJobs(List<JobStatus> jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return "JobsStatusResponse{" +
                "jobs=" + jobs +
                '}';
    }

    /**
     * Job status is one of those.
     *
     * "CREATED", "RUNNING", "FAILING", "FAILED", "CANCELLING",
     * "CANCELED", "FINISHED", "RESTARTING", "SUSPENDING", "SUSPENDED", "RECONCILING"
     *
     */
    public static class JobStatus {
        private String id;
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "JobStatus{" +
                    "id='" + id + '\'' +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

}

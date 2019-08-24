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

package io.github.oasis.services.services.backend.model;

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

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

package io.github.oasis.services.services;

import io.github.oasis.model.db.IOasisDao;
import io.github.oasis.services.model.SubmittedJob;
import io.github.oasis.services.utils.Checks;
import io.github.oasis.services.utils.Maps;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("jobService")
public class JobServiceImpl implements IJobService {

    @Autowired
    private IOasisDao dao;

    @Override
    public SubmittedJob readJob(long defId) throws Exception {
        Checks.greaterThanZero(defId, "defId");

        return ServiceUtils.getTheOnlyRecord(dao,
                Q.JOBS.GET_JOB,
                Maps.create("defId", defId),
                SubmittedJob.class);
    }

    @Override
    public boolean stopJob(String jobId) throws Exception {
        Checks.nonNullOrEmpty(jobId, "jobId");

        return dao.executeCommand(Q.JOBS.STOP_JOB, Maps.create("jobId", jobId)) > 0;
    }

    @Override
    public boolean stopJobByDef(long defId) throws Exception {
        Checks.greaterThanZero(defId, "defId");

        return dao.executeCommand(Q.JOBS.STOP_JOB_BY_DEF, Maps.create("defId", defId)) > 0;
    }

    @Override
    public List<SubmittedJob> listHadRunningJobs(long timeShutdown) throws Exception {
        Checks.greaterThanZero(timeShutdown, "timeShutdown");

        return ServiceUtils.toList(dao.executeQuery(Q.JOBS.GET_HAD_RUNNING_JOBS,
                Maps.create("currentTime", timeShutdown),
                SubmittedJob.class));
    }

    @Override
    public long submitJob(SubmittedJob job) throws Exception {
        Checks.nonNull(job, "job");
        Checks.greaterThanZero(job.getDefId(), "defId");
        Checks.nonNullOrEmpty(job.getJobId(), "jobId");
        Checks.greaterThanZero(job.getToBeFinishedAt(), "toBeFinishedAt");

        return dao.executeInsert(Q.JOBS.SUBMIT_JOB,
                Maps.create()
                    .put("defId", job.getDefId())
                    .put("jobId", job.getJobId())
                    .put("snapshotDir", job.getSnapshotDir())
                    .put("jarId", job.getJarId())
                    .put("toBeFinishedAt", job.getToBeFinishedAt())
                    .put("stateData", job.getStateData())
                    .build(),
                "id");
    }

    @Override
    public boolean updateJobState(long defId, byte[] stateData) throws Exception {
        Checks.greaterThanZero(defId, "defId");

        return dao.executeCommand(Q.JOBS.UPDATE_JOB,
                Maps.create("defId", defId,
                        "stateData", stateData)) > 0;
    }
}

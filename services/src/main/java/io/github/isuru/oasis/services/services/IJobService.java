package io.github.isuru.oasis.services.services;

import io.github.isuru.oasis.services.model.SubmittedJob;

import java.util.List;

public interface IJobService {

    SubmittedJob readJob(long defId) throws Exception;

    boolean stopJob(String jobId) throws Exception;

    boolean stopJobByDef(long defId) throws Exception;

    List<SubmittedJob> listHadRunningJobs(long timeShutdown) throws Exception;

    long submitJob(SubmittedJob job) throws Exception;

    boolean updateJobState(long defId, byte[] stateData) throws Exception;
}

INSERT INTO OA_JOB (
    def_id,
    jar_id,
    job_id,
    snapshot_dir,
    to_be_finished_at,
    state_data
) VALUES (
    :defId,
    :jarId,
    :jobId,
    :snapshotDir,
    :toBeFinishedAt,
    :stateData
)
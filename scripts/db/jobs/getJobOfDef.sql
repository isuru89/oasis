SELECT
    def_id as defId,
    job_id as jobId,
    jar_id as jarId,
    snapshot_dir AS snapshotDir,
    is_active as active,
    to_be_finished_at AS toBeFinishedAt,
    state_data AS stateData

FROM OA_JOB
WHERE
    def_id = :defId
    AND
    is_active = 1
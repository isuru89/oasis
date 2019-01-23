SELECT
    id AS id,
    def_id as defId,
    job_id as jobId,
    jar_id as jarId,
    snapshot_dir AS snapshotDir,
    is_active as active,
    to_be_finished_at AS toBeFinishedAt,
    state_data AS stateData

FROM OA_JOB
WHERE
    to_be_finished_at IS NOT NULL
    AND
    to_be_finished_at > :currentTime
    AND
    is_active = 1
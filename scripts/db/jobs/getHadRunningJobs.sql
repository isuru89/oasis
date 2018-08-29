SELECT
    def_id as defId,
    job_id as jobId,
    jar_id as jarId,
    is_active as active,
    to_be_finished_at AS toBeFinishedAt

FROM OA_JOB
WHERE
    to_be_finished_at IS NOT NULL
    AND
    to_be_finished_at > :currentTime
    AND
    is_active = 1
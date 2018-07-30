SELECT
    def_id as defId,
    job_id as jobId,
    jar_id as jarId

FROM OA_JOBS
WHERE
    def_id = :defId
    AND
    is_active = 1
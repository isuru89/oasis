UPDATE OA_JOBS
SET
    is_active = 0
WHERE
    job_id = :jobId
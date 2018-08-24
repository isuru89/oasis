UPDATE OA_JOB
SET
    is_active = 0
WHERE
    job_id = :jobId
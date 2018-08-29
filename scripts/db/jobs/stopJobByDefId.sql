UPDATE OA_JOB
SET
    is_active = 0
WHERE
    def_id = :defId
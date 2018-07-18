SELECT
    oms.user_id as userId,
    oms.milestone_id as milestoneId,
    mcur.currLevel as currentLevel,
    mcur.achievedTime as achievedTime,
    oms.current_val as currentValue,
    oms.current_val_i as currentValueL,
    oms.updated_at as lastUpdatedTime

FROM OA_MILESTONES_STATE AS oms
    LEFT JOIN (
        SELECT
            user_id as userId,
            milestone_id as milestoneId,
            MAX(level) as currLevel,
            MAX(ts) as achievedTime
        FROM OA_MILESTONES
        WHERE
            user_id = :userId AND is_active = 1
        GROUP BY
            user_id, milestone_id
    ) AS mcur
    ON oms.user_id = mcur.userId AND oms.milestone_id = mcur.milestoneId

WHERE
    user_id = :userId
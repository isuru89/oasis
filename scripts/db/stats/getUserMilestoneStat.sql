SELECT
    oms.user_id as userId,
    oms.milestone_id as milestoneId,
    oad.name as milestoneName,
    oad.display_name AS milestoneDisplayName,
    mcur.currLevel as currentLevel,
    oms.current_val as currentValue,
    oms.next_val as nextValue,
    oms.current_val_i as currentValueL,
    oms.next_val_i as nextValueL,
    mcur.achievedTime as achievedTime,
    oms.updated_at as lastUpdatedTime

FROM OA_MILESTONE_STATE AS oms
    LEFT JOIN (
        SELECT
            user_id as userId,
            milestone_id as milestoneId,
            MAX(level) as currLevel,
            MAX(ts) as achievedTime
        FROM OA_MILESTONE
        WHERE
            user_id = :userId AND is_active = 1
        GROUP BY
            user_id, milestone_id
    ) AS mcur ON oms.user_id = mcur.userId AND oms.milestone_id = mcur.milestoneId
    LEFT JOIN OA_DEFINITION oad ON oad.id = mcur.milestoneId

WHERE
    user_id = :userId
    AND
    oad.is_active = 1
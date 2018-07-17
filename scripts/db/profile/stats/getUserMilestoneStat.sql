SELECT
    oms.USER_ID as userId,
    oms.MILESTONE_ID as milestoneId,
    mcur.currLevel as currentLevel,
    mcur.achievedTime as achievedTime,
    oms.CURRENT_VAL as currentValue,
    oms.CURRENT_VAL_I as currentValueL,
    oms.UPDATED_AT as lastUpdatedTime

FROM OA_MILESTONES_STATE AS oms
    LEFT JOIN (
        SELECT
            USER_ID as userId,
            MILESTONE_ID as milestoneId,
            MAX(LEVEL) as currLevel,
            MAX(TS) as achievedTime
        FROM OA_MILESTONES
        WHERE
            USER_ID = :userId AND IS_ACTIVE = 1
        GROUP BY
            USER_ID, MILESTONE_ID
    ) AS mcur
    ON oms.USER_ID = mcur.userId AND oms.MILESTONE_ID = mcur.milestoneId

WHERE
    USER_ID = :userId
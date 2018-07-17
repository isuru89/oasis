INSERT INTO OA_MILESTONE_STATE (
    USER_ID,
    MILESTONE_ID,
    CURRENT_VAL,
    CURRENT_VAL_I
) VALUES (
    :userId,
    :milestoneId,
    :valueDouble,
    :valueLong
)
ON DUPLICATE KEY
UPDATE CURRENT_VAL = :valueDouble, CURRENT_VAL_I = :valueLong

INSERT INTO OA_MILESTONE_STATE (
    user_id,
    milestone_id,
    current_val,
    current_val_i
) VALUES (
    :userId,
    :milestoneId,
    :valueDouble,
    :valueLong
)
ON DUPLICATE KEY
UPDATE current_val = :valueDouble, current_val_i = :valueLong

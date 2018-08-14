INSERT INTO OA_MILESTONE_STATE (
    user_id,
    milestone_id,
    current_val,
    current_val_i,
    next_val,
    next_val_i,
    game_id
) VALUES (
    :userId,
    :milestoneId,
    :valueDouble,
    :valueLong,
    :nextVal,
    :nextValInt,
    :gameId
)
ON DUPLICATE KEY
UPDATE current_val = :valueDouble, current_val_i = :valueLong,
        next_val = :nextVal, next_val_i = :nextValInt

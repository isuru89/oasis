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
UPDATE current_val = VALUES(current_val),
    current_val_i = VALUES(current_val_i),
    next_val = VALUES(next_val),
    next_val_i = VALUES(next_val_i)

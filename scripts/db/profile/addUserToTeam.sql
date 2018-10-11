INSERT INTO OA_TEAM_USER (
    team_id,
    user_id,
    role_id,
    since,
    is_approved,
    approved_at
) VALUES (
    :teamId,
    :userId,
    :roleId,
    :since,
    :isApproved,
    :approvedAt
)
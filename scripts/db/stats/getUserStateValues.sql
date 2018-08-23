SELECT
    oas.user_id AS userId,
    oas.team_id AS teamId,
    oas.team_scope_id AS teamScopeId,
    oas.state_id as stateId,
    oad.name AS stateDefName,
    oad.display_name AS stateDefDisplayName,
    oas.current_state AS currentState,
    oas.current_value AS currentValue,
    oas.current_points AS currentPoints,
    oas.ext_id AS extId,
    oas.changed_at AS lastChangedAt

FROM OA_STATES oas
    INNER JOIN OA_DEFINITION oad ON oas.state_id = oad.id

WHERE
    oas.user_id = :userId
    AND
    oas.is_active = 1
    AND
    oad.is_active = 1

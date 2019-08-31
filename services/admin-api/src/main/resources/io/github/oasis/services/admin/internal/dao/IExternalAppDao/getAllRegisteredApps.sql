SELECT
    a.app_id a_id,
    a.name a_name,
    a.key_secret a_keySecret,
    a.key_public a_keyPublic,
    a.token a_token,
    a.is_internal a_internal,
    a.is_downloaded a_downloaded,
    a.for_all_games a_forAllGames,
    oeae.app_id et_id,
    oeae.event_type et_eventType,
    ogd.game_id g_id,
    ogd.name g_name,
    ogd.description g_description
FROM OA_EXT_APP a
INNER JOIN OA_EXT_APP_EVENT oeae ON a.app_id = oeae.app_id
LEFT JOIN OA_EXT_APP_GAME oeag ON a.app_id = oeag.app_id
LEFT JOIN OA_GAME_DEF ogd ON oeag.game_id = ogd.game_id
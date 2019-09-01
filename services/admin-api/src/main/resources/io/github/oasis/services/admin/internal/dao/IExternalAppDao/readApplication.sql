SELECT
    oea.app_id a_id,
    oea.name a_name,
    oea.key_secret a_keySecret,
    oea.key_public a_keyPublic,
    oea.token a_token,
    oea.is_internal a_internal,
    oea.is_downloaded a_downloaded,
    oea.for_all_games a_forAllGames,
    oeae.app_id et_id,
    oeae.event_type et_eventType,
    ogd.game_id g_id,
    ogd.name g_name,
    ogd.description g_description
FROM OA_EXT_APP oea
INNER JOIN OA_EXT_APP_EVENT oeae ON oea.app_id = oeae.app_id
LEFT JOIN OA_EXT_APP_GAME oeag ON oea.app_id = oeag.app_id
LEFT JOIN OA_GAME_DEF ogd ON oeag.game_id = ogd.game_id
WHERE
    oea.app_id = :appId
    AND
    oea.is_active = true

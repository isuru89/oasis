INSERT INTO OA_DEFINITION_ATTR (
    def_id,
    def_sub_id,
    attribute_id
) VALUES (
    :defId,
    :defSubId,
    :attributeId
)
ON DUPLICATE KEY
UPDATE attribute_id = VALUES(attribute_id)
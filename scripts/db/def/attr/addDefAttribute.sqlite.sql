INSERT INTO OA_DEFINITION_ATTR (
    def_id,
    def_sub_id,
    attribute_id
) VALUES (
    :defId,
    :defSubId,
    :attributeId
)
ON CONFLICT (def_id, def_sub_id)
DO UPDATE SET attribute_id = excluded.attribute_id
INSERT INTO OA_SHOP_ITEM (
    title,
    description,
    scope,
    level,
    price,
    image_ref,
    <if(hasMaxItems)>
    max_available,
    <endif>
    expiration_at
) VALUES (
    :title,
    :description,
    :scope,
    :level,
    :price,
    :imageRef,
    <if(hasMaxItems)>
    :maxAvailable,
    <endif>
    :expirationAt
)
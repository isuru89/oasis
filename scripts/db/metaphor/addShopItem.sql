INSERT INTO OA_SHOP_ITEM (
    title,
    description,
    scope,
    level,
    price,
    image_ref,
    for_hero,
    <if(hasMaxItems)>
    max_available,
    limited_amount,
    <endif>
    expiration_at
) VALUES (
    :title,
    :description,
    :scope,
    :level,
    :price,
    :imageRef,
    :forHero,
    <if(hasMaxItems)>
    :maxAvailable,
    :1,
    <endif>
    :expirationAt
)
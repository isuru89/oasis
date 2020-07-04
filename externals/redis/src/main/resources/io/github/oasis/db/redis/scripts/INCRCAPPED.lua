local value = tonumber(redis.call("hincrbyfloat", KEYS[1], KEYS[2], ARGV[1]))
local before = value - ARGV[1]
local limit = tonumber(ARGV[2])
if (value > limit) then
    redis.call("hset", KEYS[1], KEYS[2], limit)
    if (before >= limit) then
        return -1
    else
        return limit - before
    end
end
return ARGV[1]
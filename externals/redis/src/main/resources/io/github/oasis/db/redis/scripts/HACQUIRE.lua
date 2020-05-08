local curr = redis.call("hget", KEYS[1], KEYS[2])
if curr == ARGV[1] then
    return curr
else
    redis.call("hset", KEYS[1], KEYS[2], ARGV[1])
    return ARGV[1]
end
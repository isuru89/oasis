local records;
if ARGV[3] == "rev" then
    if ARGV[4] == "byrank" then
        records = redis.call("zrevrange", KEYS[1], ARGV[1], ARGV[2], "withscores");
    else
        records = redis.call("zrevrangebyscore", KEYS[1], ARGV[2], ARGV[1], "withscores");
    end
else
    if ARGV[4] == "byrank" then
        records = redis.call("zrange", KEYS[1], ARGV[1], ARGV[2], "withscores");
    else
        records = redis.call("zrangebyscore", KEYS[1], ARGV[1], ARGV[2], "withscores");
    end
end

local res = {}
local length = table.getn(records);

for i = 1, length, 2 do
    res[(2 * i) - 1] = redis.call("hget", KEYS[2], records[i])
    res[2 * i] = records[i + 1]
end

return res
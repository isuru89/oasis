local length = table.getn(KEYS);
local includecount;
if ARGV and ARGV[2] == "withcard" then
    includecount = true
else
    includecount = false
end
local res = {}

for i = 1, length, 1 do
    res[(3 * i) - 2] = redis.call("zrank", KEYS[i], ARGV[1]);
    res[(3 * i) - 1] = redis.call("zscore", KEYS[i], ARGV[1]);
    if includecount == true then
        res[(3 * i)] = redis.call("zcard", KEYS[i]);
    else
        res[(3 * i)] = '0'
    end
end
return res
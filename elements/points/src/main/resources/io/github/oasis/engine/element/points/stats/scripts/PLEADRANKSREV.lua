local length = table.getn(KEYS);
local includecount;
if ARGV and ARGV[1] == "withcard" then
    includecount = true
else
    includecount = false
end
local res = {}

for i = 2, length, 1 do
    res[(3 * i) - 5] = redis.call("zrevrank", KEYS[i], KEYS[1]);
    res[(3 * i) - 4] = redis.call("zscore", KEYS[i], KEYS[1]);
    if includecount == true then
        res[(3 * i) - 3] = redis.call("zcard", KEYS[i]);
    else
        res[(3 * i) - 3] = '0'
    end
end
return res
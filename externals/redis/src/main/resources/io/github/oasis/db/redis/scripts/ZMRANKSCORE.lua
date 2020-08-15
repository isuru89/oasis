local length = table.getn(KEYS);
local res = {};
for i = 2, length, 1 do
  res[i*2-3] = redis.call("zrank", KEYS[1], KEYS[i]);
  res[i*2-2] = redis.call("zscore", KEYS[1], KEYS[i]);
end
return res
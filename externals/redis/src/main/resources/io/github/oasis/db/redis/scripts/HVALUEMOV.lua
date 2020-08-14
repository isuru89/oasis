local length = table.getn(KEYS);
for i = 2, length, 2 do
    redis.call("hincrby", KEYS[1], KEYS[i], ARGV[1]);
    redis.call("hincrby", KEYS[1], KEYS[i+1], ARGV[2]);
end
return 0;
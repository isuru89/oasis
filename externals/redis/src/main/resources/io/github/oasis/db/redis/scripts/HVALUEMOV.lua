local length = table.getn(ARGV);
for i = 3, length, 2 do
    redis.call("hincrby", KEYS[1], ARGV[i], ARGV[1]);
    redis.call("hincrby", KEYS[1], ARGV[i+1], ARGV[2]);
end
return 0
local length = table.getn(KEYS);
for i = 1, length, 1 do
  redis.call("zincrby", KEYS[i], ARGV[1], ARGV[2])
end
return 1
local length = table.getn(KEYS);
for i = 2, length, 1 do
  redis.call("zincrby", KEYS[i], ARGV[1], KEYS[1])
end
return 1
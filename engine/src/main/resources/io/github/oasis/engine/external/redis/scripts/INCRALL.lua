local length = table.getn(KEYS);
for i = 2, length, 1 do
  redis.call("hincrbyfloat", KEYS[1], KEYS[i], ARGV[1])
end
return 1
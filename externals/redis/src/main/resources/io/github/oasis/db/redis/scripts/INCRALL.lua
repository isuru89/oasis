local length = table.getn(ARGV);
for i = 2, length, 1 do
  redis.call("hincrbyfloat", KEYS[1], ARGV[i], ARGV[1])
end
return 1
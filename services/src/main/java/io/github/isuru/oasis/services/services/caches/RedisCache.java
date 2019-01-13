package io.github.isuru.oasis.services.services.caches;

import io.github.isuru.oasis.model.utils.ICacheProxy;
import io.github.isuru.oasis.model.utils.OasisUtils;
import io.github.isuru.oasis.services.configs.OasisConfigurations;
import io.github.isuru.oasis.services.utils.Commons;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.io.IOException;
import java.util.Optional;

@Component("cacheRedis")
public class RedisCache implements ICacheProxy {

    private Jedis jedis;

    @Autowired
    private OasisConfigurations configurations;

    @Override
    public void init() throws IOException {
        try {
            String host = OasisUtils.getEnvOr("OASIS_CACHE_REDIS_URL", "localhost");
            jedis = new Jedis(Commons.firstNonNull(host, configurations.getCacheRedisHost()));

            String msg = "Hello From Oasis!";

            if (!msg.equals(jedis.echo(msg))) {
                throw new JedisConnectionException("Cannot connect to redis!");
            }
        } catch (JedisException ex) {
            ex.printStackTrace();
            throw new IOException("Failed to load redis cache!", ex);
        }
    }

    @Override
    public Optional<String> get(String key) {
        String val = jedis.get(key);
        if (val == null) {
            return Optional.empty();
        } else {
            return Optional.of(val);
        }
    }

    @Override
    public void update(String key, String value) {
        jedis.set(key, value);
    }

    @Override
    public void expire(String key) {
        jedis.del(key);
    }
}

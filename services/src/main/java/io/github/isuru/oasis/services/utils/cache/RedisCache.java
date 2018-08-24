package io.github.isuru.oasis.services.utils.cache;

import io.github.isuru.oasis.model.configs.Configs;
import io.github.isuru.oasis.model.utils.AbstractCacheFactory;
import io.github.isuru.oasis.model.utils.ICacheProxy;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;

import java.util.Optional;

public class RedisCache implements ICacheProxy {

    private Jedis jedis;

    ICacheProxy init(AbstractCacheFactory.CacheOptions options, Configs configs) throws Exception {
        try {
            jedis = new Jedis(configs.getStr("oasis.cache.redis.url", "localhost"));

            String msg = "Hello Oasis!";

            if (msg.equals(jedis.echo(msg))) {
                return this;
            } else {
                throw new JedisConnectionException("Cannot connect to redis!");
            }
        } catch (JedisException ex) {
            ex.printStackTrace();
            return new InMemoryCache(5000);
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

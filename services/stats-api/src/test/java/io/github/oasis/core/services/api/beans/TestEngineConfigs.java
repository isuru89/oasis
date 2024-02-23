package io.github.oasis.core.services.api.beans;

import com.redis.testcontainers.RedisContainer;
import io.github.oasis.core.configs.OasisConfigs;
import io.github.oasis.core.external.Db;
import io.github.oasis.core.external.EventDispatcher;
import io.github.oasis.core.external.messages.EngineMessage;
import io.github.oasis.db.redis.RedisDb;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.utility.DockerImageName;

import java.io.IOException;
import java.util.Map;

/**
 * @author Isuru Weerarathna
 */
@TestConfiguration
public class TestEngineConfigs {

    @Bean
    public OasisConfigs createOasisConfigs(RedisContainer redisContainer) {
        var overriddenMap = Map.of(
                "O_OASIS_CACHE_URL", redisContainer.getRedisURI(),
                "O_OASIS_ENGINEDB_URL", redisContainer.getRedisURI());
        return new OasisConfigs.Builder()
                .withCustomEnvOverrides(overriddenMap)
                .buildFromYamlResource("test-configs.yml");
    }

    @Bean
    public RedisContainer createRedisContainer() {
        var redisContainer = new RedisContainer(DockerImageName.parse("redis:5"));
        redisContainer.start();
        return redisContainer;
    }

    @Bean
    @Primary
    public RedissonClient createRedissonClient(RedisContainer redisContainer) {
        Config redisConfigs = new Config();
        redisConfigs.useSingleServer()
                .setAddress(redisContainer.getRedisURI())
                .setConnectionMinimumIdleSize(1)
                .setConnectionPoolSize(2);
        return Redisson.create(redisConfigs);
    }

    @Bean("enginedb")
    public Db createEngineDb(RedissonClient redissonClient) {
        return RedisDb.create(redissonClient);
    }

    @Bean("cache")
    public Db createCache(RedissonClient redissonClient) {
        return RedisDb.create(redissonClient);
    }

    @Bean
    @Primary
    public EventDispatcher create() {
        return new EventDispatcher() {
            @Override
            public void init(DispatcherContext context) throws Exception {

            }

            @Override
            public void push(EngineMessage message) throws Exception {

            }

            @Override
            public void broadcast(EngineMessage message) throws Exception {

            }

            @Override
            public void close() throws IOException {

            }
        };
    }
}

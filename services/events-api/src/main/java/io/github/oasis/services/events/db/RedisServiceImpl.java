package io.github.oasis.services.events.db;

import io.github.oasis.core.ID;
import io.github.oasis.core.utils.CacheUtils;
import io.github.oasis.core.utils.Texts;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.GameInfo;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static io.vertx.core.Future.failedFuture;
import static io.vertx.core.Future.succeededFuture;

/**
 * @author Isuru Weerarathna
 */
public class RedisServiceImpl implements RedisService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final RedisAPI api;
    private final String ttlSeconds;

    public static RedisServiceImpl create(Redis redisClient, RedisSettings redisSettings, Handler<AsyncResult<RedisService>> resultHandler) {
        return new RedisServiceImpl(redisClient, redisSettings, resultHandler);
    }

    public RedisServiceImpl(Redis redisClient, RedisSettings settings, Handler<AsyncResult<RedisService>> resultHandler) {
        api = RedisAPI.api(redisClient);
        if (settings.getEventSourceTTL() != null && settings.getEventSourceTTL() > 0) {
            ttlSeconds = String.valueOf(settings.getEventSourceTTL());
        } else {
            ttlSeconds = null;
        }
        resultHandler.handle(succeededFuture(this));
    }

    @Override
    public RedisService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler) {
        api.hget(ID.EVENT_API_CACHE_USERS_KEY,
                email,
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.nonNull(result)) {
                            JsonObject userObj = new JsonObject(result.toBuffer());
                            resultHandler.handle(succeededFuture(UserInfo.create(email, userObj)));
                        } else {
                            resultHandler.handle(succeededFuture(null));
                        }
                    } else {
                        resultHandler.handle(failedFuture(res.cause()));
                    }
                }
        );
        return this;
    }

    @Override
    public RedisService readSourceInfo(String sourceToken, Handler<AsyncResult<EventSource>> resultHandler) {
        api.get(CacheUtils.getSourceCacheKey(sourceToken),
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.isNull(result)) {
                            resultHandler.handle(succeededFuture(null));
                            return;
                        }
                        try {
                            JsonObject sourceJson = (JsonObject) Json.decodeValue(result.toBuffer());
                            EventSource eventSource = EventSource.create(sourceToken, sourceJson);
                            LOG.info("Found event source {}", eventSource);
                            resultHandler.handle(succeededFuture(eventSource));
                        } catch (RuntimeException e) {
                            LOG.error("Failed to retrieve source info {}!", sourceToken, e);
                            resultHandler.handle(failedFuture(e));
                        }
                    } else {
                        resultHandler.handle(failedFuture(res.cause()));
                    }
                });
        return this;
    }

    @Override
    public RedisService persistUserInfo(String email, UserInfo userInfo, Handler<AsyncResult<UserInfo>> resultHandler) {
        api.hset(Arrays.asList(ID.EVENT_API_CACHE_USERS_KEY, email, userInfo.toJson().encode()), res -> {
            if (res.succeeded()) {
                resultHandler.handle(succeededFuture(userInfo));
            } else {
                resultHandler.handle(failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public RedisService persistSourceInfo(String sourceToken, EventSource eventSource, Handler<AsyncResult<EventSource>> resultHandler) {
        String key = CacheUtils.getSourceCacheKey(sourceToken);
        api.set(Arrays.asList(key, eventSource.toJson().encode()), res -> {
            if (res.succeeded()) {
                if (Texts.isNotEmpty(ttlSeconds)) {
                    api.expire(List.of(key, ttlSeconds), expireRes -> resultHandler.handle(succeededFuture(eventSource)));
                } else {
                    resultHandler.handle(succeededFuture(eventSource));
                }
            } else {
                resultHandler.handle(failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public RedisService deleteKey(String key, Handler<AsyncResult<Boolean>> resultHandler) {
        api.del(List.of(key), res -> {
            if (res.succeeded()) {
                LOG.info("Redis cache key deleted! [Key: {}]", key);
                resultHandler.handle(succeededFuture(true));
            } else {
                LOG.error("Unable to clear Redis cache key! [Key: {}]", key);
                LOG.error(" Cause:", res.cause());
                resultHandler.handle(failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public RedisService readGameInfo(int gameId, Handler<AsyncResult<GameInfo>> resultHandler) {
        api.hget(ID.EVENT_API_CACHE_GAMES_KEY,
                String.valueOf(gameId),
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.nonNull(result)) {
                            JsonObject userObj = new JsonObject(result.toBuffer());
                            resultHandler.handle(succeededFuture(GameInfo.create(gameId, userObj)));
                        } else {
                            resultHandler.handle(succeededFuture(null));
                        }
                    } else {
                        resultHandler.handle(failedFuture(res.cause()));
                    }
                }
        );
        return this;
    }

    @Override
    public RedisService persistGameInfo(int gameId, GameInfo gameInfo, Handler<AsyncResult<GameInfo>> resultHandler) {
        api.hset(Arrays.asList(ID.EVENT_API_CACHE_GAMES_KEY, String.valueOf(gameId), gameInfo.toJson().encode()), res -> {
            if (res.succeeded()) {
                resultHandler.handle(succeededFuture(gameInfo));
            } else {
                resultHandler.handle(failedFuture(res.cause()));
            }
        });
        return this;
    }
}

package io.github.oasis.services.events.db;

import io.github.oasis.services.events.Constants;
import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class RedisServiceImpl implements RedisService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisServiceImpl.class);

    private final RedisAPI api;

    public static RedisServiceImpl create(Redis redisClient, Handler<AsyncResult<RedisService>> resultHandler) {
        return new RedisServiceImpl(redisClient, resultHandler);
    }

    public RedisServiceImpl(Redis redisClient, Handler<AsyncResult<RedisService>> resultHandler) {
        api = RedisAPI.api(redisClient);
        resultHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public RedisService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler) {
        api.hget(Constants.CACHE_USERS_KEY,
                email,
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.nonNull(result)) {
                            JsonObject userObj = new JsonObject(result.toBuffer());
                            resultHandler.handle(Future.succeededFuture(UserInfo.create(email, userObj)));
                        } else {
                            resultHandler.handle(Future.succeededFuture(null));
                        }
                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }
                }
        );
        return this;
    }

    @Override
    public RedisService readSourceInfo(String sourceId, Handler<AsyncResult<EventSource>> resultHandler) {
        api.hget(Constants.CACHE_SOURCES_KEY,
                sourceId,
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.isNull(result)) {
                            resultHandler.handle(Future.succeededFuture(null));
                            return;
                        }
                        try {
                            JsonObject sourceJson = (JsonObject) Json.decodeValue(result.toBuffer());
                            EventSource eventSource = EventSource.create(sourceId, sourceJson);
                            LOG.info("Found event source {}", eventSource);
                            resultHandler.handle(Future.succeededFuture(eventSource));
                        } catch (RuntimeException e) {
                            LOG.error("Failed to retrieve source info {}!", sourceId, e);
                            resultHandler.handle(Future.failedFuture(e));
                        }
                    } else {
                        resultHandler.handle(Future.failedFuture(res.cause()));
                    }
                });
        return this;
    }

    @Override
    public RedisService persistUserInfo(String email, UserInfo userInfo, Handler<AsyncResult<UserInfo>> resultHandler) {
        api.hset(Arrays.asList(Constants.CACHE_USERS_KEY, email, userInfo.toJson().encode()), res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(userInfo));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }

    @Override
    public RedisService persistSourceInfo(String sourceId, EventSource eventSource, Handler<AsyncResult<EventSource>> resultHandler) {
        api.hset(Arrays.asList(Constants.CACHE_SOURCES_KEY, sourceId, eventSource.toJson().encode()), res -> {
            if (res.succeeded()) {
                resultHandler.handle(Future.succeededFuture(eventSource));
            } else {
                resultHandler.handle(Future.failedFuture(res.cause()));
            }
        });
        return this;
    }
}

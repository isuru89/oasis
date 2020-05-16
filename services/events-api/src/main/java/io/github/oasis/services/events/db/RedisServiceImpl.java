package io.github.oasis.services.events.db;

import io.github.oasis.services.events.model.EventSource;
import io.github.oasis.services.events.model.UserInfo;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.redis.client.Redis;
import io.vertx.redis.client.RedisAPI;
import io.vertx.redis.client.Response;

import java.util.Objects;

/**
 * @author Isuru Weerarathna
 */
public class RedisServiceImpl implements RedisService {

    private static final String OASIS_USERS_KEY = "oasis.users";

    private RedisAPI api;

    public static RedisServiceImpl create(Redis redisClient, Handler<AsyncResult<RedisService>> resultHandler) {
        return new RedisServiceImpl(redisClient, resultHandler);
    }

    public RedisServiceImpl(Redis redisClient, Handler<AsyncResult<RedisService>> resultHandler) {
        api = RedisAPI.api(redisClient);
        resultHandler.handle(Future.succeededFuture(this));
    }

    @Override
    public RedisService readUserInfo(String email, Handler<AsyncResult<UserInfo>> resultHandler) {
        api.hget(OASIS_USERS_KEY,
                email,
                res -> {
                    if (res.succeeded()) {
                        Response result = res.result();
                        if (Objects.nonNull(result)) {
                            JsonObject userObj = new JsonObject(result.toBuffer());
                            resultHandler.handle(Future.succeededFuture(UserInfo.create(email, userObj)));
                        } else {
                            resultHandler.handle(Future.failedFuture("No user found by email " + email + "!"));
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
        return null;
    }
}

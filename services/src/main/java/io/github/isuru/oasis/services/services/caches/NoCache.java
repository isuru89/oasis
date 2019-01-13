package io.github.isuru.oasis.services.services.caches;

import io.github.isuru.oasis.model.utils.ICacheProxy;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("cacheNone")
class NoCache implements ICacheProxy {

    @Override
    public void init() {
        // do nothing
    }

    @Override
    public Optional<String> get(String key) {
        return Optional.empty();
    }

    @Override
    public void update(String key, String value) {

    }

    @Override
    public void expire(String key) {

    }
}

package io.github.isuru.oasis.services.utils.cache;

import io.github.isuru.oasis.model.utils.ICacheProxy;

import java.util.Optional;

class NoCache implements ICacheProxy {
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

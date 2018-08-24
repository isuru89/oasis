package io.github.isuru.oasis.model.utils;

import java.util.Optional;

public interface ICacheProxy {

    Optional<String> get(String key);

    void update(String key, String value);

    void expire(String key);

}

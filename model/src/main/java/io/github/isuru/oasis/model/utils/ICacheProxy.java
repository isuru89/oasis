package io.github.isuru.oasis.model.utils;

import java.io.IOException;
import java.util.Optional;

public interface ICacheProxy {

    void init() throws IOException;

    Optional<String> get(String key);

    void update(String key, String value);

    void expire(String key);

}

package io.github.isuru.oasis.services.security;

import java.io.Closeable;

public interface OasisAuthenticator extends Closeable {

    boolean authenticate(String username, String password);

}

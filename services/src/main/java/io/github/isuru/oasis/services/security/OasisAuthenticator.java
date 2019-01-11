package io.github.isuru.oasis.services.security;

public interface OasisAuthenticator {

    boolean authenticate(String username, String password);

}

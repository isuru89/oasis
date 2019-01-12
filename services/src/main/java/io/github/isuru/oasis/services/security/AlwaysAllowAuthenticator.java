package io.github.isuru.oasis.services.security;

import org.springframework.stereotype.Component;

@Component
public class AlwaysAllowAuthenticator implements OasisAuthenticator {
    @Override
    public boolean authenticate(String username, String password) {
        return true;
    }
}

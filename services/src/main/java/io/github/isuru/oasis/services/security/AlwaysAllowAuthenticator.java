package io.github.isuru.oasis.services.security;

import org.springframework.stereotype.Component;

import java.io.IOException;

@Component("authNone")
public class AlwaysAllowAuthenticator implements OasisAuthenticator {
    @Override
    public boolean authenticate(String username, String password) {
        return true;
    }

    @Override
    public void close() throws IOException {
        // do nothing
    }
}

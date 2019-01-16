package io.github.isuru.oasis.services.controllers;

import io.github.isuru.oasis.services.security.OasisAuthenticator;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component("authTest")
@Scope(ConfigurableBeanFactory.SCOPE_SINGLETON)
public class TestAuthenticator implements OasisAuthenticator {

    private Map<String, String> allowedUsers = new HashMap<>();

    public TestAuthenticator() {}

    public void refill(Map<String, String> allowedUsers) {
        this.allowedUsers = new HashMap<>(allowedUsers);
    }

    @Override
    public boolean authenticate(String username, String password) {
        if (allowedUsers.containsKey(username)) {
            return password.equals(allowedUsers.get(username));
        }
        return false;
    }

    @Override
    public void close() throws IOException {
        allowedUsers.clear();
    }
}

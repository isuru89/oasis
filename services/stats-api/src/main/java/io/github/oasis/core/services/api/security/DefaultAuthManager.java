package io.github.oasis.core.services.api.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;

@Configuration
public class DefaultAuthManager {

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationProvider provider) throws Exception {
        return new ProviderManager(provider);
    }

}

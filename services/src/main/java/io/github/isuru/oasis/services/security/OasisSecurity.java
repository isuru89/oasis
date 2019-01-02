package io.github.isuru.oasis.services.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class OasisSecurity extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .anyRequest()
                .permitAll()
                .and()
                .csrf().disable();
    }

    @Bean
    public FilterRegistrationBean<TokenSecurityFilter> filterAuthToken() {
        FilterRegistrationBean<TokenSecurityFilter> bean = new FilterRegistrationBean<>();
        TokenSecurityFilter filter = new TokenSecurityFilter();

        bean.setFilter(filter);
        bean.addUrlPatterns("/admin/*");

        return bean;
    }
}

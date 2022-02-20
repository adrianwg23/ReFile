package com.example.refile.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.example.refile.util.Constants.TEST_PROFILE;

@Configuration(proxyBeanMethods = false)
public class SameSiteCookieConfig {

    @Bean
    @ConditionalOnProperty(name = "spring.profiles.active", havingValue = TEST_PROFILE)
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofLax().whenHasNameMatching("XSRF-TOKEN");
    }
}

package com.example.refile.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.server.CookieSameSiteSupplier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class SameSiteCookieConfig {

    @Bean
    @ConditionalOnProperty(name="spring.profiles.active", havingValue="test")
    public CookieSameSiteSupplier applicationCookieSameSiteSupplier() {
        return CookieSameSiteSupplier.ofNone().whenHasNameMatching("XSRF-TOKEN");
    }
}

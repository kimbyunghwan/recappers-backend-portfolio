package com.sch.capstone.backend.config;

import org.springframework.context.annotation.Bean;

public class ProxyHeaderConfig {
    @Bean
    public org.springframework.web.filter.ForwardedHeaderFilter forwardedHeaderFilter() {
        return new org.springframework.web.filter.ForwardedHeaderFilter();
    }
}
package com.websitestudios.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class JwtConfig {

    @Bean
    public JwtTokenProvider jwtTokenProvider(JwtProperties jwtProperties, Environment env) {
        boolean prod = env != null
                && env.acceptsProfiles(org.springframework.core.env.Profiles.of("prod", "production"));
        return new JwtTokenProvider(jwtProperties, prod);
    }
}

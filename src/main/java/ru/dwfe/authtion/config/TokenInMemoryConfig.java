package ru.dwfe.authtion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
public class TokenInMemoryConfig
{
    @Bean
    @Primary
    public TokenStore tokenStore()
    {
        return new InMemoryTokenStore();
    }


}

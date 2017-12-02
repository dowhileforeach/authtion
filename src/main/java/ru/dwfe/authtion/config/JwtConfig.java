package ru.dwfe.authtion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
public class JwtConfig
{
    @Bean
    public AccessTokenConverter accessTokenConverter()
    {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        converter.setSigningKey("G5SeDYBzscfKYsSQJ9mXDdAiuABt2sa46i222dbqLBtZcVBwNUhEb8RY8ymSbuhXFtnNUFgpG9ddTU7BDW5fp6u3xeHBphixwZTM");
        return converter;

        //JwtAccessTokenConverter подменяет хранилище токенов следующим образом:
        //берет пришедший токен, декодирует содержимое и оборачивает его в OAuth2AccessToken
        //и возвращет как будто взял его из хранилища.
        //
        //Самое главное - это SigningKey. При помощи него JWT праверяет валидность токена.
        //SigningKey должен быть известен только Auth серверу.
        //SigningKey это первая и последняя точка защиты всей вашей JWT Auth инфраструктуры.
    }

    @Bean
    public TokenStore tokenStore()
    {
        return new JwtTokenStore((JwtAccessTokenConverter) accessTokenConverter());
    }
}
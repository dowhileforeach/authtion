package ru.dwfe.authtion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import static ru.dwfe.authtion.config.GlobalVariables.APIv1;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter
{
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private TokenStore tokenStore;

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception
    {
        endpoints
                .pathMapping("/oauth/token", APIv1 + "/oauth/token")
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore)
        ;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception
    {
        //Client credentials is not encrypted
        security.passwordEncoder(NoOpPasswordEncoder.getInstance());
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer configurer) throws Exception
    {
        //Authorization Server: https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-security.html#boot-features-security-oauth2-authorization-server
        //Access Token Request: https://tools.ietf.org/html/rfc6749#section-4.3.2

        configurer
                .inMemory() // in Memory or in JDBC

                .withClient("Standard")
                .secret("Login")
                .scopes("all")
                .accessTokenValiditySeconds(60 * 60 * 24 * 10) // 10 days

                .and()

                .withClient("ThirdParty")
                .secret("Computer")
                .scopes("all")
                .accessTokenValiditySeconds(60 * 3) // 3 minutes
        ;

        //Здесь Клиентом является Фронтэнд.
        //В качестве фронтенда может быть обычная HTML страничка + JavaScript, либо фреймворк, например, Angular.
        //Если User логинится на Клиенте, то клиент должен методом POST отправить его credentials на сервер.
        //Протестировать логинг можно так:
        //curl withClient:secret@localhost:8080/oauth/token -d grant_type=password -d username=UserLogin -d password=UserPass
        //
        //Сервер ответит токеном, либо ошибкой
    }
}

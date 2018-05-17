package ru.dwfe.net.authtion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

import static ru.dwfe.net.authtion.AuthtionGlobal.*;

@Configuration
@EnableAuthorizationServer
public class AuthtionAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter
{
  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private TokenStore tokenStore;
  @Autowired
  private UserDetailsService userDetailsService;

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception
  {
    endpoints
            .pathMapping("/oauth/token", API_CURRENT_VERSION + resource_signIn)
            .authenticationManager(authenticationManager)
            .tokenStore(tokenStore)
            .userDetailsService(userDetailsService) //needed for token refreshing
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

            .withClient(client_ID_TRUSTED)
            .secret(client_PASSWORD_TRUSTED)
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(60 * 60 * 24 * 20) // 20 days

            .and()

            .withClient(client_ID_UNTRUSTED)
            .secret(client_PASSWORD_UNTRUSTED)
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(60 * 3) // 3 minutes
    ;

    //Здесь Клиентом является Фронтэнд.
    //В качестве фронтенда может быть обычная HTML страничка + JavaScript, либо фреймворк, например, Angular.
    //В любом случае, чтобы залогиниться надо отправить методом POST пользовательские credentials на сервер.
    //Протестировать Sign In можно так:
    //curl withClient:secret@localhost:8080/v1/sign-in -d grant_type=password -d username=UserLogin -d password=UserPass
    //
    //Сервер ответит токеном, либо ошибкой
  }
}
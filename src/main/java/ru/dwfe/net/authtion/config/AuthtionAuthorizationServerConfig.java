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

@Configuration
@EnableAuthorizationServer
public class AuthtionAuthorizationServerConfig extends AuthorizationServerConfigurerAdapter
{
  private final AuthenticationManager authenticationManager;
  private final TokenStore tokenStore;
  private final UserDetailsService userDetailsService;
  private final AuthtionConfigProperties authtionConfigProperties;

  @Autowired
  public AuthtionAuthorizationServerConfig(AuthenticationManager authenticationManager, TokenStore tokenStore, UserDetailsService userDetailsService, AuthtionConfigProperties authtionConfigProperties)
  {
    this.authenticationManager = authenticationManager;
    this.tokenStore = tokenStore;
    this.userDetailsService = userDetailsService;
    this.authtionConfigProperties = authtionConfigProperties;
  }

  @Override
  public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception
  {
    endpoints
            .pathMapping("/oauth/token", authtionConfigProperties.getApi() + authtionConfigProperties.getResource().getSignIn())
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

            .withClient(authtionConfigProperties.getOauth2ClientTrusted().getId())
            .secret(authtionConfigProperties.getOauth2ClientTrusted().getPassword())
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(authtionConfigProperties.getOauth2ClientTrusted().getTokenValiditySeconds())

            .and()

            .withClient(authtionConfigProperties.getOauth2ClientUntrusted().getId())
            .secret(authtionConfigProperties.getOauth2ClientUntrusted().getPassword())
            .scopes("all")
            .authorizedGrantTypes("password", "refresh_token")
            .accessTokenValiditySeconds(authtionConfigProperties.getOauth2ClientUntrusted().getTokenValiditySeconds())
    ;
  }
}

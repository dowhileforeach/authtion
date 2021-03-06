package ru.dwfe.net.authtion.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

@Configuration
public class AuthtionTokenStoreConfig
{
  @Autowired
  private DataSource dataSource;

  @Bean
  @Primary
  public TokenStore tokenStore()
  {
    //return new InMemoryTokenStore();

    // To persist tokens between restarts I need:
    // 1) to configure a persistent token store (JdbcTokenStore for example, see config/TokenStoreConfig.java)
    // 2) create SQL tables: https://github.com/spring-projects/spring-security-oauth/blob/master/spring-security-oauth2/src/test/resources/schema.sql
    return new JdbcTokenStore(dataSource);

    // Regarding the log "INFO Failed to find access token for token", this is NORMAL during refresh since
    // the new generated access token will be queried from the database to check for duplicates (hence the log).
    // In case another entry is found that uses the same access token, it will be removed (see JdbcTokenStore.java
    // line 144). If no record is found, the access token will be updated to the new generated access token.
    // Read access_token after removing it from database, fragment of code (JdbcTokenStore:144):
    //        if (readAccessToken(token.getValue())!=null) {
    //            removeAccessToken(token.getValue());
    //        }
  }
}

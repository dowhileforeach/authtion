package ru.dwfe.net.authtion.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;

@Component
public class AuthtionTestClient
{
  @Autowired
  private AuthtionConfigProperties authtionConfigProperties;

  String clientname;
  String clientpass;
  int maxTokenExpirationTime;
  int minTokenExpirationTime;

  public AuthtionTestClient of(String clientname, String clientpass, int maxTokenExpirationTime, int minTokenExpirationTime)
  {
    var client = new AuthtionTestClient();
    client.clientname = clientname;
    client.clientpass = clientpass;
    client.maxTokenExpirationTime = maxTokenExpirationTime;
    client.minTokenExpirationTime = minTokenExpirationTime;

    return client;
  }

  public AuthtionTestClient getClientTrusted()
  {
    return of(
            authtionConfigProperties.getOauth2ClientTrusted().getId(),
            authtionConfigProperties.getOauth2ClientTrusted().getPassword(),
            1_728_000,
            180
    );
  }

  public AuthtionTestClient getClientUntrusted()
  {
    return of(
            authtionConfigProperties.getOauth2ClientUntrusted().getId(),
            authtionConfigProperties.getOauth2ClientUntrusted().getPassword(),
            180,
            0
    );
  }
}

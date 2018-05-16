package ru.dwfe.net.authtion.test;

public class AuthtionTestClient
{
  String clientname;
  String clientpass;
  int maxTokenExpirationTime;
  int minTokenExpirationTime;

  public static AuthtionTestClient of(String clientname, String clientpass, int maxTokenExpirationTime, int minTokenExpirationTime)
  {
    AuthtionTestClient client = new AuthtionTestClient();
    client.clientname = clientname;
    client.clientpass = clientpass;
    client.maxTokenExpirationTime = maxTokenExpirationTime;
    client.minTokenExpirationTime = minTokenExpirationTime;

    return client;
  }
}

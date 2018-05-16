package ru.dwfe.net.authtion.test;

import static ru.dwfe.net.authtion.test.AuthtionTestSignInType.SignIn;

public class AuthtionTestConsumer
{

  public String username;
  String password;

  AuthtionTestClient client;

  public AuthtionTestAuthorityLevel level;

  public String access_token;
  public String refresh_token;


  public static AuthtionTestConsumer of(AuthtionTestAuthorityLevel level, String username, String password, AuthtionTestClient client, int signInExpectedStatus)
  {
    AuthtionTestConsumer testConsumer = new AuthtionTestConsumer();
    testConsumer.level = level;
    testConsumer.username = username;
    testConsumer.password = password;
    testConsumer.client = client;

    AuthtionTestUtil.setNewTokens(testConsumer, signInExpectedStatus, SignIn);

    return testConsumer;
  }

  static AuthtionTestConsumer getAnonymous()
  {
    AuthtionTestConsumer testConsumer = new AuthtionTestConsumer();
    testConsumer.level = AuthtionTestAuthorityLevel.ANY;
    return testConsumer;
  }
}

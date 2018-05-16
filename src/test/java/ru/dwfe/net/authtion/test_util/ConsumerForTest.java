package ru.dwfe.net.authtion.test_util;

import static ru.dwfe.net.authtion.test_util.SignInType.SignIn;

public class ConsumerForTest
{

  public String username;
  String password;

  Client client;

  public AuthorityLevel level;

  public String access_token;
  public String refresh_token;


  public static ConsumerForTest of(AuthorityLevel level, String username, String password, Client client, int signInExpectedStatus)
  {
    ConsumerForTest consumerForTest = new ConsumerForTest();
    consumerForTest.level = level;
    consumerForTest.username = username;
    consumerForTest.password = password;
    consumerForTest.client = client;

    UtilForTest.setNewTokens(consumerForTest, signInExpectedStatus, SignIn);

    return consumerForTest;
  }

  static ConsumerForTest getAnonymous()
  {
    ConsumerForTest consumerForTest = new ConsumerForTest();
    consumerForTest.level = AuthorityLevel.ANY;
    return consumerForTest;
  }
}

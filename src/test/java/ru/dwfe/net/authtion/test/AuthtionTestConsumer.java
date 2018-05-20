package ru.dwfe.net.authtion.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.ADMIN;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.USER;
import static ru.dwfe.net.authtion.test.AuthtionTestGlobalVariables.client_TRUSTED;
import static ru.dwfe.net.authtion.test.AuthtionTestGlobalVariables.client_UNTRUSTED;
import static ru.dwfe.net.authtion.test.AuthtionTestSignInType.SignIn;

@Component
public class AuthtionTestConsumer
{
  @Autowired
  private AuthtionTestUtil authtionTestUtil;

  public String username;
  String password;

  AuthtionTestClient client;

  public AuthtionTestAuthorityLevel level;

  public String access_token;
  public String refresh_token;


  public AuthtionTestConsumer of(AuthtionTestAuthorityLevel level, String username, String password, AuthtionTestClient client, int signInExpectedStatus)
  {
    AuthtionTestConsumer testConsumer = new AuthtionTestConsumer();
    testConsumer.level = level;
    testConsumer.username = username;
    testConsumer.password = password;
    testConsumer.client = client;

    authtionTestUtil.setNewTokens(testConsumer, signInExpectedStatus, SignIn);

    return testConsumer;
  }

  public AuthtionTestConsumer getAnonymous()
  {
    AuthtionTestConsumer testConsumer = new AuthtionTestConsumer();
    testConsumer.level = AuthtionTestAuthorityLevel.ANY;
    return testConsumer;
  }

  public AuthtionTestConsumer getUSER()
  {
    return of(USER, "test2@dwfe.ru", "test22", client_TRUSTED, 200);
  }

  public AuthtionTestConsumer getADMIN()
  {
    return of(ADMIN, "test1@dwfe.ru", "test11", client_UNTRUSTED, 200);
  }
}


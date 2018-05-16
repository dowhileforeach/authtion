package ru.dwfe.net.authtion.test;

import ru.dwfe.net.authtion.AuthtionGlobal;

import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.ADMIN;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.USER;

public class AuthtionTestGlobalVariables
{
  public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
  public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + AuthtionGlobal.API_CURRENT_VERSION;

  /*
      Clents to get access to resource: /sign-in - for operations 'Sign In' and 'Token refreshing'
  */
  public static final AuthtionTestClient client_TRUSTED = AuthtionTestClient.of(AuthtionGlobal.client_ID_TRUSTED, AuthtionGlobal.client_PASSWORD_TRUSTED, 1_728_000, 180);
  public static final AuthtionTestClient client_UNTRUSTED = AuthtionTestClient.of(AuthtionGlobal.client_ID_UNTRUSTED, AuthtionGlobal.client_PASSWORD_UNTRUSTED, 180, 0);

  /*
      Consumers from backend database
  */
  public static final AuthtionTestConsumer USER_consumer = AuthtionTestConsumer.of(USER, "test2@dwfe.ru", "test22", client_TRUSTED, 200);
  public static final AuthtionTestConsumer ADMIN_consumer = AuthtionTestConsumer.of(ADMIN, "test1@dwfe.ru", "test11", client_UNTRUSTED, 200);
  public static final AuthtionTestConsumer ANY_consumer = AuthtionTestConsumer.getAnonymous();
}

package ru.dwfe.net.authtion.test_util;

import ru.dwfe.net.authtion.Global;

import static ru.dwfe.net.authtion.test_util.AuthorityLevel.ADMIN;
import static ru.dwfe.net.authtion.test_util.AuthorityLevel.USER;

public class VariablesGlobal
{
  public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
  public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + Global.API_CURRENT_VERSION;

  /*
      Clents to get access to resource: /sign-in - for operations 'Sign In' and 'Token refreshing'
  */
  public static final Client client_TRUSTED = Client.of(Global.client_ID_TRUSTED, Global.client_PASSWORD_TRUSTED, 1_728_000, 180);
  public static final Client client_UNTRUSTED = Client.of(Global.client_ID_UNTRUSTED, Global.client_PASSWORD_UNTRUSTED, 180, 0);

  /*
      Consumers from backend database
  */
  public static final ConsumerForTest USER_consumer = ConsumerForTest.of(USER, "test2@dwfe.ru", "test22", client_TRUSTED, 200);
  public static final ConsumerForTest ADMIN_consumer = ConsumerForTest.of(ADMIN, "test1@dwfe.ru", "test11", client_UNTRUSTED, 200);
  public static final ConsumerForTest ANY_consumer = ConsumerForTest.getAnonymous();
}

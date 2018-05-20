package ru.dwfe.net.authtion.test;

import ru.dwfe.net.authtion.AuthtionGlobal;

public class AuthtionTestGlobalVariables
{
  /*
      Clents to get access to resource: /sign-in - for operations 'Sign In' and 'Token refreshing'
  */
  public static final AuthtionTestClient client_TRUSTED = AuthtionTestClient.of(AuthtionGlobal.client_ID_TRUSTED, AuthtionGlobal.client_PASSWORD_TRUSTED, 1_728_000, 180);
  public static final AuthtionTestClient client_UNTRUSTED = AuthtionTestClient.of(AuthtionGlobal.client_ID_UNTRUSTED, AuthtionGlobal.client_PASSWORD_UNTRUSTED, 180, 0);

  /*
      Consumers from backend database
  */
}

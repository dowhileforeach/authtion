package ru.dwfe.net.authtion.test;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.AuthtionGlobal.*;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.*;

public class AuthtionTestVariablesForAuthTest
{
  /*
      RESOURCES
  */
  public static final Map<String, Map<AuthtionTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> RESOURCE_AUTHORITY_reqDATA()
  {
    Map<String, Map<AuthtionTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> result = new HashMap<>();

    result.put(resource_checkConsumerEmail, Map.of(ANY, Map.of(POST, Map.of("email", "user"))));
    result.put(resource_checkConsumerPass, Map.of(ANY, Map.of(POST, Map.of("password", "some password"))));
    result.put(resource_createConsumer, Map.of(ANY, Map.of(POST, Map.of("email", "user", "password", "some password", "firstName", "some first name", "lastName", ""))));
    result.put(resource_updateConsumer, Map.of(USER, Map.of(POST, Map.of())));
    result.put(resource_getConsumerData, Map.of(USER, Map.of(GET, Map.of())));
    result.put(resource_publicConsumer + "/1", Map.of(ANY, Map.of(GET, Map.of())));
    result.put(resource_listOfConsumers, Map.of(ADMIN, Map.of(GET, Map.of())));
    result.put(resource_reqConfirmConsumerEmail, Map.of(USER, Map.of(GET, Map.of())));
    result.put(resource_confirmConsumerEmail, Map.of(ANY, Map.of(GET, Map.of("key", "AnyString"))));
    result.put(resource_changeConsumerPass, Map.of(USER, Map.of(POST, Map.of())));
    result.put(resource_reqRestoreConsumerPass, Map.of(ANY, Map.of(POST, Map.of())));
    result.put(resource_confirmRestoreConsumerPass, Map.of(ANY, Map.of(GET, Map.of("key", "AnyString"))));
    result.put(resource_restoreConsumerPass, Map.of(ANY, Map.of(POST, Map.of())));

    return result;
  }


  /* Expected statuses:
      200 = OK
      400 = Bad Request
      401 = Unauthorized
      403 = Forbidden, access_denied
  */
  public static final Map<AuthtionTestAuthorityLevel, Map<AuthtionTestAuthorityLevel, Integer>> AUTHORITY_to_AUTHORITY_STATUS = Map.of(
          ANY, Map.of(
                  ANY, 200,
                  USER, 401,
                  ADMIN, 401),
          USER, Map.of(
                  ANY, 200,
                  USER, 200,
                  ADMIN, 403),
          ADMIN, Map.of(
                  ANY, 200,
                  USER, 200,
                  ADMIN, 200)
  );

  public static final Map<AuthtionTestAuthorityLevel, Map<AuthtionTestAuthorityLevel, Integer>> AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN = Map.of(
          USER, Map.of(
                  ANY, 401,
                  USER, 401,
                  ADMIN, 401),
          ADMIN, Map.of(
                  ANY, 401,
                  USER, 401,
                  ADMIN, 401)
  );


  /* OTHER */

  public static final int TOTAL_ACCESS_TOKEN_COUNT = 2;

}
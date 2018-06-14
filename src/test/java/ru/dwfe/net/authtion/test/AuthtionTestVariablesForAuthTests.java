package ru.dwfe.net.authtion.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.*;

@Configuration
public class AuthtionTestVariablesForAuthTests
{
  @Autowired
  private AuthtionConfigProperties authtionConfigProperties;

  /*
      RESOURCES
  */
  public Map<String, Map<AuthtionTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> RESOURCE_AUTHORITY_reqDATA()
  {
    Map<String, Map<AuthtionTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>>> result = new HashMap<>();

    result.put(authtionConfigProperties.getResource().getCheckEmail(), Map.of(ANY, Map.of(POST, Map.of("email", "user"))));
    result.put(authtionConfigProperties.getResource().getCheckPass(), Map.of(ANY, Map.of(POST, Map.of("password", "some password"))));
    result.put(authtionConfigProperties.getResource().getCreateAccount(), Map.of(ANY, Map.of(POST, Map.of("email", "user", "password", "some password", "firstName", "some first name", "lastName", ""))));
    result.put(authtionConfigProperties.getResource().getGetUserPersonal(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(authtionConfigProperties.getResource().getUpdateUserPersonal(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(authtionConfigProperties.getResource().getPublicAccount() + "/1", Map.of(ANY, Map.of(GET, Map.of())));
    result.put(authtionConfigProperties.getResource().getReqConfirmEmail(), Map.of(USER, Map.of(GET, Map.of())));
    result.put(authtionConfigProperties.getResource().getConfirmEmail(), Map.of(ANY, Map.of(POST, Map.of("key", "AnyString"))));
    result.put(authtionConfigProperties.getResource().getChangePass(), Map.of(USER, Map.of(POST, Map.of())));
    result.put(authtionConfigProperties.getResource().getReqResetPass(), Map.of(ANY, Map.of(POST, Map.of())));
    result.put(authtionConfigProperties.getResource().getConfirmResetPass(), Map.of(ANY, Map.of(POST, Map.of("key", "AnyString"))));
    result.put(authtionConfigProperties.getResource().getResetPass(), Map.of(ANY, Map.of(POST, Map.of())));

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

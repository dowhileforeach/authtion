package ru.dwfe.net.authtion.test_util;

import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.test_util.AuthorityType.*;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;

public class Variables_for_AuthorityTest
{
    /*
        RESOURCES
    */
    public static final Map<String, Map<AuthorityType, Map<RequestMethod, Map<String, Object>>>> RESOURCE_AUTHORITY_reqDATA()
    {
        Map<String, Map<AuthorityType, Map<RequestMethod, Map<String, Object>>>> result = new HashMap<>();

        result.put(resource_public, Map.of(ANY, Map.of(GET, Map.of())));
        result.put(resource_cities, Map.of(USER, Map.of(GET, Map.of())));
        result.put(resource_users, Map.of(ADMIN, Map.of(GET, Map.of())));
        result.put(resource_checkUserEmail, Map.of(FRONTEND, Map.of(POST, Map.of("email", "user"))));
        result.put(resource_checkUserPass, Map.of(FRONTEND, Map.of(POST, Map.of("password", "some password"))));
        result.put(resource_createUser, Map.of(FRONTEND, Map.of(POST, Map.of("email", "user", "password", "some password", "firstName", "some first name", "lastName", ""))));
        result.put(resource_userData, Map.of(USER, Map.of(GET, Map.of())));
        result.put(resource_publicUser1, Map.of(ANY, Map.of(GET, Map.of())));
        result.put(resource_reqConfirmEmail, Map.of(USER, Map.of(GET, Map.of())));
        result.put(resource_confirmEmail, Map.of(ANY, Map.of(GET, Map.of("key", "AnyString"))));
        result.put(resource_changeUserPass, Map.of(USER, Map.of(POST, Map.of())));

        return result;
    }


    /* Expected statuses:
        200 = OK
        401 = Unauthorized
        403 = Forbidden, access_denied
    */
    public static final Map<AuthorityType, Map<AuthorityType, Integer>> AUTHORITY_to_AUTHORITY_STATUS = Map.of(
            ANY, Map.of(
                    ANY, 200,
                    USER, 401,
                    ADMIN, 401,
                    FRONTEND, 401),
            USER, Map.of(
                    ANY, 200,
                    USER, 200,
                    ADMIN, 403,
                    FRONTEND, 403),
            ADMIN, Map.of(
                    ANY, 200,
                    USER, 200,
                    ADMIN, 200,
                    FRONTEND, 403),
            FRONTEND, Map.of(
                    ANY, 200,
                    USER, 403,
                    ADMIN, 403,
                    FRONTEND, 200)
    );


    /* OTHER */

    public static final int TOTAL_ACCESS_TOKEN_COUNT = 3;

}

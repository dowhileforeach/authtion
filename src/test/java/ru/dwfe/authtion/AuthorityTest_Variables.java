package ru.dwfe.authtion;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Map;

public class AuthorityTest_Variables
{

    /* Expected statuses
        200 = OK
        401 = Unauthorized
        403 = Forbidden, access_denied
    */

    //user
    public static final int user_userLevelResource_expectedStatus = 200;
    public static final int user_adminLevelResource_expectedStatus = 403;
    public static final int user_frontendLevelResource_checkUserId_expectedStatus = 403;
    public static final int user_frontendLevelResource_createUser_expectedStatus = 403;

    //admin
    public static final int admin_userLevelResource_expectedStatus = 200;
    public static final int admin_adminLevelResource_expectedStatus = 200;
    public static final int admin_frontendLevelResource_checkUserId_expectedStatus = 403;
    public static final int admin_frontendLevelResource_createUser_expectedStatus = 403;

    //shop
    public static final int shop_userLevelResource_expectedStatus = 403;
    public static final int shop_adminLevelResource_expectedStatus = 403;
    public static final int shop_frontendLevelResource_checkUserId_expectedStatus = 200;
    public static final int shop_frontendLevelResource_createUser_expectedStatus = 200;

    //anonymous
    public static final int anonymous_userLevelResource_expectedStatus = 401;
    public static final int anonymous_adminLevelResource_expectedStatus = 401;
    public static final int anonymous_frontendLevelResource_checkUserId_expectedStatus = 401;
    public static final int anonymous_frontendLevelResource_createUser_expectedStatus = 401;


    /* BODIES */
    public static final RequestBody body_for_frontendLevelResource_checkUserId =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{\"id\": \"user\"}");

    public static final RequestBody body_for_frontendLevelResource_createUser =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{" +
                            "\"id\": \"user\"," +
                            "\"password\": \"some password\"," +
                            "\"firstName\": \"some first name\"," +
                            "\"lastName\": \"\"" +
                            "}");
    /* QUERIES */

    public static final Map<String, String> queries_for_publicLevelResource_confirmUser =
            Map.of("confirmkey","AnyString");

}

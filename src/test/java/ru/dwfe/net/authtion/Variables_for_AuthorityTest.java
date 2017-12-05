package ru.dwfe.net.authtion;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Map;

public class Variables_for_AuthorityTest
{
    /* Expected statuses
        200 = OK
        401 = Unauthorized
        403 = Forbidden, access_denied
    */

    //user
    public static final int user_USERLevelResource_expectedStatus = 200;
    public static final int user_ADMINLevelResource_expectedStatus = 403;
    public static final int user_FRONTENDLevelResource_expectedStatus = 403;

    //admin
    public static final int admin_USERLevelResource_expectedStatus = 200;
    public static final int admin_ADMINLevelResource_expectedStatus = 200;
    public static final int admin_FRONTENDLevelResource_expectedStatus = 403;

    //shop
    public static final int shop_USERLevelResource_expectedStatus = 403;
    public static final int shop_ADMINLevelResource_expectedStatus = 403;
    public static final int shop_FRONTENDLevelResource_expectedStatus = 200;

    //anonymous
    public static final int anonymous_USERLevelResource_expectedStatus = 401;
    public static final int anonymous_ADMINLevelResource_expectedStatus = 401;
    public static final int anonymous_FRONTENDLevelResource_expectedStatus = 401;


    /* BODIES */
    public static final RequestBody body_for_FRONTENDLevelResource_checkUserId =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{\"id\": \"user\"}");

    public static final RequestBody body_for_FRONTENDLevelResource_createUser =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{" +
                            "\"id\": \"user\"," +
                            "\"password\": \"some password\"," +
                            "\"firstName\": \"some first name\"," +
                            "\"lastName\": \"\"" +
                            "}");
    /* QUERIES */

    public static final Map<String, String> queries_for_PUBLICLevelResource_confirmUser =
            Map.of("confirmkey", "AnyString");


    /* OTHER */

    public static final int totalAccessTokenCount = 3;

}

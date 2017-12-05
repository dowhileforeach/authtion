package ru.dwfe.net.authtion;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.Map;

public class Variables_for_CreateUserTest
{
    /* BODIES */

    public static final Map<String, Boolean> userIDlist_for_checkUserId = Map.of(
            "", false,
            "123456789012345678901234567890", false,
            "Administrator", false,
            "user", false

    );

    public static final RequestBody body_for_FRONTENDLevelResource_checkUserId_existedUser =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{\"id\": \"user\"}");

    public static final RequestBody body_for_FRONTENDLevelResource_checkUserId_notExistedUser =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{\"id\": \"admin1234567\"}");
}

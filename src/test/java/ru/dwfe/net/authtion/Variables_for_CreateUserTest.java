package ru.dwfe.net.authtion;

import okhttp3.MediaType;
import okhttp3.RequestBody;

public class Variables_for_CreateUserTest
{
    /* BODIES */

    public static final RequestBody body_for_FRONTENDLevelResource_checkUserId_admin =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{\"id\": \"admin\"}");

    public static final RequestBody body_for_FRONTENDLevelResource_checkUserId_admin123 =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                    "{\"id\": \"admin123\"}");
}
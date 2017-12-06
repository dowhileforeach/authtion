package ru.dwfe.net.authtion.util;

import okhttp3.MediaType;
import okhttp3.RequestBody;

import java.util.List;

public class Variables_for_CreateUserTest
{
    /* BODIES */

    public static final List<Checker> checkers_for_checkUserId = List.of(
            Checker.of(false, "", "id", "can't be empty"),
            Checker.of(false, "123456789012345678901234567890", "id", "length must be less than 30 characters"),
            Checker.of(false, "Administrator", "id", "not allowed"),
            Checker.of(false, "user", "id", "must be valid e-mail address"),
            Checker.of(false, "user@ya.ru", "id", "user is present"),
            Checker.of(true, "notExistedUser@ya.ru", "id", "")
    );

    public static final RequestBody requestBody_empty =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

    public static RequestBody getRequestBody_for_FRONTENDLevelResource_checkUserId(String value)
    {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                "{\"id\": \"" + value + "\"}");
    }
}


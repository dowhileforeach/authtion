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
            Checker.of(false, ".uuqu@mail.ru", "id", "must be valid e-mail address"),
            Checker.of(false, "user@ya.ru", "id", "user is present"),
            Checker.of(true, "notExistedUser@ya.ru", "id", null)
    );

    public static final List<Checker> checkers_for_checkUserPass = List.of(
            Checker.of(false, "", "password", "can't be empty"),
            Checker.of(false, "12345", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of(true, "123456", "password", null)
    );

    public static final RequestBody requestBody_empty =
            RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

    public static RequestBody getRequestBody_for_FRONTENDLevelResource_checkUser(String fieldName, String value)
    {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                String.format("{\"%s\": \"%s\"}", fieldName, value));
    }
}


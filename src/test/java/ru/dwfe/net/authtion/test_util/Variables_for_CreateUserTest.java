package ru.dwfe.net.authtion.test_util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Variables_for_CreateUserTest
{
    public static final String ID_notExistedUser = "notExistedUser@ya.ru";
    public static final String PASS_notExistedUser = "123456";


    /* BODIES */

    public static final List<Checker> checkers_for_checkUserId = List.of(
            Checker.of("canUse", false, Map.of(),                                       200, "details", "id", "required field"),
            Checker.of("canUse", false, Map.of("id", ""),                               200, "details", "id", "can't be empty"),
            Checker.of("canUse", false, Map.of("id", "123456789012345678901234567890"), 200, "details", "id", "length must be less than 30 characters"),
            Checker.of("canUse", false, Map.of("id", "Administrator"),                  200, "details", "id", "not allowed"),
            Checker.of("canUse", false, Map.of("id", "user"),                           200, "details", "id", "must be valid e-mail address"),
            Checker.of("canUse", false, Map.of("id", ".uuqu@mail.ru"),                  200, "details", "id", "must be valid e-mail address"),
            Checker.of("canUse", false, Map.of("id", "user@ya.ru"),                     200, "details", "id", "user is present"),
            Checker.of("canUse", true,  Map.of("id", ID_notExistedUser),                200, null,      null, null)
    );

    public static final List<Checker> checkers_for_checkUserPass = List.of(
            Checker.of("canUse", false, Map.of(),                     200, "details", "password", "required field"),
            Checker.of("canUse", false, Map.of("password", ""),       200, "details", "password", "can't be empty"),
            Checker.of("canUse", false, Map.of("password", "12345"),  200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of("canUse", true,  Map.of("password", "123456"), 200, null,       null,      null)
    );

    public static List<Checker> checkers_for_createUser()
    {
        List<Checker> list = new ArrayList<>(List.of(
                Checker.of("success", false, Map.of(),                                       200, "details", "id", "required field"),
                Checker.of("success", false, Map.of("id", ""),                               200, "details", "id", "can't be empty"),
                Checker.of("success", false, Map.of("id", "123456789012345678901234567890"), 200, "details", "id", "length must be less than 30 characters"),
                Checker.of("success", false, Map.of("id", "Administrator"),                  200, "details", "id", "not allowed"),
                Checker.of("success", false, Map.of("id", "user"),                           200, "details", "id", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("id", ".uuqu@mail.ru"),                  200, "details", "id", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("id", "user@ya.ru"),                     200, "details", "id", "user is present")
        ));
        list.addAll(List.of(
                Checker.of("success", false, Map.of("id", ID_notExistedUser),                       200, "details", "password", "required field"),
                Checker.of("success", false, Map.of("id", ID_notExistedUser, "password", ""),       200, "details", "password", "can't be empty"),
                Checker.of("success", false, Map.of("id", ID_notExistedUser, "password", "12345"),  200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of("success", true,  Map.of("id", ID_notExistedUser, "password", PASS_notExistedUser), 200, null,      null,       null)
        ));
        return list;
    }

    public static List<Checker> checkers_for_confirmUser(String existedKey){
        return List.of(
                Checker.of(null,      null,  Map.of(),                  400, null,      "message", "Required String parameter 'key' is not present"),
                Checker.of("success", false, Map.of("key", ""),         200, "details", "error",   "bad key"),
                Checker.of("success", false, Map.of("key", "123"),      200, "details", "error",   "key does not exist"),
                Checker.of("success", true,  Map.of("key", existedKey), 200, null,      null,      null)
        );
    }
}
package ru.dwfe.net.authtion.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Variables_for_CreateUserTest
{
    /* BODIES */

    public static final List<Checker> checkers_for_checkUserId = List.of(
            Checker.of(false, Map.of(), "id", "required field"),
            Checker.of(false, Map.of("id", ""), "id", "can't be empty"),
            Checker.of(false, Map.of("id", "123456789012345678901234567890"), "id", "length must be less than 30 characters"),
            Checker.of(false, Map.of("id", "Administrator"), "id", "not allowed"),
            Checker.of(false, Map.of("id", "user"), "id", "must be valid e-mail address"),
            Checker.of(false, Map.of("id", ".uuqu@mail.ru"), "id", "must be valid e-mail address"),
            Checker.of(false, Map.of("id", "user@ya.ru"), "id", "user is present"),
            Checker.of(true, Map.of("id", "notExistedUser@ya.ru"), null, null)
    );

    public static final List<Checker> checkers_for_checkUserPass = List.of(
            Checker.of(false, Map.of(), "password", "required field"),
            Checker.of(false, Map.of("password", ""), "password", "can't be empty"),
            Checker.of(false, Map.of("password", "12345"), "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of(true, Map.of("password", "123456"), null, null)
    );

    public static final List<Checker> checkers_for_createUser()
    {
        List<Checker> list = new ArrayList<>(List.of(
                Checker.of(false, Map.of(), "id", "required field"),
                Checker.of(false, Map.of("id", ""), "id", "can't be empty"),
                Checker.of(false, Map.of("id", "123456789012345678901234567890"), "id", "length must be less than 30 characters"),
                Checker.of(false, Map.of("id", "Administrator"), "id", "not allowed"),
                Checker.of(false, Map.of("id", "user"), "id", "must be valid e-mail address"),
                Checker.of(false, Map.of("id", ".uuqu@mail.ru"), "id", "must be valid e-mail address"),
                Checker.of(false, Map.of("id", "user@ya.ru"), "id", "user is present")
        ));
        list.addAll(List.of(
                Checker.of(false, Map.of("id", "notExistedUser@ya.ru"), "password", "required field"),
                Checker.of(false, Map.of("id", "notExistedUser@ya.ru", "password", ""), "password", "can't be empty"),
                Checker.of(false, Map.of("id", "notExistedUser@ya.ru", "password", "12345"), "password", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of(true, Map.of("id", "notExistedUser@ya.ru", "password", "123456"), null, null)
        ));
        return list;
    }
}
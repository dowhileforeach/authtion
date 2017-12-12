package ru.dwfe.net.authtion.test_util;

import org.springframework.boot.json.JsonParserFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Variables_for_CreateUserTest
{
    public static final String EMAIL_NEW_User = "notExistedUser@ya.ru";
    public static final String EMAIL_2_NEW_User = "not_02_ExistedUser@ya.ru";
    public static final String PASS_NEW_User = "123456";
    public static final String NEWPASS_NEW_User = "1234567890";


    /* BODIES */

    public static final List<Checker> checkers_for_checkUserEmail = List.of(
            Checker.of("canUse", false, Map.of(),                         200, "details", "email", "required field"),
            Checker.of("canUse", false, Map.of("email", ""),              200, "details", "email", "can't be empty"),
            Checker.of("canUse", false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "details", "email", "length must be less than 50 characters"),
            Checker.of("canUse", false, Map.of("email", "user"),          200, "details", "email", "must be valid e-mail address"),
            Checker.of("canUse", false, Map.of("email", ".uuqu@mail.ru"), 200, "details", "email", "must be valid e-mail address"),
            Checker.of("canUse", false, Map.of("email", "user@ya.ru"),    200, "details", "email", "user is present"),
            Checker.of("canUse", true,  Map.of("email", EMAIL_NEW_User),  200)
    );

    public static final List<Checker> checkers_for_checkUserPass = List.of(
            Checker.of("canUse", false, Map.of(),                     200, "details", "password", "required field"),
            Checker.of("canUse", false, Map.of("password", ""),       200, "details", "password", "can't be empty"),
            Checker.of("canUse", false, Map.of("password", "12345"),  200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of("canUse", false, Map.of("password", "12345678901234567890123456789012345678901234567890123456"), 200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of("canUse", true,  Map.of("password", "123456"), 200)
    );

    public static List<Checker> checkers_for_createUser()
    {
        List<Checker> list = new ArrayList<>(List.of(
                Checker.of("success", false, Map.of(),                         200, "details", "email", "required field"),
                Checker.of("success", false, Map.of("email", ""),              200, "details", "email", "can't be empty"),
                Checker.of("success", false, Map.of("email", "ogygyg_bnmkkskslwlwllogygyg_bnmkkskslwlwll@gmail.com"), 200, "details", "email", "length must be less than 50 characters"),
                Checker.of("success", false, Map.of("email", "user"),          200, "details", "email", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("email", "@puqu@mail.ru"), 200, "details", "email", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("email", "admin@ya.ru"),   200, "details", "email", "user is present")
        ));
        list.addAll(List.of(
                Checker.of("success", true,  Map.of("email", EMAIL_2_NEW_User, "firstName", "ozon"), 200),
                Checker.of("success", false, Map.of("email", EMAIL_2_NEW_User),                      200, "details", "email", "user is present"),
                Checker.of("success", false, Map.of("email", EMAIL_NEW_User, "password", ""),        200, "details", "password", "can't be empty"),
                Checker.of("success", false, Map.of("email", EMAIL_NEW_User, "password", "54321"),   200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of("success", false, Map.of("email", EMAIL_NEW_User, "password", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of("success", true,  Map.of("email", EMAIL_NEW_User, "password", PASS_NEW_User, "publicName", "nobody"), 200)
        ));
        return list;
    }

    public static final List<Checker> checkers_for_userData = List.of(
            Checker.of("success", true, Map.of(), 200, JsonParserFactory.getJsonParser().parseMap("{\n" +
                    " \"id\": 555,\n" +
                    " \"email\": \"user@ya.ru\",\n" +
                    " \"password\": \"****\",\n" +
                    " \"authorities\": [\"USER\"],\n" +
                    " \"publicName\": \"user\",\n" +
                    " \"firstName\": \"\",\n" +
                    " \"lastName\": \"\",\n" +
                    " \"accountNonExpired\": true,\n" +
                    " \"credentialsNonExpired\": true,\n" +
                    " \"accountNonLocked\": true,\n" +
                    " \"enabled\": true,\n" +
                    " \"emailConfirmed\": true\n" +
                    "}"))
    );

    public static final List<Checker> checkers_for_publicUser9 = List.of(
            Checker.of("success", false, Map.of(), 200, "details", "error", "user doesn't exist")
    );
    public static final List<Checker> checkers_for_publicUser1 = List.of(
            Checker.of("success", true, Map.of(), 200, Map.of("id", 1, "publicName", "admin"))
    );

    public static final List<Checker> checkers_for_reqConfirmEmail = List.of(
            Checker.of("success", true, Map.of(), 200)
    );

    public static List<Checker> checkers_for_confirmEmail(String existedKey)
    {
        return List.of(
                Checker.of(null,      null,  Map.of(),                  400, null,      "message", "Required String parameter 'key' is not present"),
                Checker.of("success", false, Map.of("key", ""),         200, "details", "error",   "can't be empty"),
                Checker.of("success", false, Map.of("key", "123"),      200, "details", "error",   "key does not exist"),
                Checker.of("success", true,  Map.of("key", existedKey), 200)
        );
    }

    public static final List<Checker> checkers_for_changeUserPass = List.of(
            Checker.of("success", false, Map.of(),                                                      200, "details", "oldpass", "required field"),
            Checker.of("success", false, Map.of("oldpass", ""),                                         200, "details", "oldpass", "can't be empty"),
            Checker.of("success", false, Map.of("oldpass", PASS_NEW_User),                              200, "details", "newpass", "required field"),
            Checker.of("success", false, Map.of("oldpass", PASS_NEW_User, "newpass", ""),               200, "details", "newpass", "can't be empty"),
            Checker.of("success", false, Map.of("oldpass", PASS_NEW_User, "newpass", "12345"),          200, "details", "newpass", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of("success", false, Map.of("oldpass", PASS_NEW_User + "1", "newpass", "1234567"),  200, "details", "oldpass", "wrong"),
            Checker.of("success", true,  Map.of("oldpass", PASS_NEW_User, "newpass", NEWPASS_NEW_User), 200)
    );

    public static final List<Checker> checkers_for_reqRestoreUserPass = List.of(
            Checker.of("success", false, Map.of(),                        200, "details", "email", "required field"),
            Checker.of("success", false, Map.of("email", ""),             200, "details", "email", "can't be empty"),
            Checker.of("success", false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "details", "email", "length must be less than 50 characters"),
            Checker.of("success", false, Map.of("email", "admin"),        200, "details", "email", "must be valid e-mail address"),
            Checker.of("success", false, Map.of("email", "@uu@mail.ru"),  200, "details", "email", "must be valid e-mail address"),
            Checker.of("success", false, Map.of("email", "!uu@mail.ru"),  200, "details", "error", "user doesn't exist"),
            Checker.of("success", true,  Map.of("email", EMAIL_NEW_User), 200)
    );

    public static List<Checker> checkers_for_confirmRestoreUserPass(String existedKey)
    {
        return List.of(
                Checker.of(null,      null,  Map.of(),                  400, null,      "message", "Required String parameter 'key' is not present"),
                Checker.of("success", false, Map.of("key", ""),         200, "details", "error",   "can't be empty"),
                Checker.of("success", false, Map.of("key", "657"),      200, "details", "error",   "key does not exist"),
                Checker.of("success", true,  Map.of("key", existedKey), 200, Map.of("email",EMAIL_NEW_User, "key",existedKey))
        );
    }

    public static List<Checker> checkers_for_restoreUserPass(String existedKey)
    {
        List<Checker> list = new ArrayList<>(List.of(
                Checker.of("success", false, Map.of(),                   200, "details", "newpass", "required field"),
                Checker.of("success", false, Map.of("newpass", ""),      200, "details", "newpass", "can't be empty"),
                Checker.of("success", false, Map.of("newpass", "54321"), 200, "details", "newpass", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of("success", false, Map.of("newpass", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "details", "newpass", "length must be greater than or equal to 6 and less than or equal to 55")
        ));
        list.addAll(List.of(
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User),           200, "details", "key", "required field"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key",""), 200, "details", "key", "can't be empty")
        ));
        list.addAll(List.of(
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key","123"),                              200, "details", "email", "required field"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key","123", "email",""),                  200, "details", "email", "can't be empty"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key","123", "email","lllgyg_bnmkksk12345llogygyg_bnmkkskslwlwll@gmail.com"), 200, "details", "email", "length must be less than 50 characters"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key","123", "email","shop"),              200, "details", "email", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key","123", "email","..puqu@mail.ru"),    200, "details", "email", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key","123", "email","ehlo@mail.ru"),      200, "details", "error", "key does not exist"),
                Checker.of("success", false, Map.of("newpass",PASS_NEW_User, "key",existedKey, "email","ehlo@mail.ru"), 200, "details", "error", "email from request doesn't match with email associated with key"),
                Checker.of("success", true,  Map.of("newpass",PASS_NEW_User, "key",existedKey, "email",EMAIL_NEW_User), 200)
        ));
        return list;
    }

}
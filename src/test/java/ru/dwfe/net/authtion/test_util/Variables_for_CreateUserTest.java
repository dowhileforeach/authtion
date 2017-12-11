package ru.dwfe.net.authtion.test_util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Variables_for_CreateUserTest
{
    public static final String EMAIL_notExistedUser = "notExistedUser@ya.ru";
    public static final String EMAIL_2_notExistedUser = "notExistedUser_02@ya.ru";
    public static final String PASS_notExistedUser = "123456";

//    public static final String NEWPASS_notExistedUser = "1234567";
//
//    public static final String confirm_NONisCreateNewUser_user = "shop@ya.ru";
//    public static final String confirm_NONisCreateNewUser_key = "NONisCreateNewUser";
//    public static final String confirm_SomethingWentWrong_user = "admin@ya.ru";
//    public static final String confirm_SomethingWentWrong_key = "SomethingWentWrong";




    /* BODIES */

    public static final List<Checker> checkers_for_checkUserEmail = List.of(
            Checker.of("canUse", false, Map.of(),                                                                     200, "details", "email", "required field"),
            Checker.of("canUse", false, Map.of("email", ""),                                                          200, "details", "email", "can't be empty"),
            Checker.of("canUse", false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "details", "email", "length must be less than 50 characters"),
            Checker.of("canUse", false, Map.of("email", "user"),                                                      200, "details", "email", "must be valid e-mail address"),
            Checker.of("canUse", false, Map.of("email", ".uuqu@mail.ru"),                                             200, "details", "email", "must be valid e-mail address"),
            Checker.of("canUse", false, Map.of("email", "user@ya.ru"),                                                200, "details", "email", "user is present"),
            Checker.of("canUse", true,  Map.of("email", EMAIL_notExistedUser),                                        200, null, null, null)
    );

    public static final List<Checker> checkers_for_checkUserPass = List.of(
            Checker.of("canUse", false, Map.of(),                                                                        200, "details", "password", "required field"),
            Checker.of("canUse", false, Map.of("password", ""),                                                          200, "details", "password", "can't be empty"),
            Checker.of("canUse", false, Map.of("password", "12345"),                                                     200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of("canUse", false, Map.of("password", "12345678901234567890123456789012345678901234567890123456"),  200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
            Checker.of("canUse", true,  Map.of("password", "123456"),                                                    200, null, null, null)
    );

    public static List<Checker> checkers_for_createUser()
    {
        List<Checker> list = new ArrayList<>(List.of(
                Checker.of("success", false, Map.of(),                                                                200, "details", "email", "required field"),
                Checker.of("success", false, Map.of("email", ""),                                                     200, "details", "email", "can't be empty"),
                Checker.of("success", false, Map.of("email", "ogygyg_bnmkkskslwlwllogygyg_bnmkkskslwlwll@gmail.com"), 200, "details", "email", "length must be less than 50 characters"),
                Checker.of("success", false, Map.of("email", "user"),                                                 200, "details", "email", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("email", "@puqu@mail.ru"),                                        200, "details", "email", "must be valid e-mail address"),
                Checker.of("success", false, Map.of("email", "admin@ya.ru"),                                          200, "details", "email", "user is present")
        ));
        list.addAll(List.of(
                Checker.of("success", true,  Map.of("email", EMAIL_2_notExistedUser, "firstName", "ozon"),           200, null, null, null),
                Checker.of("success", false, Map.of("email", EMAIL_2_notExistedUser),                                200, "details", "email", "user is present"),
                Checker.of("success", false, Map.of("email", EMAIL_notExistedUser, "password", ""),                  200, "details", "password", "can't be empty"),
                Checker.of("success", false, Map.of("email", EMAIL_notExistedUser, "password", "54321"),             200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of("success", false, Map.of("email", EMAIL_notExistedUser, "password", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "details", "password", "length must be greater than or equal to 6 and less than or equal to 55"),
                Checker.of("success", true,  Map.of("email", EMAIL_notExistedUser, "password", PASS_notExistedUser, "publicName", "nobody"), 200, null, null, null)
        ));
        return list;
    }

//    public static List<Checker> checkers_for_confirmUser(String existedKey){
//        return List.of(
//                Checker.of(null,      null,  Map.of(),                                      400, null,      "message", "Required String parameter 'key' is not present"),
//                Checker.of("success", false, Map.of("key", ""),                             200, "details", "error",   "bad key"),
//                Checker.of("success", false, Map.of("key", "123"),                          200, "details", "error",   "key does not exist"),
//                Checker.of("success", false, Map.of("key", confirm_NONisCreateNewUser_key), 200, "details", "error",   "key is not valid for confirmation after a new user is created"),
//                Checker.of("success", false, Map.of("key", confirm_SomethingWentWrong_key), 200, "details", "error",   "user is non locked. Something went wrong..."),
//                Checker.of("success", true,  Map.of("key", existedKey),                     200, null,      null,      null)
//        );
//    }
//
//    public static final List<Checker> checkers_for_changeUserPass = List.of(
//            Checker.of("success", false, Map.of(),                                                                  200, "details", "oldpass", "required field"),
//            Checker.of("success", false, Map.of("oldpass", ""),                                                     200, "details", "oldpass", "can't be empty"),
//            Checker.of("success", false, Map.of("oldpass", PASS_notExistedUser),                                    200, "details", "newpass", "required field"),
//            Checker.of("success", false, Map.of("oldpass", PASS_notExistedUser, "newpass", ""),                     200, "details", "newpass", "can't be empty"),
//            Checker.of("success", false, Map.of("oldpass", PASS_notExistedUser, "newpass", "12345"),                200, "details", "newpass", "length must be greater than or equal to 6 and less than or equal to 55"),
//            Checker.of("success", false, Map.of("oldpass", PASS_notExistedUser + "1", "newpass", "1234567"),        200, "details", "oldpass", "incorrect"),
//            Checker.of("success", true,  Map.of("oldpass", PASS_notExistedUser, "newpass", NEWPASS_notExistedUser), 200, null, null, null)
//    );
}
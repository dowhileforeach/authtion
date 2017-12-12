package ru.dwfe.net.authtion.test_util;

import static ru.dwfe.net.authtion.test_util.AuthorityType.*;

public class Variables_Global
{
    public static final String API_VERSION = "/v1";
    public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
    public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + API_VERSION;

    /*
        Clents to get access to resource /oauth/token
    */
    public static final Client client_TRUSTED = Client.of("Trusted", "trPass", 864_000, 180);
    public static final Client client_UNTRUSTED = Client.of("Untrusted", "untrPass", 180, 0);
    public static final Client client_FRONTEND = Client.of("Frontend", "frntndPass", 1_728_000, 864_000);

    /*
        Users from backend database
    */
    public static final UserTest USER_user = UserTest.of(USER, "user@ya.ru", "passUser", client_TRUSTED, 200);
    public static final UserTest ADMIN_user = UserTest.of(ADMIN, "admin@ya.ru", "passAdmin", client_UNTRUSTED, 200);
    public static final UserTest FRONTEND_user = UserTest.of(FRONTEND, "shop@ya.ru", "passFrontend", client_FRONTEND, 200);
    public static final UserTest ANONYMOUS_user = UserTest.getAnonymous();

    /*
        RESOURCES
    */
    public static final String resource_public = "/public";
    public static final String resource_cities = "/cities";
    public static final String resource_users = "/users";
    public static final String resource_checkUserEmail = "/check-user-email";
    public static final String resource_checkUserPass = "/check-user-pass";
    public static final String resource_createUser = "/create-user";
    public static final String resource_userUpdate = "/user-update";
    public static final String resource_userData = "/user-data";
    public static final String resource_publicUser1 = "/public/user/1";
    public static final String resource_publicUser9 = "/public/user/9";
    public static final String resource_reqConfirmEmail = "/req-confirm-email";
    public static final String resource_confirmEmail = "/confirm-email";
    public static final String resource_changeUserPass = "/change-user-pass";
    public static final String resource_reqRestoreUserPass = "/req-restore-user-pass";
    public static final String resource_confirmRestoreUserPass = "/confirm-restore-user-pass";
    public static final String resource_restoreUserPass = "/restore-user-pass";
}

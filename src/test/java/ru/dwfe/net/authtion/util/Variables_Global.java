package ru.dwfe.net.authtion.util;

import static ru.dwfe.net.authtion.util.AuthorityType.*;

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
    public static final User user_USER = User.of(USER, "user@ya.ru", "passUser", client_TRUSTED);
    public static final User user_ADMIN = User.of(ADMIN, "admin@ya.ru", "passAdmin", client_UNTRUSTED);
    public static final User user_FRONTEND = User.of(FRONTEND, "shop@ya.ru", "passFrontend", client_FRONTEND);
    public static final User user_ANONYMOUS = User.getAnonymous();

    /*
        RESOURCES
    */
    public static final String resource_public = "/public";
    public static final String resource_cities = "/cities";
    public static final String resource_users = "/users";
    public static final String resource_checkUserId = "/check-user-id";
    public static final String resource_checkUserPass = "/check-user-pass";
    public static final String resource_createUser = "/create-user";
    public static final String resource_confirmUser = "/confirm-user";
}

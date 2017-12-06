package ru.dwfe.net.authtion.util;

public class Variables_Global
{
    public static final String API_VERSION = "/v1";
    public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
    public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + API_VERSION;

    /*Clents*/
    public static final String trusted_clientname = "Trusted";
    public static final String trusted_clientpass = "trPass";
    public static final int trusted_maxTokenExpirationTime = 864_000;
    public static final int trusted_minTokenExpirationTime = 180;

    public static final String untrusted_clientname = "Untrusted";
    public static final String untrusted_clientpass = "untrPass";
    public static final int untrusted_maxTokenExpirationTime = 180;
    public static final int untrusted_minTokenExpirationTime = 0;

    public static final String frontend_clientname = "Frontend";
    public static final String frontend_clientpass = "frntndPass";
    public static final int frontend_maxTokenExpirationTime = 1_728_000;
    public static final int frontend_minTokenExpirationTime = 864_000;


    /* user = USER */
    public static final String user_username = "user@ya.ru";
    public static final String user_userpass = "passUser";


    /* admin = ADMIN */
    public static final String admin_username = "admin@ya.ru";
    public static final String admin_userpass = "passAdmin";


    /* shop = FRONTEND */
    public static final String shop_username = "shop@ya.ru";
    public static final String shop_userpass = "passFrontend";


    /* RESOURCES*/
    public static final String PUBLICLevelResource_public = "/public";
    public static final String USERLevelResource_cities = "/cities";
    public static final String ADMINLevelResource_users = "/users";
    public static final String FRONTENDLevelResource_checkUserId = "/check-user-id";
    public static final String FRONTENDLevelResource_createUser = "/create-user";
    public static final String PUBLICLevelResource_confirmUser = "/confirm-user";
}

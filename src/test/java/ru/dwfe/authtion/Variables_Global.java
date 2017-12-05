package ru.dwfe.authtion;

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


    /* user */
    public static final String user_username = "user";
    public static final String user_userpass = "passUser";


    /* admin */
    public static final String admin_username = "admin";
    public static final String admin_userpass = "passAdmin";


    /* shop */
    public static final String shop_username = "shop";
    public static final String shop_userpass = "passFrontend";


    /* RESOURCES*/
    public static final String publicLevelResource_public = "/public";
    public static final String userLevelResource_cities = "/cities";
    public static final String adminLevelResource_users = "/users";
    public static final String frontendLevelResource_checkUserId = "/check-user-id";
    public static final String frontendLevelResource_createUser = "/create-user";
    public static final String publicLevelResource_confirmUser = "/confirm-user";
}

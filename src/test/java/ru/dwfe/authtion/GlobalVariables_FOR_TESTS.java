package ru.dwfe.authtion;

public class GlobalVariables_FOR_TESTS
{
    public static final String API_VERSION = "/v1";
    public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
    public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + API_VERSION;

    /* user */
    public static final String user_clientname = "Standard";
    public static final String user_clientpass = "Login";
    public static final String user_username = "user";
    public static final String user_userpass = "passUser";
    public static final int user_maxTokenExpirationTime = 864000;

    /* admin */
    public static final String admin_clientname = "ThirdParty";
    public static final String admin_clientpass = "Computer";
    public static final String admin_username = "admin";
    public static final String admin_userpass = "passAdmin";
    public static final int admin_maxTokenExpirationTime = 180;

    /* RESOURCES*/
    public static final String publicLevelResource = "/public";
    public static final String userLevelResource = "/cities";
    public static final String adminLevelResource = "/users";

}

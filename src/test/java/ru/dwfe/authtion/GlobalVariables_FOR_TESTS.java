package ru.dwfe.authtion;

public class GlobalVariables_FOR_TESTS
{
    public static final String API_VERSION = "/v1";
    public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
    public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + API_VERSION;

    /*Clents*/
    public static final String standard_clientname = "Standard";
    public static final String standard_clientpass = "Login";
    public static final int standard_maxTokenExpirationTime = 864000;

    public static final String thirdPartyComp_clientname = "ThirdParty";
    public static final String thirdPartyComp_clientpass = "Computer";
    public static final int thirdPartyComp_maxTokenExpirationTime = 180;


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
    public static final String publicLevelResource = "/public";
    public static final String userLevelResource = "/cities";
    public static final String adminLevelResource = "/users";
    public static final String frontendLevelResource_checkUserId = "/check-user-id";
    public static final String frontendLevelResource_addUser = "/create-user";
}

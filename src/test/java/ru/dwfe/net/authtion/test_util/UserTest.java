package ru.dwfe.net.authtion.test_util;

public class UserTest
{

    String username;
    String password;

    Client client;

    public AuthorityType level;

    public String access_token;


    public static UserTest of(AuthorityType level, String username, String password, Client client, int loginExpectedStatus)
    {
        UserTest userTest = new UserTest();
        userTest.level = level;
        userTest.username = username;
        userTest.password = password;
        userTest.client = client;

        Util.setAccessToken(userTest, loginExpectedStatus);

        return userTest;
    }

    public static UserTest getAnonymous()
    {
        UserTest userTest = new UserTest();
        userTest.level = AuthorityType.ANY;
        return userTest;
    }
}

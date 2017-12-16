package ru.dwfe.net.authtion.test_util;

public class ConsumerTest
{

    public String username;
    String password;

    Client client;

    public AuthorityLevel level;

    public String access_token;


    public static ConsumerTest of(AuthorityLevel level, String username, String password, Client client, int loginExpectedStatus)
    {
        ConsumerTest consumerTest = new ConsumerTest();
        consumerTest.level = level;
        consumerTest.username = username;
        consumerTest.password = password;
        consumerTest.client = client;

        UtilTest.setAccessToken(consumerTest, loginExpectedStatus);

        return consumerTest;
    }

    public static ConsumerTest getAnonymous()
    {
        ConsumerTest consumerTest = new ConsumerTest();
        consumerTest.level = AuthorityLevel.ANY;
        return consumerTest;
    }
}

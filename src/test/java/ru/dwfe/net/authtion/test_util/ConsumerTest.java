package ru.dwfe.net.authtion.test_util;

import static ru.dwfe.net.authtion.test_util.SignInType.SignIn;

public class ConsumerTest
{

    public String username;
    String password;

    Client client;

    public AuthorityLevel level;

    public String access_token;
    public String refresh_token;


    public static ConsumerTest of(AuthorityLevel level, String username, String password, Client client, int signInExpectedStatus)
    {
        ConsumerTest consumerTest = new ConsumerTest();
        consumerTest.level = level;
        consumerTest.username = username;
        consumerTest.password = password;
        consumerTest.client = client;

        UtilTest.setNewTokens(consumerTest, signInExpectedStatus, SignIn);

        return consumerTest;
    }

    static ConsumerTest getAnonymous()
    {
        ConsumerTest consumerTest = new ConsumerTest();
        consumerTest.level = AuthorityLevel.ANY;
        return consumerTest;
    }
}

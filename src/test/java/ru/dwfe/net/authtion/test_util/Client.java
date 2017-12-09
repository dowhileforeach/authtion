package ru.dwfe.net.authtion.test_util;

public class Client
{
    String clientname;
    String clientpass;
    int maxTokenExpirationTime;
    int minTokenExpirationTime;

    public static Client of(String clientname, String clientpass, int maxTokenExpirationTime, int minTokenExpirationTime)
    {
        Client client = new Client();
        client.clientname = clientname;
        client.clientpass = clientpass;
        client.maxTokenExpirationTime = maxTokenExpirationTime;
        client.minTokenExpirationTime = minTokenExpirationTime;

        return client;
    }
}

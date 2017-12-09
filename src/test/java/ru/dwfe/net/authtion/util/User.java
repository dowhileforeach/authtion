package ru.dwfe.net.authtion.util;

public class User
{

    String username;
    String password;

    Client client;

    public AuthorityType level;

    public String access_token;


    public static User of(AuthorityType level, String username, String password, Client client)
    {
        User user = new User();
        user.level = level;
        user.username = username;
        user.password = password;
        user.client = client;

        Util.setAccessToken(user);

        return user;
    }

    public static User getAnonymous()
    {
        User user = new User();
        user.level = AuthorityType.ANY;
        return user;
    }
}

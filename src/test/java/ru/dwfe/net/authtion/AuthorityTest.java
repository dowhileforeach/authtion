package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dwfe.net.authtion.util.User;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static ru.dwfe.net.authtion.util.Util.checkAllResources;
import static ru.dwfe.net.authtion.util.Variables_Global.*;
import static ru.dwfe.net.authtion.util.Variables_for_AuthorityTest.TOTAL_ACCESS_TOKEN_COUNT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorityTest
{
    private static Set<String> access_tokens = new HashSet<>();

    @Test
    public void _01_user() throws Exception
    {
        logHead("user");

        User user = user_USER;
        access_tokens.add(user.access_token);

        checkAllResources(user);
    }

    @Test
    public void _02_admin() throws Exception
    {
        logHead("admin");

        User user = user_ADMIN;
        access_tokens.add(user.access_token);

        checkAllResources(user);
    }

    @Test
    public void _03_shop() throws Exception
    {
        logHead("shop");

        User user = user_FRONTEND;
        access_tokens.add(user.access_token);

        checkAllResources(user);

    }

    @Test
    public void _04_anonymous() throws Exception
    {
        logHead("anonymous");

        checkAllResources(user_ANONYMOUS);
    }

    @Test
    public void _05_different_access_tokens()
    {
        logHead("list of Access Tokens");
        log.info("\n\n{}", access_tokens.stream().collect(Collectors.joining("\n")));

        assertEquals(TOTAL_ACCESS_TOKEN_COUNT, access_tokens.size());
    }


    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }

    private static final Logger log = LoggerFactory.getLogger(AuthorityTest.class);
}

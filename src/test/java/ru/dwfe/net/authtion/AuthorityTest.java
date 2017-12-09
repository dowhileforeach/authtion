package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dwfe.net.authtion.test_util.UserTest;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static ru.dwfe.net.authtion.test_util.Util.checkAllResources;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;
import static ru.dwfe.net.authtion.test_util.Variables_for_AuthorityTest.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorityTest
{
    private static Set<String> access_tokens = new HashSet<>();

    @Test
    public void _01_USER() throws Exception
    {
        logHead("USER");

        UserTest userTest = USERtest_USER;
        access_tokens.add(userTest.access_token);

        checkAllResources(userTest);
    }

    @Test
    public void _02_ADMIN() throws Exception
    {
        logHead("ADMIN");

        UserTest userTest = USERtest_ADMIN;
        access_tokens.add(userTest.access_token);

        checkAllResources(userTest);
    }

    @Test
    public void _03_FRONTEND() throws Exception
    {
        logHead("FRONTEND");

        UserTest userTest = USERtest_FRONTEND;
        access_tokens.add(userTest.access_token);

        checkAllResources(userTest);

    }

    @Test
    public void _04_ANONYMOUS() throws Exception
    {
        logHead("ANONYMOUS");

        checkAllResources(USERtest_ANONYMOUS);
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

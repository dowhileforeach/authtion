package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dwfe.net.authtion.test_util.ConsumerTest;
import ru.dwfe.net.authtion.test_util.SignInType;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static ru.dwfe.net.authtion.test_util.SignInType.Refresh;
import static ru.dwfe.net.authtion.test_util.UtilTest.performResourceAccessing;
import static ru.dwfe.net.authtion.test_util.UtilTest.setNewTokens;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;
import static ru.dwfe.net.authtion.test_util.Variables_for_AuthorityTest.TOTAL_ACCESS_TOKEN_COUNT;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthTest
{
    private static Set<String> access_tokens = new HashSet<>();

    @Test
    public void _01_USER()
    {
        logHead("USER");

        ConsumerTest consumerTest = USER_consumer;
        access_tokens.add(consumerTest.access_token);

        performResourceAccessing(consumerTest);

        String old_access_token = consumerTest.access_token;
        String old_refresh_token = consumerTest.refresh_token;
        setNewTokens(consumerTest, 200, Refresh);
        assertEquals(false, old_access_token.equals(consumerTest.access_token));
        assertEquals(false, old_refresh_token.equals(consumerTest.refresh_token));
    }

    @Test
    public void _02_ADMIN()
    {
        logHead("ADMIN");

        ConsumerTest consumerTest = ADMIN_consumer;
        access_tokens.add(consumerTest.access_token);

        performResourceAccessing(consumerTest);
    }

    @Test
    public void _03_FRONTEND()
    {
        logHead("FRONTEND");

        ConsumerTest consumerTest = FRONTEND_consumer;
        access_tokens.add(consumerTest.access_token);

        performResourceAccessing(consumerTest);

    }

    @Test
    public void _04_ANONYMOUS()
    {
        logHead("ANONYMOUS");

        performResourceAccessing(ANONYMOUS_consumer);
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

    private static final Logger log = LoggerFactory.getLogger(AuthTest.class);
}

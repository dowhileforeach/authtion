package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dwfe.net.authtion.util.Checker;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static ru.dwfe.net.authtion.util.ClientType.TRUSTED;
import static ru.dwfe.net.authtion.util.Util.*;
import static ru.dwfe.net.authtion.util.Variables_Global.*;
import static ru.dwfe.net.authtion.util.Variables_for_CreateUserTest.checkers_for_checkUserId;
import static ru.dwfe.net.authtion.util.Variables_for_CreateUserTest.getRequestBody_for_FRONTENDLevelResource_checkUserId;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateUserTest
{
    @Test
    public void _01_checkUserId() throws Exception
    {
        logHead("Check User ID");

        String access_token = getAccessToken(TRUSTED, shop_username, shop_userpass);
        check("canUse", FRONTENDLevelResource_checkUserId, access_token, checkers_for_checkUserId);
    }

    @Test
    public void _02_createUser() throws Exception
    {
        logHead("Create User");
    }

    @Test
    public void _03_confirmUser() throws Exception
    {
        logHead("Confirm User");
    }

    private void check(String fieldName, String resource, String access_token,
                       List<Checker> checkers) throws Exception
    {
        for (Checker checker : checkers)
        {
            String body = getResponseAfterPOSTrequest(access_token, resource, getRequestBody_for_FRONTENDLevelResource_checkUserId(checker.sendValue));

            Map<String, Object> map = parse(body);
            assertEquals(checker.expectedResult, getValueFromResponse(map, fieldName));

            if (!checker.expectedResult) //if error is expected
                assertEquals(checker.expectedError, getValueFromValueFromResponse(map, "details", "error"));
        }
    }

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }


    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

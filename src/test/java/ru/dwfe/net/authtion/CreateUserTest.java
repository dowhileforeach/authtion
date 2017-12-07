package ru.dwfe.net.authtion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.MediaType;
import okhttp3.RequestBody;
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
import static ru.dwfe.net.authtion.util.Variables_for_CreateUserTest.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateUserTest
{
    private static String access_token = getAccessToken(TRUSTED, shop_username, shop_userpass);

    @Test
    public void _01_checkUserId() throws Exception
    {
        logHead("Check User ID");
        checkList("canUse", FRONTENDLevelResource_checkUserId, access_token, checkers_for_checkUserId);
    }

    @Test
    public void _02_checkUserPass() throws Exception
    {
        logHead("Check User Pass");
        checkList("canUse", FRONTENDLevelResource_checkUserPass, access_token, checkers_for_checkUserPass);
    }

    @Test
    public void _03_createUser() throws Exception
    {
        logHead("Create User");
        checkList("success", FRONTENDLevelResource_createUser, access_token, checkers_for_createUser());
    }

    @Test
    public void _04_confirmUser() throws Exception
    {
        logHead("Confirm User");
    }

    private void checkList(String responseFieldName, String resource, String access_token,
                           List<Checker> checkers) throws Exception
    {
        for (Checker checker : checkers)
        {
            String body = getResponseAfterPOSTrequest(access_token, resource, getRequestBody(checker.req));
            Map<String, Object> map = parse(body);

            assertEquals(checker.expectedResult, getValueFromResponse(map, responseFieldName));

            if (!checker.expectedResult) //if error is expected
                assertEquals(checker.expectedError, getValueFromValueFromResponse(map, "details", checker.expectedErrorFieldName));
        }
    }

    private static RequestBody getRequestBody(Map<String, Object> map) throws JsonProcessingException
    {
        return RequestBody.create(MediaType.parse("application/json; charset=utf-8"),
                new ObjectMapper().writeValueAsString(map));
    }

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }

    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

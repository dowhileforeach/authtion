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
import org.springframework.web.bind.annotation.RequestMethod;
import ru.dwfe.net.authtion.util.Checker;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.springframework.web.bind.annotation.RequestMethod.*;
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
        checkList(POST, FRONTENDLevelResource_checkUserId, access_token, checkers_for_checkUserId);
    }

    @Test
    public void _02_checkUserPass() throws Exception
    {
        logHead("Check User Pass");
        checkList(POST, FRONTENDLevelResource_checkUserPass, access_token, checkers_for_checkUserPass);
    }

    @Test
    public void _03_createUser() throws Exception
    {
        logHead("Create User");
        checkList(POST, FRONTENDLevelResource_createUser, access_token, checkers_for_createUser());
    }

    @Test
    public void _04_confirmUser() throws Exception
    {
        logHead("Confirm User");
        checkList(GET, PUBLICLevelResource_confirmUser, access_token, checkers_for_confirmUser);
    }

    private void checkList(RequestMethod method, String resource, String access_token, List<Checker> checkers)
    {
        String body;
        for (Checker checker : checkers)
        {
            if (method == POST)
                body = getResponseAfterPOSTrequest(access_token, resource, getRequestBody(checker.req), checker.expectedStatus);
            else
                body = getResponseAfterGETrequest(null, resource, checker.req, checker.expectedStatus);

            Map<String, Object> map = parse(body);

            if (checker.resultFieldName != null)
                assertEquals(checker.expectedResult, getValueFromResponse(map, checker.resultFieldName));

            if (checker.expectedError != null)  //if error is expected
            {
                String expectedErrorContainer = checker.expectedErrorContainer;
                if (expectedErrorContainer == null) //нет вложенного контейнера
                    assertEquals(checker.expectedError, getValueFromResponse(map, checker.expectedErrorFieldName));
                else
                    assertEquals(checker.expectedError, getValueFromValueFromResponse(map, expectedErrorContainer, checker.expectedErrorFieldName));
            }
        }
    }

    private static RequestBody getRequestBody(Map<String, Object> map)
    {
        RequestBody result = null;
        try
        {
            result = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new ObjectMapper().writeValueAsString(map));
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }

    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

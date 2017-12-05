package ru.dwfe.net.authtion;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import static org.junit.Assert.assertEquals;
import static ru.dwfe.net.authtion.Utils.ClientType.TRUSTED;
import static ru.dwfe.net.authtion.Utils.*;
import static ru.dwfe.net.authtion.Variables_Global.*;
import static ru.dwfe.net.authtion.Variables_for_CreateUserTest.userIDlist_for_checkUserId;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateUserTest
{
    @Test
    public void _01_checkUserId() throws Exception
    {
        logHead("Check User ID");

        String access_token = getAccessToken(TRUSTED, shop_username, shop_userpass);

        userIDlist_for_checkUserId.forEach((key, value) -> {
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), "{\"id\": \"" + key + "\"}");
            try
            {
                String body = getResponseAfterPOSTrequest(access_token, FRONTENDLevelResource_checkUserId, requestBody);
                assertEquals(false, getValueFromResponse(body, "canUseID"));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });

//        String body = getResponseAfterPOSTrequest(access_token, FRONTENDLevelResource_checkUserId,
//                body_for_FRONTENDLevelResource_checkUserId_existedUser);
//        assertEquals(false, getValueFromResponse(body, "canUseID"));
//
//        body = getResponseAfterPOSTrequest(access_token, FRONTENDLevelResource_checkUserId,
//                body_for_FRONTENDLevelResource_checkUserId_notExistedUser);
//        assertEquals(true, getValueFromResponse(body, "canUseID"));
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

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }


    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();
}

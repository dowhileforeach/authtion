package ru.dwfe.net.authtion;

import okhttp3.Request;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static ru.dwfe.net.authtion.HTTP_utils.*;
import static ru.dwfe.net.authtion.HTTP_utils.ClientType.*;
import static ru.dwfe.net.authtion.Variables_Global.*;
import static ru.dwfe.net.authtion.Variables_for_CreateUserTest.*;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateUserTest
{
    @Test
    public void _01_checkUserId() throws Exception
    {
        logHead("Check User ID");

        String access_token = getAccessToken(TRUSTED, shop_username, shop_userpass);
        Request request = POST_request(ALL_BEFORE_RESOURCE + FRONTENDLevelResource_checkUserId, access_token, body_for_FRONTENDLevelResource_checkUserId_admin);

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
}

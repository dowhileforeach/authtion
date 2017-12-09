package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.util.Util.check_send_data;
import static ru.dwfe.net.authtion.util.Variables_Global.*;
import static ru.dwfe.net.authtion.util.Variables_for_CreateUserTest.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateUserTest
{
    private static String access_token = user_FRONTEND.access_token;

    @Test
    public void _01_checkUserId() throws Exception
    {
        logHead("Check User ID");
        check_send_data(POST, resource_checkUserId, access_token, checkers_for_checkUserId);
    }

    @Test
    public void _02_checkUserPass() throws Exception
    {
        logHead("Check User Pass");
        check_send_data(POST, resource_checkUserPass, access_token, checkers_for_checkUserPass);
    }

    @Test
    public void _03_createUser() throws Exception
    {
        logHead("Create User");
        check_send_data(POST, resource_createUser, access_token, checkers_for_createUser());
    }

    @Test
    public void _04_confirmUser() throws Exception
    {
        logHead("Confirm User");
        check_send_data(GET, resource_confirmUser, null, checkers_for_confirmUser);
    }

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);
    }

    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

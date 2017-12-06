package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static ru.dwfe.net.authtion.util.ClientType.*;
import static ru.dwfe.net.authtion.util.Util.*;
import static ru.dwfe.net.authtion.util.Variables_Global.*;
import static ru.dwfe.net.authtion.util.Variables_for_AuthorityTest.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorityTest
{
    private static Set<String> access_tokens = new HashSet<>();

    @Test
    public void _01_user() throws Exception
    {
        logHead("user");

        String access_token = getAccessToken(TRUSTED, user_username, user_userpass);
        access_tokens.add(access_token);

        checkAllResources(
                access_token,
                user_USERLevelResource_expectedStatus,
                user_ADMINLevelResource_expectedStatus,
                user_FRONTENDLevelResource_expectedStatus
        );
    }

    @Test
    public void _02_admin() throws Exception
    {
        logHead("admin");

        String access_token = getAccessToken(UNTRUSTED, admin_username, admin_userpass);
        access_tokens.add(access_token);

        checkAllResources(
                access_token,
                admin_USERLevelResource_expectedStatus,
                admin_ADMINLevelResource_expectedStatus,
                admin_FRONTENDLevelResource_expectedStatus
        );
    }

    @Test
    public void _03_shop() throws Exception
    {
        logHead("shop");

        String access_token = getAccessToken(FRONTEND, shop_username, shop_userpass);
        access_tokens.add(access_token);

        checkAllResources(
                access_token,
                shop_USERLevelResource_expectedStatus,
                shop_ADMINLevelResource_expectedStatus,
                shop_FRONTENDLevelResource_expectedStatus
        );
    }

    @Test
    public void _04_anonymous() throws Exception
    {
        logHead("anonymous");

        checkAllResources(null,
                anonymous_USERLevelResource_expectedStatus,
                anonymous_ADMINLevelResource_expectedStatus,
                anonymous_FRONTENDLevelResource_expectedStatus
        );
    }

    @Test
    public void _05_different_access_tokens()
    {
        logHead("list of Access Tokens");
        log.info("\n\n{}", access_tokens.stream().collect(Collectors.joining("\n")));

        assertEquals(totalAccessTokenCount, access_tokens.size());
    }

    private static void checkAllResources(String access_token,
                                          int USERLevelResource_expectedStatus,
                                          int ADMINLevelResource_expectedStatus,
                                          int FRONTENDLevelResource_expectedStatus
    ) throws Exception
    {
        performRequest(GET_request(ALL_BEFORE_RESOURCE + PUBLICLevelResource_public, access_token, null)
                , 200); // success for all levels

        performRequest(GET_request(ALL_BEFORE_RESOURCE + USERLevelResource_cities, access_token, null)
                , USERLevelResource_expectedStatus);

        performRequest(GET_request(ALL_BEFORE_RESOURCE + ADMINLevelResource_users, access_token, null)
                , ADMINLevelResource_expectedStatus);

        performRequest(POST_request(ALL_BEFORE_RESOURCE + FRONTENDLevelResource_checkUserId, access_token, body_for_FRONTENDLevelResource_checkUserId)
                , FRONTENDLevelResource_expectedStatus);

        performRequest(POST_request(ALL_BEFORE_RESOURCE + FRONTENDLevelResource_createUser, access_token, body_for_FRONTENDLevelResource_createUser)
                , FRONTENDLevelResource_expectedStatus);

        performRequest(GET_request(ALL_BEFORE_RESOURCE + PUBLICLevelResource_confirmUser, access_token, queries_for_PUBLICLevelResource_confirmUser)
                , 200); // success for all levels
    }
    
    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }

    private static final Logger log = LoggerFactory.getLogger(AuthorityTest.class);
}

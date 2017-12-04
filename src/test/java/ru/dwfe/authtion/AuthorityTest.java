package ru.dwfe.authtion;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import ru.dwfe.authtion.dao.User;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static ru.dwfe.authtion.GlobalVariables_FOR_TESTS.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorityTest
{
    @Test
    public void _01_user() throws Exception
    {
        logHead("user");

        Request req = authPostRequest(standard_clientname, standard_clientpass, user_username, user_userpass);
        String access_token = login(req, standard_maxTokenExpirationTime);

        checkAllResources(access_token, 200, 403, 403, 403, null, null);
        //200 = OK
        //403 = Forbidden, access_denied
    }

    @Test
    public void _02_admin() throws Exception
    {
        logHead("admin");

        Request req = authPostRequest(thirdPartyComp_clientname, thirdPartyComp_clientpass, admin_username, admin_userpass);
        String access_token = login(req, thirdPartyComp_maxTokenExpirationTime);

        checkAllResources(access_token, 200, 200, 403, 403, null, null);
        //200 = OK
        //403 = Forbidden, access_denied
    }

    @Test
    public void _03_shop() throws Exception
    {
        logHead("shop");

        RequestBody body_for_checkUserId = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{\"id\": \"user\"}");

        RequestBody body_for_createUser = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{" +
                "\"id\": \"user\"," +
                "\"password\": \"somePass\"," +
                "\"firstName\": \"olala\"," +
                "\"lastName\": \"\"" +
                "}"
        );

//        RequestBody body_for_createUser = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

        Request req = authPostRequest(standard_clientname, standard_clientpass, shop_username, shop_userpass);
        String access_token = login(req, standard_maxTokenExpirationTime);

        checkAllResources(access_token, 403, 403, 200, 200, body_for_checkUserId, body_for_createUser);
        //200 = OK
        //403 = Forbidden, access_denied
    }


    @Test
    public void _04_anonymous() throws Exception
    {
        logHead("anonymous");

        checkAllResources(null, 401, 401, 401, 401, null, null);
        //401 = Unauthorized
    }

    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private static String login(Request req, Integer expires) throws Exception
    {
        log.info("get Token");
        log.info("-> Authorization: {}", req.header("Authorization"));
        log.info("-> " + req.url().toString());

        Map<String, Object> parsedBody = performAuthentification(req);

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((Integer) parsedBody.get("expires_in"),
                is(both(greaterThan(0)).and(lessThanOrEqualTo(expires))));

        return access_token;
    }

    private void checkAllResources(String access_token,
                                   int userLevelResource_expectedStatus,
                                   int adminLevelResource_expectedStatus,
                                   int frontendLevelResource_checkUserId_expectedStatus,
                                   int frontendLevelResource_addUser_expectedStatus,
                                   RequestBody body_for_checkUserId,
                                   RequestBody body_for_createUser
    ) throws Exception
    {
        checkResource(simpleGetRequest(ALL_BEFORE_RESOURCE + publicLevelResource, access_token)
                , 200); // success for all levels

        checkResource(simpleGetRequest(ALL_BEFORE_RESOURCE + userLevelResource, access_token)
                , userLevelResource_expectedStatus);

        checkResource(simpleGetRequest(ALL_BEFORE_RESOURCE + adminLevelResource, access_token)
                , adminLevelResource_expectedStatus);

        checkResource(simplePostRequest(ALL_BEFORE_RESOURCE + frontendLevelResource_checkUserId, access_token, body_for_checkUserId)
                , frontendLevelResource_checkUserId_expectedStatus);

        checkResource(simplePostRequest(ALL_BEFORE_RESOURCE + frontendLevelResource_addUser, access_token, body_for_createUser)
                , frontendLevelResource_addUser_expectedStatus);
    }

    private static void checkResource(Request req, int expectedStatus) throws Exception
    {
        log.info("-> " + req.url().encodedPath());
        Map<String, Object> result = performResourceChecking(req);
        assertEquals(expectedStatus, result.get("statusCode"));
    }

    private static Map<String, Object> performAuthentification(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String respBody = response.body().string();
            log.info("<- token\n{}", respBody);
            assertEquals(200, response.code());

            return jsonParser.parseMap(respBody);
        }
    }

    private static Map<String, Object> performResourceChecking(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String respBody = response.body().string();
            log.info("<- " + respBody);

            Map<String, Object> map = new HashMap<>();
            map.put("statusCode", response.code());

            String resource = req.url().pathSegments().get(1);

            if ("users".equals(resource))
            {
                if (respBody.contains("denied") || respBody.contains("unauthorized"))
                    map.put("parsedBody", jsonParser.parseMap(respBody));
                else
                {
                    ObjectMapper mapper = new ObjectMapper();
                    User[] users = mapper.readValue(respBody, User[].class);
                    map.put("parsedBody", users);
                }
            }
            else
                map.put("parsedBody", jsonParser.parseMap(respBody));

            return map;
        }
    }

    private Request simpleGetRequest(String url, String access_token)
    {
        Request request;

        if (access_token == null)
            return new Request.Builder().url(url).build();
        else
            return new Request.Builder().url(url).addHeader("Authorization", "Bearer " + access_token).build();
    }

    private Request simplePostRequest(String url, String access_token, RequestBody body)
    {
        Request.Builder req = new Request.Builder().url(url);

        if (access_token != null)
            req.addHeader("Authorization", "Bearer " + access_token);

        if (body == null)
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

        req.post(body);

        return req.build();

    }

    private Request authPostRequest(String clientname, String clientpass, String username, String userpass)
    {
        String url = String.format(PROTOCOL_HOST_PORT
                        + "/oauth/token?grant_type=password&username=%s&password=%s",
                username, userpass);

        log.info("Client credentials - {}:{}", clientname, clientpass);
        log.info("User credentials - {}:{}", username, userpass);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(clientname, clientpass))
                .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), ""))
                .build();
    }

    private static final Logger log = LoggerFactory.getLogger(AuthorityTest.class);

    private void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }
}

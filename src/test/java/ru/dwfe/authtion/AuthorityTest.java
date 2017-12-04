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
import static ru.dwfe.authtion.AuthorityTest_Variables.*;
import static ru.dwfe.authtion.Global_Test_Variables.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorityTest
{
    @Test
    public void _01_user() throws Exception
    {
        logHead("user");

        Request req = authPostRequest(standard_clientname, standard_clientpass, user_username, user_userpass);
        String access_token = login(req, standard_maxTokenExpirationTime);

        checkAllResources(access_token,
                user_userLevelResource_expectedStatus, //cities
                user_adminLevelResource_expectedStatus, //users
                user_frontendLevelResource_checkUserId_expectedStatus, //check-user-id
                user_frontendLevelResource_createUser_expectedStatus //create-user
        );
    }

    @Test
    public void _02_admin() throws Exception
    {
        logHead("admin");

        Request req = authPostRequest(thirdPartyComp_clientname, thirdPartyComp_clientpass, admin_username, admin_userpass);
        String access_token = login(req, thirdPartyComp_maxTokenExpirationTime);

        checkAllResources(access_token,
                admin_userLevelResource_expectedStatus, //cities
                admin_adminLevelResource_expectedStatus, //users
                admin_frontendLevelResource_checkUserId_expectedStatus, //check-user-id
                admin_frontendLevelResource_createUser_expectedStatus //create-user
        );
    }

    @Test
    public void _03_shop() throws Exception
    {
        logHead("shop");

        Request req = authPostRequest(standard_clientname, standard_clientpass, shop_username, shop_userpass);
        String access_token = login(req, standard_maxTokenExpirationTime);

        checkAllResources(access_token,
                shop_userLevelResource_expectedStatus, //cities
                shop_adminLevelResource_expectedStatus, //users
                shop_frontendLevelResource_checkUserId_expectedStatus, //check-user-id
                shop_frontendLevelResource_createUser_expectedStatus //create-user
        );
    }


    @Test
    public void _04_anonymous() throws Exception
    {
        logHead("anonymous");

        checkAllResources(null,
                anonymous_userLevelResource_expectedStatus, //cities
                anonymous_adminLevelResource_expectedStatus, //users
                anonymous_frontendLevelResource_checkUserId_expectedStatus, //check-user-id
                anonymous_frontendLevelResource_createUser_expectedStatus //create-user
        );
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
                                   int frontendLevelResource_createUser_expectedStatus
    ) throws Exception
    {
        checkResource(GET_request(ALL_BEFORE_RESOURCE + publicLevelResource_public, access_token, null)
                , 200); // success for all levels

        checkResource(GET_request(ALL_BEFORE_RESOURCE + userLevelResource, access_token, null)
                , userLevelResource_expectedStatus);

        checkResource(GET_request(ALL_BEFORE_RESOURCE + adminLevelResource, access_token, null)
                , adminLevelResource_expectedStatus);

        checkResource(POST_request(ALL_BEFORE_RESOURCE + frontendLevelResource_checkUserId, access_token, body_for_frontendLevelResource_checkUserId)
                , frontendLevelResource_checkUserId_expectedStatus);

        checkResource(POST_request(ALL_BEFORE_RESOURCE + frontendLevelResource_createUser, access_token, body_for_frontendLevelResource_createUser)
                , frontendLevelResource_createUser_expectedStatus);

        checkResource(GET_request(ALL_BEFORE_RESOURCE + publicLevelResource_confirmUser, access_token, queries_for_publicLevelResource_confirmUser)
                , 200); // success for all levels
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

    private Request GET_request(String url, String access_token, Map<String, String> queries)
    {
        Request request;

        if (access_token == null)
            request = new Request.Builder().url(url).build();
        else
            request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + access_token).build();

        if (queries != null)
            for (Map.Entry<String, String> next : queries.entrySet())
            {
                HttpUrl newUrl = request.url().newBuilder()
                        .addQueryParameter(next.getKey(), next.getValue())
                        .build();
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
            }
        return request;
    }

    private Request POST_request(String url, String access_token, RequestBody body)
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

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
import static ru.dwfe.authtion.Variables_Global.*;
import static ru.dwfe.authtion.Variables_for_AuthorityTest.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorityTest
{
    @Test
    public void _01_user() throws Exception
    {
        logHead("user");

        checkAllResources(
                getAccessToken(ClientType.TRUSTED, user_username, user_userpass),
                user_userLevelResource_expectedStatus,
                user_adminLevelResource_expectedStatus,
                user_frontendLevelResource_expectedStatus
        );
    }

    @Test
    public void _02_admin() throws Exception
    {
        logHead("admin");

        checkAllResources(
                getAccessToken(ClientType.UNTRUSTED, admin_username, admin_userpass),
                admin_userLevelResource_expectedStatus,
                admin_adminLevelResource_expectedStatus,
                admin_frontendLevelResource_expectedStatus
        );
    }

    @Test
    public void _03_shop() throws Exception
    {
        logHead("shop");

        checkAllResources(
                getAccessToken(ClientType.TRUSTED, shop_username, shop_userpass),
                shop_userLevelResource_expectedStatus,
                shop_adminLevelResource_expectedStatus,
                shop_frontendLevelResource_expectedStatus
        );
    }

    @Test
    public void _04_anonymous() throws Exception
    {
        logHead("anonymous");

        checkAllResources(null,
                anonymous_userLevelResource_expectedStatus,
                anonymous_adminLevelResource_expectedStatus,
                anonymous_frontendLevelResource_expectedStatus
        );
    }

    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private static String login(Request req, int expires) throws Exception
    {
        log.info("get Token");
        log.info("-> Authorization: {}", req.header("Authorization"));
        log.info("-> " + req.url().toString());

        Map<String, Object> parsedBody = performAuthentification(req);

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((int) parsedBody.get("expires_in"),
                is(both(greaterThan(0)).and(lessThanOrEqualTo(expires))));

        return access_token;
    }

    private void checkAllResources(String access_token,
                                   int userLevelResource_expectedStatus,
                                   int adminLevelResource_expectedStatus,
                                   int frontendLevelResource_expectedStatus
    ) throws Exception
    {
        checkResource(GET_request(ALL_BEFORE_RESOURCE + publicLevelResource_public, access_token, null)
                , 200); // success for all levels

        checkResource(GET_request(ALL_BEFORE_RESOURCE + userLevelResource_cities, access_token, null)
                , userLevelResource_expectedStatus);

        checkResource(GET_request(ALL_BEFORE_RESOURCE + adminLevelResource_users, access_token, null)
                , adminLevelResource_expectedStatus);

        checkResource(POST_request(ALL_BEFORE_RESOURCE + frontendLevelResource_checkUserId, access_token, body_for_frontendLevelResource_checkUserId)
                , frontendLevelResource_expectedStatus);

        checkResource(POST_request(ALL_BEFORE_RESOURCE + frontendLevelResource_createUser, access_token, body_for_frontendLevelResource_createUser)
                , frontendLevelResource_expectedStatus);

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

    enum ClientType
    {
        TRUSTED,
        UNTRUSTED
    }

    private String getAccessToken(ClientType clientType, String username, String userpass) throws Exception
    {
        String clientname;
        String clientpass;
        int maxTokenExpirationTime;

        if (ClientType.TRUSTED == clientType)
        {
            clientname = trusted_clientname;
            clientpass = trusted_clientpass;
            maxTokenExpirationTime = trusted_maxTokenExpirationTime;
        }
        else
        {
            clientname = trusted_clientname;
            clientpass = trusted_clientpass;
            maxTokenExpirationTime = trusted_maxTokenExpirationTime;
        }

        Request req = authPostRequest(clientname, clientpass, username, userpass);
        String access_token = login(req, maxTokenExpirationTime);

        return access_token;
    }
}

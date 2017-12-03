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
import static ru.dwfe.authtion.GlobalVariables_FOR_TESTS.ALL_BEFORE_RESOURCE;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicRun
{
    @Test
    public void _01_user() throws Exception
    {
        logHead("user");

        Request req = getAuthRequest("Standard", "Login", "user", "passUser");
        String access_token = login(req, 864000);

        checkAllResources(access_token, 200, 200, 403);
    }

    @Test
    public void _02_admin() throws Exception
    {
        logHead("admin");

        Request req = getAuthRequest("ThirdParty", "Computer", "admin", "passAdmin");
        String access_token = login(req, 180);

        checkAllResources(access_token, 200, 200, 200);
    }

    @Test
    public void _03_anonymous() throws Exception
    {
        logHead("anonymous");

        checkAllResources(null, 200, 401, 401);
    }

    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private static String login(Request req, Integer expires) throws Exception
    {
        log.info("Login");

        Map<String, Object> parsedBody = httpPOST(req);

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((Integer) parsedBody.get("expires_in"),
                is(both(greaterThan(0)).and(lessThanOrEqualTo(expires))));

        return access_token;
    }

    private void checkAllResources(String access_token, int public_expectedStatus, int cities_expectedStatus, int users_expectedStatus) throws Exception
    {
        checkResource(getSimpleRequest(ALL_BEFORE_RESOURCE + "/public", access_token)
                , public_expectedStatus);

        checkResource(getSimpleRequest(ALL_BEFORE_RESOURCE + "/cities", access_token)
                , cities_expectedStatus);

        checkResource(getSimpleRequest(ALL_BEFORE_RESOURCE + "/users", access_token)
                , users_expectedStatus);
    }

    private static void checkResource(Request req, int expectedStatus) throws Exception
    {
        log.info(req.url().encodedPath());
        Map<String, Object> result = httpGET(req);
        assertEquals(expectedStatus, result.get("statusCode"));
    }

    private static Map<String, Object> httpPOST(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String respBody = response.body().string();
            log.info("token\n{}", respBody);
            assertEquals(200, response.code());

            return jsonParser.parseMap(respBody);
        }
    }

    private static Map<String, Object> httpGET(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String respBody = response.body().string();
            log.info(respBody);

            Map<String, Object> map = new HashMap<>();
            map.put("statusCode", response.code());
            if (req.url().pathSegments().get(1).equals("users"))
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

    private Request getSimpleRequest(String url, String access_token)
    {
        Request request;

        if (access_token == null)
            return new Request.Builder().url(url).build();
        else
            return new Request.Builder().url(url).addHeader("Authorization", "Bearer " + access_token).build();
    }

    private Request getAuthRequest(String clientname, String clientpass, String username, String userpass)
    {
        String url = String.format(ALL_BEFORE_RESOURCE
                        + "/oauth/token?grant_type=password&username=%s&password=%s",
                username, userpass);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(clientname, clientpass))
                .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), ""))
                .build();
    }

    private static final Logger log = LoggerFactory.getLogger(BasicRun.class);

    private void logHead(String who)
    {
        log.info("\n=============================="
                + "\n  {}"
                + "\n------------------------------", who);

    }
}

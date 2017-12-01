package ru.dwfe.authtion;

import okhttp3.*;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BasicRun
{
    @Test
    public void _01_user() throws Exception
    {
        System.out.printf("%n==============================");
        System.out.printf("%n  user");
        System.out.printf("%n------------------------------%n");

        Request req = getAuthRequest("Standard", "Login", "user", "passUser");
        String access_token = login(req, 864000);

        checkAllResources(access_token, 200, 200, 403);
    }

    @Test
    public void _02_admin() throws Exception
    {
        System.out.printf("%n==============================");
        System.out.printf("%n  admin");
        System.out.printf("%n------------------------------%n");

        Request req = getAuthRequest("ThirdParty", "Computer", "admin", "passAdmin");
        String access_token = login(req, 180);

        checkAllResources(access_token, 200, 200, 200);
    }

    @Test
    public void _03_anonymous() throws Exception
    {
        System.out.printf("%n==============================");
        System.out.printf("%n  anonymous");
        System.out.printf("%n------------------------------%n%n");

        checkAllResources(null, 200, 401, 401);
    }

    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private static String login(Request req, Integer expires) throws Exception
    {
        System.out.printf("%nlogin");

        Map<String, Object> parsedBody = httpPOST(req);

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((Integer) parsedBody.get("expires_in"),
                is(both(greaterThan(0)).and(lessThanOrEqualTo(expires))));

        return access_token;
    }

    private void checkAllResources(String access_token, int public_expectedStatus, int cities_expectedStatus, int users_expectedStatus) throws Exception
    {
        checkResource(getSimpleRequest("http://localhost:8080/public", access_token)
                , public_expectedStatus);

        checkResource(getSimpleRequest("http://localhost:8080/cities", access_token)
                , cities_expectedStatus);

        checkResource(getSimpleRequest("http://localhost:8080/users", access_token)
                , users_expectedStatus);
    }

    private static void checkResource(Request req, int expectedStatus) throws Exception
    {
        System.out.print(req.url().encodedPath());
        Map<String, Object> result = httpGET(req);
        assertEquals(expectedStatus, result.get("statusCode"));
    }

    private static Map<String, Object> httpPOST(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String respBody = response.body().string();
            System.out.printf("%n%s%n%n", respBody);
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
            System.out.printf("%n\t%s%n%n", respBody);

            Map<String, Object> map = new HashMap<>();
            map.put("statusCode", response.code());
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
        String url = String.format("http://localhost:8080/oauth/token?grant_type=password&username=%s&password=%s",
                username, userpass);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(clientname, clientpass))
                .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), ""))
                .build();
    }
}

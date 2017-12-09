package ru.dwfe.net.authtion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static ru.dwfe.net.authtion.util.Variables_Global.ALL_BEFORE_RESOURCE;
import static ru.dwfe.net.authtion.util.Variables_Global.PROTOCOL_HOST_PORT;
import static ru.dwfe.net.authtion.util.Variables_for_AuthorityTest.AUTHORITY_to_AUTHORITY_STATUS;
import static ru.dwfe.net.authtion.util.Variables_for_AuthorityTest.RESOURCE_AUTHORITY_reqDATA;

public class Util
{
    public static void setAccessToken(User user)
    {
        Client client = user.client;

        Request req = auth_POST_Request(client.clientname, client.clientpass, user.username, user.password);
        try
        {
            user.access_token = login(req, client.maxTokenExpirationTime, client.minTokenExpirationTime);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private static Request auth_POST_Request(String clientname, String clientpass, String username, String userpass)
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

    private static String login(Request req, int maxExpirationTime, int minExpirationTime) throws Exception
    {
        log.info("get Token");
        log.info("-> Authorization: {}", req.header("Authorization"));
        log.info("-> " + req.url().toString());

        String body = performAuthentification(req);
        Map<String, Object> map = parse(body);

        String access_token = (String) getValueFromResponse(map, "access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((int) getValueFromResponse(map, "expires_in"),
                is(both(greaterThan(minExpirationTime)).and(lessThanOrEqualTo(maxExpirationTime))));

        return access_token;
    }

    private static String performAuthentification(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String body = response.body().string();
            log.info("<- token\n{}\n", body);
            assertEquals(200, response.code());

            return body;
        }
    }

    public static Request GET_request(String url, String access_token, Map<String, Object> queries)
    {
        Request request;

        if (access_token == null)
            request = new Request.Builder().url(url).build();
        else
            request = new Request.Builder().url(url).addHeader("Authorization", "Bearer " + access_token).build();

        if (queries.size() > 0)
            for (Map.Entry<String, Object> next : queries.entrySet())
            {
                HttpUrl newUrl = request.url().newBuilder()
                        .addQueryParameter(next.getKey(), (String) next.getValue())
                        .build();
                request = request.newBuilder()
                        .url(newUrl)
                        .build();
            }
        return request;
    }

    public static Request POST_request(String url, String access_token, Map<String, Object> prorepty_value)
    {
        RequestBody body = getRequestBody(prorepty_value);

        Request.Builder req = new Request.Builder().url(url);

        if (access_token != null)
            req.addHeader("Authorization", "Bearer " + access_token);

        if (body == null)
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

        req.post(body);

        return req.build();
    }

    public static String performRequest(Request req, int expectedStatus)
    {
        log.info("-> " + req.url().encodedPath());

        String body = null;
        int actualStatusCode = -1;
        OkHttpClient client = new OkHttpClient();
        try (Response resp = client.newCall(req).execute())
        {
            body = resp.body().string();
            actualStatusCode = resp.code();
            log.info("<- {}\n", body);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        assertEquals(expectedStatus, actualStatusCode);
        return body;
    }

    public static String getResponseAfterPOSTrequest(String access_token, String resource, Map<String, Object> prorepty_value, int expectedStatus)
    {
        Request req = POST_request(ALL_BEFORE_RESOURCE + resource, access_token, prorepty_value);

        log.info("-> {}", prorepty_value.toString());

        return performRequest(req, expectedStatus);
    }

    public static String getResponseAfterGETrequest(String access_token, String resource, Map<String, Object> queries, int expectedStatus)
    {
        Request req = GET_request(ALL_BEFORE_RESOURCE + resource, access_token, queries);

        String query = queries.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        log.info("-> {}", query);

        return performRequest(req, expectedStatus);
    }

    public static void checkAllResources(User user) throws Exception
    {
        String access_token = user.access_token;

        RESOURCE_AUTHORITY_reqDATA.forEach((resource, next) -> {

            Map.Entry<AuthorityType, Map<RequestMethod, Map<String, Object>>> next1 = next.entrySet().iterator().next();
            Map.Entry<RequestMethod, Map<String, Object>> next2 = next1.getValue().entrySet().iterator().next();

            AuthorityType level = next1.getKey();
            RequestMethod method = next2.getKey();
            Map<String, Object> reqData = next2.getValue();

            Request req;
            if (GET == method)
                req = GET_request(ALL_BEFORE_RESOURCE + resource, access_token, reqData);
            else
                req = POST_request(ALL_BEFORE_RESOURCE + resource, access_token, reqData);

            Map<AuthorityType, Integer> statusList = AUTHORITY_to_AUTHORITY_STATUS.get(user.level);

            performRequest(req, statusList.get(level));
        });
    }

    public static void check_send_data(RequestMethod method, String resource, String access_token, List<Checker> checkers)
    {
        String body;
        for (Checker checker : checkers)
        {
            if (GET == method)
                body = getResponseAfterGETrequest(access_token, resource, checker.req, checker.expectedStatus);
            else
                body = getResponseAfterPOSTrequest(access_token, resource, checker.req, checker.expectedStatus);

            Map<String, Object> map = parse(body);

            if (checker.resultFieldName != null)
                assertEquals(checker.expectedResult, getValueFromResponse(map, checker.resultFieldName));

            if (checker.expectedError != null)
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

    private static Map<String, Object> parse(String body)
    {
        return jsonParser.parseMap(body);
    }

    private static Object getValueFromResponse(String body, String key)
    {
        return parse(body).get(key);
    }

    private static Object getValueFromResponse(Map<String, Object> map, String key)
    {
        return map.get(key);
    }

    private static Object getValueFromValueFromResponse(Map<String, Object> map, String fromKey, String key)
    {
        Map<String, Object> next = (Map<String, Object>) getValueFromResponse(map, fromKey);
        return next.get(key);
    }

    private static final Logger log = LoggerFactory.getLogger(Util.class);
    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();
}

package ru.dwfe.net.authtion.test_util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.dwfe.net.authtion.Global;
import ru.dwfe.net.authtion.util.Util;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static ru.dwfe.net.authtion.test_util.ResourceAccessingType.BAD_ACCESS_TOKEN;
import static ru.dwfe.net.authtion.test_util.SignInType.SignIn;
import static ru.dwfe.net.authtion.test_util.Variables_Global.ALL_BEFORE_RESOURCE;
import static ru.dwfe.net.authtion.test_util.Variables_for_AuthorityTest.AUTHORITY_to_AUTHORITY_STATUS;
import static ru.dwfe.net.authtion.test_util.Variables_for_AuthorityTest.AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;
import static ru.dwfe.net.authtion.test_util.Variables_for_AuthorityTest.RESOURCE_AUTHORITY_reqDATA;

public class UtilTest
{
    public static void setNewTokens(ConsumerTest consumerTest, int signInExpectedStatus, SignInType signInType)
    {
        Client client = consumerTest.client;
        Request req;

        if (SignIn == signInType)
            req = auth_signIn_POST_Request(client.clientname, client.clientpass, consumerTest.username, consumerTest.password);
        else
            req = auth_refresh_POST_Request(client.clientname, client.clientpass, consumerTest.refresh_token);

        String body = performSignIn(req, signInExpectedStatus);

        String access_token = "";
        String refresh_token = "";

        if (signInExpectedStatus == 200)
        {
            Map<String, Object> map = parse(body);

            access_token = (String) getValueFromResponse(map, "access_token");
            assertThat(access_token.length(), greaterThan(0));

            refresh_token = (String) getValueFromResponse(map, "refresh_token");
            assertThat(refresh_token.length(), greaterThan(0));

            assertThat((int) getValueFromResponse(map, "expires_in"),
                    is(both(greaterThan(client.minTokenExpirationTime)).and(lessThanOrEqualTo(client.maxTokenExpirationTime))));

            consumerTest.access_token = access_token;
            consumerTest.refresh_token = refresh_token;
        }
    }

    private static Request auth_signIn_POST_Request(String clientname, String clientpass, String username, String userpass)
    {
        String url = String.format(ALL_BEFORE_RESOURCE + Global.resource_signIn
                        + "?grant_type=password&username=%s&password=%s",
                username, userpass);

        log.info("Client's credentials - {}:{}", clientname, clientpass);
        log.info("Consumer's credentials - {}:{}", username, userpass);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(clientname, clientpass))
                .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), ""))
                .build();
    }

    private static Request auth_refresh_POST_Request(String clientname, String clientpass, String refresh_token)
    {
        String url = String.format(ALL_BEFORE_RESOURCE + Global.resource_signIn
                + "?grant_type=refresh_token&refresh_token=%s", refresh_token);

        log.info("Client's credentials - {}:{}", clientname, clientpass);
        log.info("Refresh token: {}", refresh_token);

        return new Request.Builder()
                .url(url)
                .addHeader("Authorization", Credentials.basic(clientname, clientpass))
                .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), ""))
                .build();
    }

    private static String performSignIn(Request req, int expectedStatus)
    {
        log.info("get Tokens");
        log.info("-> Authorization: {}", req.header("Authorization"));
        log.info("-> " + req.url().toString());

        String body = "";
        OkHttpClient client = getHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            body = response.body().string();
            log.info("<- tokens\n{}\n", body);
            assertEquals(expectedStatus, response.code());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        assertEquals(false, body.isEmpty());
        return body;
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
        OkHttpClient client = getHttpClient();
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

    private static OkHttpClient getHttpClient()
    {
        return new OkHttpClient
                .Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .build();
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

    public static void performResourceAccessing(String access_token, AuthorityLevel consumerLevel, ResourceAccessingType resourceAccessingType)
    {
        Map<AuthorityLevel, Map<AuthorityLevel, Integer>> statusMap;

        if (BAD_ACCESS_TOKEN == resourceAccessingType)
            statusMap = AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;
        else
            statusMap = AUTHORITY_to_AUTHORITY_STATUS;

        RESOURCE_AUTHORITY_reqDATA().forEach((resource, next) -> {

            Map.Entry<AuthorityLevel, Map<RequestMethod, Map<String, Object>>> next1 = next.entrySet().iterator().next();
            Map.Entry<RequestMethod, Map<String, Object>> next2 = next1.getValue().entrySet().iterator().next();

            AuthorityLevel level = next1.getKey();
            RequestMethod method = next2.getKey();
            Map<String, Object> reqData = next2.getValue();

            Request req;
            if (GET == method)
                req = GET_request(ALL_BEFORE_RESOURCE + resource, access_token, reqData);
            else
                req = POST_request(ALL_BEFORE_RESOURCE + resource, access_token, reqData);

            Map<AuthorityLevel, Integer> statusList = statusMap.get(consumerLevel);

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

            if (checker.expectedResponseMap != null)
            {
                log.info("[expected] = " + Util.getJSONfromObject(checker.expectedResponseMap));
                checker.responseHandler(map);
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

    public static Object getValueFromResponse(Map<String, Object> map, String key)
    {
        return map.get(key);
    }

    private static Object getValueFromValueFromResponse(Map<String, Object> map, String fromKey, String key)
    {
        Map<String, Object> next = (Map<String, Object>) getValueFromResponse(map, fromKey);
        return next.get(key);
    }

    private UtilTest()
    {
    }

    private static final Logger log = LoggerFactory.getLogger(UtilTest.class);
    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();
}

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
import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static ru.dwfe.net.authtion.test_util.ResourceAccessingType.BAD_ACCESS_TOKEN;
import static ru.dwfe.net.authtion.test_util.ResourceAccessingType.USUAL;
import static ru.dwfe.net.authtion.test_util.SignInType.Refresh;
import static ru.dwfe.net.authtion.test_util.Variables_Global.ALL_BEFORE_RESOURCE;
import static ru.dwfe.net.authtion.test_util.Variables_for_AuthorityTest.*;

public class UtilTest
{
  static void setNewTokens(ConsumerTest consumerTest, int signInExpectedStatus, SignInType signInType)
  {
    Client client = consumerTest.client;
    Request req;

    if (Refresh == signInType)
      req = auth_refresh_POST_Request(client.clientname, client.clientpass, consumerTest.refresh_token);
    else
      req = auth_signIn_POST_Request(client.clientname, client.clientpass, consumerTest.username, consumerTest.password);

    String body = performSignIn(req, signInExpectedStatus);

    String access_token;
    String refresh_token;

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

    String body = performSign(req, expectedStatus);

    log.info("<- tokens\n{}\n", body);
    return body;
  }

  private static String performSignOut(ConsumerTest consumerTest, int expectedStatus)
  {
    log.info("Sign Out");

    Request req = GET_request(ALL_BEFORE_RESOURCE + Global.resource_signOut, consumerTest.access_token, Map.of());

    log.info("-> Authorization: {}", req.header("Authorization"));
    log.info("-> " + req.url().toString());

    String body = performSign(req, expectedStatus);

    if (expectedStatus == 200)
    {
      Map<String, Object> map = parse(body);
      assertEquals(true, getValueFromResponse(map, "success"));
    }

    log.info("<- result of sign out\n{}\n", body);
    return body;
  }

  private static String performSign(Request req, int expectedStatus)
  {
    String body = "";
    OkHttpClient client = getHttpClient();
    try (Response response = client.newCall(req).execute())
    {
      assertNotEquals(null, response.body());
      body = response.body().string();
      assertEquals(expectedStatus, response.code());
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    assertFalse(body.isEmpty());
    return body;
  }

  private static Request GET_request(String url, String access_token, Map<String, Object> queries)
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

  private static Request POST_request(String url, String access_token, Map<String, Object> prorepty_value)
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

  private static String performRequest(Request req, int expectedStatus)
  {
    log.info("-> " + req.url().encodedPath());

    String body = null;
    int actualStatusCode = -1;
    OkHttpClient client = getHttpClient();
    try (Response resp = client.newCall(req).execute())
    {
      assertNotEquals(null, resp.body());
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

  private static String getResponseAfterPOSTrequest(String access_token, String resource, Map<String, Object> prorepty_value, int expectedStatus)
  {
    Request req = POST_request(ALL_BEFORE_RESOURCE + resource, access_token, prorepty_value);

    log.info("-> {}", prorepty_value.toString());

    return performRequest(req, expectedStatus);
  }

  private static String getResponseAfterGETrequest(String access_token, String resource, Map<String, Object> queries, int expectedStatus)
  {
    Request req = GET_request(ALL_BEFORE_RESOURCE + resource, access_token, queries);

    String query = queries.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
    log.info("-> {}", query);

    return performRequest(req, expectedStatus);
  }

  private static void performAuthTest_ResourceAccessing_ChangeToken(ConsumerTest consumerTest)
  {
    //1. Resource accessing
    performResourceAccessing(consumerTest.access_token, consumerTest.level, USUAL);

    //2. Change Token
    String old_access_token = consumerTest.access_token;
    String old_refresh_token = consumerTest.refresh_token;
    setNewTokens(consumerTest, 200, Refresh);
    assertNotEquals(old_access_token, consumerTest.access_token);
    assertEquals(old_refresh_token, consumerTest.refresh_token);

    //3. Resource accessing: old/new token
    performResourceAccessing(old_access_token, consumerTest.level, BAD_ACCESS_TOKEN);
    performResourceAccessing(consumerTest.access_token, consumerTest.level, USUAL);
  }

  public static void performFullAuthTest(ConsumerTest consumerTest)
  {
    //1,2,3
    performAuthTest_ResourceAccessing_ChangeToken(consumerTest);

    //4. Sign Out
    performSignOut(consumerTest, 200);

    //5. Resource accessing
    performResourceAccessing(consumerTest.access_token, consumerTest.level, BAD_ACCESS_TOKEN);

    //6. Change Token
    setNewTokens(consumerTest, 400, Refresh);
  }

  public static void performResourceAccessing(String access_token, AuthorityLevel consumerLevel, ResourceAccessingType resourceAccessingType)
  {
    Map<AuthorityLevel, Map<AuthorityLevel, Integer>> statusMap;

    if (BAD_ACCESS_TOKEN == resourceAccessingType)
      statusMap = AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;
    else
      statusMap = AUTHORITY_to_AUTHORITY_STATUS;

    RESOURCE_AUTHORITY_reqDATA().forEach((resource, next) -> {

      try
      {
        TimeUnit.MILLISECONDS.sleep(30);
      }
      catch (InterruptedException ignored)
      {
      }

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

      assertEquals(checker.expectedResult, getValueFromResponse(map, "success"));

      if (checker.expectedError != null)
      {
        assertEquals(checker.expectedError, getErrorFromResponse(map));
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

  public static Object getValueFromResponse(Map<String, Object> map, String key)
  {
    return map.get(key);
  }

  private static Object getErrorFromResponse(Map<String, Object> map)
  {
    List<String> next = (List<String>) getValueFromResponse(map, "error-codes");
    return next.get(0);
  }

  private UtilTest()
  {
  }

  private static final Logger log = LoggerFactory.getLogger(UtilTest.class);
  private static JsonParser jsonParser = JsonParserFactory.getJsonParser();
}

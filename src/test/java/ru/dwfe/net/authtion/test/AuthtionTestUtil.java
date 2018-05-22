package ru.dwfe.net.authtion.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;
import ru.dwfe.net.authtion.util.AuthtionUtil;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static ru.dwfe.net.authtion.test.AuthtionTestResourceAccessingType.BAD_ACCESS_TOKEN;
import static ru.dwfe.net.authtion.test.AuthtionTestResourceAccessingType.USUAL;
import static ru.dwfe.net.authtion.test.AuthtionTestSignInType.Refresh;
import static ru.dwfe.net.authtion.test.AuthtionTestVariablesForAuthTests.AUTHORITY_to_AUTHORITY_STATUS;
import static ru.dwfe.net.authtion.test.AuthtionTestVariablesForAuthTests.AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;

@Component
public class AuthtionTestUtil
{
  private final AuthtionConfigProperties authtionConfigProperties;
  private final AuthtionTestVariablesForAuthTests authtionTestVariablesForAuthTest;
  private final String ALL_BEFORE_RESOURCE;

  private AuthtionTestUtil(AuthtionConfigProperties authtionConfigProperties, AuthtionTestVariablesForAuthTests authtionTestVariablesForAuthTest)
  {
    this.authtionConfigProperties = authtionConfigProperties;
    this.authtionTestVariablesForAuthTest = authtionTestVariablesForAuthTest;
    this.ALL_BEFORE_RESOURCE = "http://localhost:8080" + authtionConfigProperties.getApi();
  }

  void setNewTokens(AuthtionTestConsumer testConsumer, int signInExpectedStatus, AuthtionTestSignInType signInType)
  {
    AuthtionTestClient client = testConsumer.client;
    Request req;

    if (Refresh == signInType)
      req = auth_refresh_POST_Request(client.clientname, client.clientpass, testConsumer.refresh_token);
    else
      req = auth_signIn_POST_Request(client.clientname, client.clientpass, testConsumer.username, testConsumer.password);

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

      testConsumer.access_token = access_token;
      testConsumer.refresh_token = refresh_token;
    }
  }

  private Request auth_signIn_POST_Request(String clientname, String clientpass, String username, String userpass)
  {
    String url = String.format(ALL_BEFORE_RESOURCE + authtionConfigProperties.getResource().getSignIn()
                    + "?grant_type=password&username=%s&password=%s",
            username, userpass);

    log.info("AuthtionTestClient's credentials - {}:{}", clientname, clientpass);
    log.info("AuthtionConsumer's credentials - {}:{}", username, userpass);

    return new Request.Builder()
            .url(url)
            .addHeader("Authorization", Credentials.basic(clientname, clientpass))
            .post(RequestBody.create(MediaType.parse("text/x-markdown; charset=utf-8"), ""))
            .build();
  }

  private Request auth_refresh_POST_Request(String clientname, String clientpass, String refresh_token)
  {
    String url = String.format(ALL_BEFORE_RESOURCE + authtionConfigProperties.getResource().getSignIn()
            + "?grant_type=refresh_token&refresh_token=%s", refresh_token);

    log.info("AuthtionTestClient's credentials - {}:{}", clientname, clientpass);
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

  private String performSignOut(AuthtionTestConsumer testConsumer, int expectedStatus)
  {
    log.info("Sign Out");

    Request req = GET_request(ALL_BEFORE_RESOURCE + authtionConfigProperties.getResource().getSignOut(), testConsumer.access_token, Map.of());

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

  private String getResponseAfterPOSTrequest(String access_token, String resource, Map<String, Object> prorepty_value, int expectedStatus)
  {
    Request req = POST_request(ALL_BEFORE_RESOURCE + resource, access_token, prorepty_value);

    log.info("-> {}", prorepty_value.toString());

    return performRequest(req, expectedStatus);
  }

  private String getResponseAfterGETrequest(String access_token, String resource, Map<String, Object> queries, int expectedStatus)
  {
    Request req = GET_request(ALL_BEFORE_RESOURCE + resource, access_token, queries);

    String query = queries.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining("&"));
    log.info("-> {}", query);

    return performRequest(req, expectedStatus);
  }

  private void performAuthTest_ResourceAccessing_ChangeToken(AuthtionTestConsumer testConsumer)
  {
    //1. Resource accessing
    performResourceAccessing(testConsumer.access_token, testConsumer.level, USUAL);

    //2. Change Token
    String old_access_token = testConsumer.access_token;
    String old_refresh_token = testConsumer.refresh_token;
    setNewTokens(testConsumer, 200, Refresh);
    assertNotEquals(old_access_token, testConsumer.access_token);
    assertEquals(old_refresh_token, testConsumer.refresh_token);

    //3. Resource accessing: old/new token
    performResourceAccessing(old_access_token, testConsumer.level, BAD_ACCESS_TOKEN);
    performResourceAccessing(testConsumer.access_token, testConsumer.level, USUAL);
  }

  public void performFullAuthTest(AuthtionTestConsumer testConsumer)
  {
    //1,2,3
    performAuthTest_ResourceAccessing_ChangeToken(testConsumer);

    //4. Sign Out
    performSignOut(testConsumer, 200);

    //5. Resource accessing
    performResourceAccessing(testConsumer.access_token, testConsumer.level, BAD_ACCESS_TOKEN);

    //6. Change Token
    setNewTokens(testConsumer, 400, Refresh);
  }

  public void performResourceAccessing(String access_token, AuthtionTestAuthorityLevel consumerLevel, AuthtionTestResourceAccessingType resourceAccessingType)
  {
    Map<AuthtionTestAuthorityLevel, Map<AuthtionTestAuthorityLevel, Integer>> statusMap;

    if (BAD_ACCESS_TOKEN == resourceAccessingType)
      statusMap = AUTHORITY_to_AUTHORITY_STATUS_BAD_ACCESS_TOKEN;
    else
      statusMap = AUTHORITY_to_AUTHORITY_STATUS;

    authtionTestVariablesForAuthTest.RESOURCE_AUTHORITY_reqDATA().forEach((resource, next) -> {

      try
      {
        TimeUnit.MILLISECONDS.sleep(70);
      }
      catch (InterruptedException ignored)
      {
      }

      Map.Entry<AuthtionTestAuthorityLevel, Map<RequestMethod, Map<String, Object>>> next1 = next.entrySet().iterator().next();
      Map.Entry<RequestMethod, Map<String, Object>> next2 = next1.getValue().entrySet().iterator().next();

      AuthtionTestAuthorityLevel level = next1.getKey();
      RequestMethod method = next2.getKey();
      Map<String, Object> reqData = next2.getValue();

      Request req;
      if (GET == method)
        req = GET_request(ALL_BEFORE_RESOURCE + resource, access_token, reqData);
      else
        req = POST_request(ALL_BEFORE_RESOURCE + resource, access_token, reqData);

      Map<AuthtionTestAuthorityLevel, Integer> statusList = statusMap.get(consumerLevel);

      performRequest(req, statusList.get(level));
    });
  }

  public void check_send_data(RequestMethod method, String resource, String access_token, List<AuthtionTestChecker> checkers)
  {
    String body;
    for (AuthtionTestChecker checker : checkers)
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
        log.info("[expected] = " + AuthtionUtil.getJSONfromObject(checker.expectedResponseMap));
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

  private static final Logger log = LoggerFactory.getLogger(AuthtionTestUtil.class);
  private static JsonParser jsonParser = JsonParserFactory.getJsonParser();
}

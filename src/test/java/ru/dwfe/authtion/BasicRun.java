package ru.dwfe.authtion;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class BasicRun
{
    @Test
    public void user() throws Exception
    {
        URI req = new URI(
                "http",
                "Standard:Login@localhost:8080",
                "/oauth/token",
                "username=user&password=passUser&grant_type=password",
                null);
        String access_token = login(req, 864000);
        BasicHeader authorization = new BasicHeader("Authorization", "Bearer " + access_token);

        req = new URI("http://localhost:8080/public");
        checkResource(req, List.of(authorization), 200); //success

        req = new URI("http://localhost:8080/cities");
        checkResource(req, List.of(authorization), 200); //success

        req = new URI("http://localhost:8080/users");
        checkResource(req, List.of(authorization), 403);  //access_denied
    }

    @Test
    public void admin() throws Exception
    {
        URI req = new URI(
                "http",
                "ThirdParty:Computer@localhost:8080",
                "/oauth/token",
                "username=admin&password=passAdmin&grant_type=password",
                null);
        String access_token = login(req, 180);
        BasicHeader authorization = new BasicHeader("Authorization", "Bearer " + access_token);

        req = new URI("http://localhost:8080/public");
        checkResource(req, List.of(authorization), 200); //success

        req = new URI("http://localhost:8080/cities");
        checkResource(req, List.of(authorization), 200); //success

        req = new URI("http://localhost:8080/users");
        checkResource(req, List.of(authorization), 200); //success
    }

    @Test
    public void anonymous() throws Exception
    {
        URI req = new URI("http://localhost:8080/public");
        checkResource(req, List.of(), 200); //success

        req = new URI("http://localhost:8080/cities");
        checkResource(req, List.of(), 401); //unauthorized

        req = new URI("http://localhost:8080/users");
        checkResource(req, List.of(), 401); //unauthorized
    }

    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private static String login(URI req, Integer expires) throws Exception
    {
        Map<String, Object> parsedBody = httpPOST(req);

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((Integer) parsedBody.get("expires_in"),
                is(both(greaterThan(0)).and(lessThanOrEqualTo(expires))));

        return access_token;
    }

    private static void checkResource(URI req, List<Header> authorization, int expectedStatus) throws Exception
    {
        Map<String, Object> result = httpGET(req, authorization);
        assertEquals(expectedStatus, result.get("statusCode"));
    }

    private static Map<String, Object> httpPOST(URI req) throws Exception
    {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(req);

        HttpResponse response = httpClient.execute(httpPost);
        String respBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.printf("%n%s%n%n", respBody);
        assertEquals(200, response.getStatusLine().getStatusCode());

        return jsonParser.parseMap(respBody);
    }

    private static Map<String, Object> httpGET(URI req, List<Header> headers) throws Exception
    {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(req);
        httpGet.setHeaders(headers.toArray(new Header[headers.size()]));

        HttpResponse response = httpClient.execute(httpGet);
        String respBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        System.out.printf("%n%s%n%n", respBody);

        Map<String, Object> map = new HashMap<>();
        map.put("statusCode", response.getStatusLine().getStatusCode());
        map.put("parsedBody", jsonParser.parseMap(respBody));

        return map;
    }
}

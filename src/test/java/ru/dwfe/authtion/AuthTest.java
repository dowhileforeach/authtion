package ru.dwfe.authtion;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.Test;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.net.URI;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class AuthTest
{
    private JsonParser jsonParser = JsonParserFactory.getJsonParser();

    private HttpResponse httpPOST(URI req) throws Exception
    {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(req);
        return httpClient.execute(httpPost);
    }

    private String login(URI req, Integer expires) throws Exception
    {
        HttpResponse response = httpPOST(req);
        String respBody = EntityUtils.toString(response.getEntity(), "UTF-8");
        Map<String, Object> parsedBody = jsonParser.parseMap(respBody);
        System.out.printf("%n%s%n%n", respBody);

        assertEquals(200, response.getStatusLine().getStatusCode());

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((Integer) parsedBody.get("expires_in"),
                is(both(greaterThan(0)).and(lessThanOrEqualTo(expires))));

        return access_token;
    }

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
    }
}

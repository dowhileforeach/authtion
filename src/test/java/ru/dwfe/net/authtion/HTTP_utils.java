package ru.dwfe.net.authtion;

import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;

import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static ru.dwfe.net.authtion.Variables_Global.*;

public class HTTP_utils
{
    public static String getAccessToken(ClientType clientType, String username, String userpass) throws Exception
    {
        String clientname = "";
        String clientpass = "";
        int maxTokenExpirationTime = -1;
        int minTokenExpirationTime = 0;

        if (ClientType.TRUSTED == clientType)
        {
            clientname = trusted_clientname;
            clientpass = trusted_clientpass;
            maxTokenExpirationTime = trusted_maxTokenExpirationTime;
            minTokenExpirationTime = trusted_minTokenExpirationTime;
        }
        else if (ClientType.UNTRUSTED == clientType)
        {
            clientname = untrusted_clientname;
            clientpass = untrusted_clientpass;
            maxTokenExpirationTime = untrusted_maxTokenExpirationTime;
            minTokenExpirationTime = untrusted_minTokenExpirationTime;
        }
        else if (ClientType.FRONTEND == clientType)
        {
            clientname = frontend_clientname;
            clientpass = frontend_clientpass;
            maxTokenExpirationTime = frontend_maxTokenExpirationTime;
            minTokenExpirationTime = frontend_minTokenExpirationTime;
        }

        Request req = auth_POST_Request(clientname, clientpass, username, userpass);
        String access_token = login(req, maxTokenExpirationTime, minTokenExpirationTime);

        return access_token;
    }

    private static String login(Request req, int maxExpirationTime, int minExpirationTime) throws Exception
    {
        log.info("get Token");
        log.info("-> Authorization: {}", req.header("Authorization"));
        log.info("-> " + req.url().toString());

        Map<String, Object> parsedBody = performAuthentification(req);

        String access_token = (String) parsedBody.get("access_token");
        assertThat(access_token.length(), greaterThan(0));

        assertThat((int) parsedBody.get("expires_in"),
                is(both(greaterThan(minExpirationTime)).and(lessThanOrEqualTo(maxExpirationTime))));

        return access_token;
    }

    private static Map<String, Object> performAuthentification(Request req) throws Exception
    {
        OkHttpClient client = new OkHttpClient();
        try (Response response = client.newCall(req).execute())
        {
            String respBody = response.body().string();
            log.info("<- token\n{}\n", respBody);
            assertEquals(200, response.code());

            return jsonParser.parseMap(respBody);
        }
    }

    public static Request GET_request(String url, String access_token, Map<String, String> queries)
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

    public static Request POST_request(String url, String access_token, RequestBody body)
    {
        Request.Builder req = new Request.Builder().url(url);

        if (access_token != null)
            req.addHeader("Authorization", "Bearer " + access_token);

        if (body == null)
            body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), "{}");

        req.post(body);

        return req.build();

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

    enum ClientType
    {
        TRUSTED,
        UNTRUSTED,
        FRONTEND
    }

    private static final Logger log = LoggerFactory.getLogger(HTTP_utils.class);
    private static JsonParser jsonParser = JsonParserFactory.getJsonParser();
}

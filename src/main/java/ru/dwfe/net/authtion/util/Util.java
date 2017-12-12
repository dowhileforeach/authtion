package ru.dwfe.net.authtion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.JsonParserFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Map;

public class Util
{
    public static Map<String, Object> parse(String body)
    {
        return JsonParserFactory.getJsonParser().parseMap(body);
    }

    public static Object getValueFromJSON(String body, String fieldName)
    {
        return JsonParserFactory.getJsonParser().parseMap(body).get(fieldName);
    }

    public static Object getValue(Map<String, Object> map, String key)
    {
        return map.get(key);
    }

    public static boolean isDefaultCheckOK(String value, String fieldName, Map<String, Object> details)
    {
        boolean result = false;

        if (value != null)
        {
            if (!value.isEmpty())
            {
                result = true;
            }
            else details.put(fieldName, "can't be empty");
        }
        else details.put(fieldName, "required field");

        return result;
    }

    public static boolean isDefaultCheckOK(String value)
    {
        return value != null && !value.isEmpty();
    }

    public static String getResponse(String resultFieldName, boolean responseResult, Map<String, Object> details)
    {
        if (details == null || details.size() == 0)

            return String.format("{" +
                    "\"%s\": %s" +
                    "}", resultFieldName, responseResult);

        else

            return String.format("{" +
                    "\"%s\": %s, " +
                    "\"details\": %s" +
                    "}", resultFieldName, responseResult, getJSONfromObject(details));
    }

    public static String getResponse(String resultFieldName, boolean responseResult, String details)
    {
        return String.format("{" +
                "\"%s\": %s, " +
                "\"details\": %s" +
                "}", resultFieldName, responseResult, details);
    }

    public static String getJSONfromObject(Object value)
    {
        String result = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try
        {
            result = mapper.writeValueAsString(value);
        }
        catch (JsonProcessingException e)
        {
            e.printStackTrace();
        }
        return result;
    }

    public static String getUniqStr(int requiredLength)
    {
        return new BigInteger(requiredLength * 5, new SecureRandom()).toString(36);
    }
}

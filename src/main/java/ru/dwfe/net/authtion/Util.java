package ru.dwfe.net.authtion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.JsonParserFactory;

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

    public static boolean isDefaultValueCheckOK(String value, String fieldName, Map<String, Object> details)
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

    public static String getResponse(String resultFieldName, boolean responseResult, Map<String, Object> details) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();

        if (details.size() == 0)

            return String.format("{" +
                    "\"%s\": %s" +
                    "}", resultFieldName, responseResult);

        else

            return String.format("{" +
                    "\"%s\": %s, " +
                    "\"details\": %s" +
                    "}", resultFieldName, responseResult, mapper.writeValueAsString(details));
    }
}

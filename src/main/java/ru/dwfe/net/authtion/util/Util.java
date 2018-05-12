package ru.dwfe.net.authtion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.json.JsonParserFactory;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class Util
{
  private static final DateTimeFormatter FORMATTER_DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

  public static boolean isDefaultCheckOK(String value, String fieldName, List<String> errorCodes)
  {
    boolean result = false;

    if (value != null)
    {
      if (!value.isEmpty())
      {
        result = true;
      }
      else errorCodes.add("empty-" + fieldName);
    }
    else errorCodes.add("missing-" + fieldName);

    return result;
  }

  public static boolean isDefaultCheckOK(String value)
  {
    return value != null && !value.isEmpty();
  }

  public static String getResponse(List<String> errorCodes)
  {
    if (errorCodes.size() == 0)
      return "{\"success\": true}";
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  public static String getResponse(List<String> errorCodes, String data)
  {
    if (errorCodes.size() == 0)
      return getResponseSuccessWithData(data);
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  public static String getResponse(List<String> errorCodes, Map<String, Object> data)
  {
    if (errorCodes.size() == 0)
      return getResponseSuccessWithData(getJSONfromObject(data));
    else
      return getResponseWithErrorCodes(errorCodes);
  }

  private static String getResponseSuccessWithData(String data)
  {
    return String.format("{\"success\": true, \"data\": %s}", data);
  }

  private static String getResponseWithErrorCodes(List<String> errorCodes)
  {
    return String.format("{\"success\": false, \"error-codes\": %s}", getJSONfromObject(errorCodes));
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

  public static String getUniqStrBase36(int requiredLength)
  {
    return new BigInteger(requiredLength * 5, new SecureRandom()).toString(36);
  }

  public static String getUniqStrBase64(int requiredLength)
  {
    // (requiredLength + 2) and new String(..., 1,...)
    // becouse first letter repeated:
    //    AfhFTjpSSg==
    //    AfhFTjpSSg==
    //    Aj3ibDty2g==
    //    AqXQoW3d1w==
    //    A42HUbmWPw==
    //    At0DXvTA/Q==
    //
    // new String(..., ..., requiredLength)
    // becouse encoder adds postfix "=="
    //
    // SUMMARY:
    // X requiredLength ==

    byte[] bytes = new BigInteger((requiredLength + 2) * 5, new SecureRandom()).toByteArray();
    return new String(Base64.getEncoder().encode(bytes), 1, requiredLength);
  }

  public static String formatDateTime(LocalDateTime localDateTime)
  {
    return localDateTime.format(FORMATTER_DATE_TIME);
  }

  private Util()
  {
  }
}

package ru.dwfe.net.authtion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@PropertySource("classpath:application.properties")
public class AuthtionUtil
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
    String result = new String(Base64.getEncoder().encode(bytes), 1, requiredLength);
    return result.replaceAll("[^a-zA-Z0-9]", ""); // becouse oauth2 incorrect work with some symbols, e.g.: "+fAhjktzuw", "6k+xfc6ZRw"
  }

  public static String formatDateTime(LocalDateTime localDateTime)
  {
    // ISO dates can be written with added hours, minutes, and seconds (YYYY-MM-DDTHH:MM:SSZ):
    //   "2015-03-25T12:00:00Z"
    // Date and time is separated with a capital T.
    // UTC time is defined with a capital letter Z.
    //
    // https://docs.oracle.com/javase/10/docs/api/java/time/format/DateTimeFormatter.html#predefined
    // I can't use ISO_INSTANT formmatter becouse LocalDateTime not contains info about time zone.
    return localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "Z";

    // If you want to modify the time relative to UTC, remove the Z and add +HH:MM or -HH:MM instead:
    //   "2015-03-25T12:00:00-06:30"
    // But I strongly do not recommend doing this, otherwise you need to consider changing the time zone
    // in other places of this project: https://github.com/dowhileforeach/authtion#date-and-time
    // and don't forget about the time zone of MySQL
  }

  public String getGoogleCaptchaSecretKey()
  {
    String secretKey = env.getProperty("dwfe.authtion.google.captcha.secret-key");
    return secretKey == null ? "" : secretKey;
  }

  private int getTimeoutForDuplicateRequest()
  {
    String sendIntervalStr = env.getProperty("dwfe.authtion.scheduled.task.mailing.send-interval");
    String maxAttemptsStr = env.getProperty("dwfe.authtion.scheduled.task.mailing.max-attempts-to-send-if-error");

    int sendInterval = sendIntervalStr == null ? 30000 : Integer.parseInt(sendIntervalStr);
    int maxAttempts = maxAttemptsStr == null ? 3 : Integer.parseInt(maxAttemptsStr);

    return sendInterval * maxAttempts;
  }

  public boolean isAllowedNewRequestForMailing(int type, String email, List<String> errorCodes)
  {
    boolean result = true;

    Optional<AuthtionMailing> lastPending = mailingRepository.findLastNotEmptyData(type, email);
    if (lastPending.isPresent())
    {
      LocalDateTime whenNewIsAllowed = lastPending.get()
              .getCreatedOn()
              .plus(getTimeoutForDuplicateRequest(), ChronoUnit.MILLIS);
      if (whenNewIsAllowed.isAfter(LocalDateTime.now()))
      {
        result = false;
        errorCodes.add("delay-between-duplicate-requests");
      }
    }
    return result;
  }

  private final Environment env;
  private final AuthtionMailingRepository mailingRepository;

  @Autowired
  private AuthtionUtil(Environment env, AuthtionMailingRepository mailingRepository)
  {
    this.env = env;
    this.mailingRepository = mailingRepository;
  }
}

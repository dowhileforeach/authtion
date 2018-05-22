package ru.dwfe.net.authtion.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.AuthtionUser;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@PropertySource("classpath:application.properties")
public class AuthtionUtil
{
  private final AuthtionMailingRepository mailingRepository;
  private final AuthtionConfigProperties authtionConfigProperties;

  @Autowired
  private AuthtionUtil(AuthtionMailingRepository mailingRepository, AuthtionConfigProperties authtionConfigProperties)
  {
    this.mailingRepository = mailingRepository;
    this.authtionConfigProperties = authtionConfigProperties;
  }

  public static String getNickNameFromEmail(String email)
  {
    return email.substring(0, email.indexOf('@'));
  }

  public static String prepareStringField(String field, int maxLength)
  {
    String result;

    if (field == null)
      result = "";
    else if (field.length() > maxLength)
      result = field.substring(0, maxLength - 1);
    else
      result = field;

    return result;
  }

  public static String prepareAccountInfo(AuthtionConsumer consumer, AuthtionUser user, boolean onPublic)
  {
    ArrayList<String> list = new ArrayList<>();
    LocalDateTime updatedOn = consumer.getUpdatedOn().isBefore(user.getUpdatedOn()) ? consumer.getUpdatedOn() : user.getUpdatedOn();

    list.add("\"id\": " + consumer.getId());

    if (onPublic)
    {
      if (!consumer.isEmailNonPublic())
        list.add("\"email\": \"" + consumer.getEmail() + "\"");
      if (!user.isNickNameNonPublic())
        list.add("\"nickName\": \"" + user.getNickName() + "\"");
      if (!user.isFirstNameNonPublic())
        list.add("\"firstName\": \"" + user.getFirstName() + "\"");
      if (!user.isMiddleNameNonPublic())
        list.add("\"middleName\": \"" + user.getMiddleName() + "\"");
      if (!user.isLastNameNonPublic())
        list.add("\"lastName\": \"" + user.getLastName() + "\"");
      if (!user.isGenderNonPublic())
        list.add("\"gender\": \"" + user.getGender() + "\"");
      if (!user.isDateOfBirthNonPublic())
        list.add("\"dateOfBirth\": \"" + user.getDateOfBirth() + "\"");
    }
    else
    {
      list.add("\"createdOn\": " + "\"" + AuthtionUtil.formatDateTime(consumer.getCreatedOn()) + "\"");
      list.add("\"updatedOn\": " + "\"" + formatDateTime(updatedOn) + "\"");
      list.add("\"authorities\": " + consumer.getAuthorities());
      list.add("\"email\": \"" + consumer.getEmail() + "\"");
      list.add("\"emailConfirmed\": " + consumer.isEmailConfirmed());
      list.add("\"emailNonPublic\": " + consumer.isEmailNonPublic());
      list.add("\"nickName\": \"" + user.getNickName() + "\"");
      list.add("\"nickNameNonPublic\": \"" + user.isNickNameNonPublic() + "\"");
      list.add("\"firstName\": \"" + user.getFirstName() + "\"");
      list.add("\"firstNameNonPublic\": \"" + user.isFirstNameNonPublic() + "\"");
      list.add("\"middleName\": \"" + user.getMiddleName() + "\"");
      list.add("\"middleNameNonPublic\": \"" + user.isMiddleNameNonPublic() + "\"");
      list.add("\"lastName\": \"" + user.getLastName() + "\"");
      list.add("\"lastNameNonPublic\": \"" + user.isLastNameNonPublic() + "\"");
      list.add("\"gender\": \"" + user.getGender() + "\"");
      list.add("\"genderNonPublic\": \"" + user.isGenderNonPublic() + "\"");
      list.add("\"dateOfBirth\": \"" + user.getDateOfBirth() + "\"");
      list.add("\"dateOfBirthNonPublic\": \"" + user.isDateOfBirthNonPublic() + "\"");
    }

    return "{" + list.stream().collect(Collectors.joining(",")) + "}";
  }


  public static Map<String, Object> parse(String body)
  {
    return JsonParserFactory.getJsonParser().parseMap(body);
  }

  public static Object getValueFromJSON(String body, String fieldName)
  {
    return JsonParserFactory.getJsonParser().parseMap(body).get(fieldName);
  }

  public static String getValue(Map<String, Object> map, String key)
  {
    return (String) map.get(key);
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

  public static String formatMilliseconds(long millis)
  { // ==https://stackoverflow.com/questions/625433/how-to-convert-milliseconds-to-x-mins-x-seconds-in-java#625624
    return String.format("%02d min, %02d sec",
            TimeUnit.MILLISECONDS.toMinutes(millis),
            TimeUnit.MILLISECONDS.toSeconds(millis) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis))
    );
  }


  public String getGoogleCaptchaSecretKey()
  {
    return authtionConfigProperties.getGoogleCaptcha().getSecretKey();
  }

  public String getGoogleCaptchaSiteVerifyUrl()
  {
    return authtionConfigProperties.getGoogleCaptcha().getSiteVerifyUrl();
  }


  public boolean isAllowedNewRequestForMailing(int type, String email, List<String> errorCodes)
  {
    boolean result = true;

    Optional<AuthtionMailing> lastPending = mailingRepository.findLastNotEmptyData(type, email);
    if (lastPending.isPresent())
    {
      LocalDateTime whenNewIsAllowed = lastPending.get()
              .getCreatedOn()
              .plus(authtionConfigProperties.getScheduledTaskMailing().getTimeoutForDuplicateRequest(),
                      ChronoUnit.MILLIS);

      if (whenNewIsAllowed.isAfter(LocalDateTime.now()))
      {
        result = false;
        errorCodes.add("delay-between-duplicate-requests");
      }
    }
    return result;
  }
}

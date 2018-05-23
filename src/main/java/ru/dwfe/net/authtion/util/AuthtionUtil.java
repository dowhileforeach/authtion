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
import java.time.LocalDate;
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


  //
  // CONTROLLER
  //

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

  public static String prepareAccountInfo(AuthtionConsumer consumer, AuthtionUser user, boolean onPublic)
  {
    ArrayList<String> list = new ArrayList<>();
    LocalDateTime updatedOn = consumer.getUpdatedOn().isBefore(user.getUpdatedOn()) ? consumer.getUpdatedOn() : user.getUpdatedOn();

    String id = "\"id\": " + consumer.getId();
    String email = "\"email\": \"" + consumer.getEmail() + "\"";
    String nickName = "\"nickName\": \"" + user.getNickName() + "\"";
    String firstName = "\"firstName\": \"" + user.getFirstName() + "\"";
    String middleName = "\"middleName\": \"" + user.getMiddleName() + "\"";
    String lastName = "\"lastName\": \"" + user.getLastName() + "\"";
    String gender = "\"gender\": " + user.getGender();
    String dateOfBirth = "\"dateOfBirth\": " + user.getDateOfBirth();

    list.add(id);
    if (onPublic)
    {
      if (!consumer.isEmailNonPublic())
        list.add(email);
      if (!user.isNickNameNonPublic())
        list.add(nickName);
      if (!user.isFirstNameNonPublic())
        list.add(firstName);
      if (!user.isMiddleNameNonPublic())
        list.add(middleName);
      if (!user.isLastNameNonPublic())
        list.add(lastName);
      if (!user.isGenderNonPublic())
        list.add(gender);
      if (!user.isDateOfBirthNonPublic())
        list.add(dateOfBirth);
    }
    else
    {
      list.add("\"createdOn\": " + "\"" + AuthtionUtil.formatDateTime(consumer.getCreatedOn()) + "\"");
      list.add("\"updatedOn\": " + "\"" + formatDateTime(updatedOn) + "\"");
      list.add("\"authorities\": " + consumer.getAuthorities());
      list.add(email);
      list.add("\"emailConfirmed\": " + consumer.isEmailConfirmed());
      list.add("\"emailNonPublic\": " + consumer.isEmailNonPublic());
      list.add(nickName);
      list.add("\"nickNameNonPublic\": \"" + user.isNickNameNonPublic() + "\"");
      list.add(firstName);
      list.add("\"firstNameNonPublic\": \"" + user.isFirstNameNonPublic() + "\"");
      list.add(middleName);
      list.add("\"middleNameNonPublic\": \"" + user.isMiddleNameNonPublic() + "\"");
      list.add(lastName);
      list.add("\"lastNameNonPublic\": \"" + user.isLastNameNonPublic() + "\"");
      list.add(gender);
      list.add("\"genderNonPublic\": \"" + user.isGenderNonPublic() + "\"");
      list.add(dateOfBirth);
      list.add("\"dateOfBirthNonPublic\": \"" + user.isDateOfBirthNonPublic() + "\"");
    }

    return "{" + list.stream().collect(Collectors.joining(",")) + "}";
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

  public static class ReqCreateAccount
  {
    public String email;
    public String password;

    public String nickName;
    public String firstName;
    public String middleName;
    public String lastName;

    public Integer gender;
    public LocalDate dateOfBirth;

    public String getEmail()
    {
      return email;
    }

    public void setEmail(String email)
    {
      this.email = email;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }

    public String getNickName()
    {
      return nickName;
    }

    public void setNickName(String nickName)
    {
      this.nickName = nickName;
    }

    public String getFirstName()
    {
      return firstName;
    }

    public void setFirstName(String firstName)
    {
      this.firstName = firstName;
    }

    public String getMiddleName()
    {
      return middleName;
    }

    public void setMiddleName(String middleName)
    {
      this.middleName = middleName;
    }

    public String getLastName()
    {
      return lastName;
    }

    public void setLastName(String lastName)
    {
      this.lastName = lastName;
    }

    public Integer getGender()
    {
      return gender;
    }

    public void setGender(Integer gender)
    {
      this.gender = gender;
    }

    public LocalDate getDateOfBirth()
    {
      return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth)
    {
      this.dateOfBirth = dateOfBirth;
    }
  }

  public static class ReqCheckEmail
  {
    public String email;

    public String getEmail()
    {
      return email;
    }

    public void setEmail(String email)
    {
      this.email = email;
    }
  }

  public static class ReqCheckPass
  {
    public String password;

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }
  }

  public static class ReqGoogleCaptchaValidate
  {
    public String googleResponse;

    public String getGoogleResponse()
    {
      return googleResponse;
    }

    public void setGoogleResponse(String googleResponse)
    {
      this.googleResponse = googleResponse;
    }
  }

  public static class ReqUpdateAccount
  {
    public String email;
    public Boolean emailNonPublic;

    public String nickName;
    public Boolean nickNameNonPublic;

    public String firstName;
    public Boolean firstNameNonPublic;

    public String middleName;
    public Boolean middleNameNonPublic;

    public String lastName;
    public Boolean lastNameNonPublic;

    public Integer gender;
    public Boolean genderNonPublic;

    public LocalDate dateOfBirth;
    public Boolean dateOfBirthNonPublic;

    public String getEmail()
    {
      return email;
    }

    public void setEmail(String email)
    {
      this.email = email;
    }

    public Boolean getEmailNonPublic()
    {
      return emailNonPublic;
    }

    public void setEmailNonPublic(Boolean emailNonPublic)
    {
      this.emailNonPublic = emailNonPublic;
    }

    public String getNickName()
    {
      return nickName;
    }

    public void setNickName(String nickName)
    {
      this.nickName = nickName;
    }

    public Boolean getNickNameNonPublic()
    {
      return nickNameNonPublic;
    }

    public void setNickNameNonPublic(Boolean nickNameNonPublic)
    {
      this.nickNameNonPublic = nickNameNonPublic;
    }

    public String getFirstName()
    {
      return firstName;
    }

    public void setFirstName(String firstName)
    {
      this.firstName = firstName;
    }

    public Boolean getFirstNameNonPublic()
    {
      return firstNameNonPublic;
    }

    public void setFirstNameNonPublic(Boolean firstNameNonPublic)
    {
      this.firstNameNonPublic = firstNameNonPublic;
    }

    public String getMiddleName()
    {
      return middleName;
    }

    public void setMiddleName(String middleName)
    {
      this.middleName = middleName;
    }

    public Boolean getMiddleNameNonPublic()
    {
      return middleNameNonPublic;
    }

    public void setMiddleNameNonPublic(Boolean middleNameNonPublic)
    {
      this.middleNameNonPublic = middleNameNonPublic;
    }

    public String getLastName()
    {
      return lastName;
    }

    public void setLastName(String lastName)
    {
      this.lastName = lastName;
    }

    public Boolean getLastNameNonPublic()
    {
      return lastNameNonPublic;
    }

    public void setLastNameNonPublic(Boolean lastNameNonPublic)
    {
      this.lastNameNonPublic = lastNameNonPublic;
    }

    public Integer getGender()
    {
      return gender;
    }

    public void setGender(Integer gender)
    {
      this.gender = gender;
    }

    public Boolean getGenderNonPublic()
    {
      return genderNonPublic;
    }

    public void setGenderNonPublic(Boolean genderNonPublic)
    {
      this.genderNonPublic = genderNonPublic;
    }

    public LocalDate getDateOfBirth()
    {
      return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth)
    {
      this.dateOfBirth = dateOfBirth;
    }

    public Boolean getDateOfBirthNonPublic()
    {
      return dateOfBirthNonPublic;
    }

    public void setDateOfBirthNonPublic(Boolean dateOfBirthNonPublic)
    {
      this.dateOfBirthNonPublic = dateOfBirthNonPublic;
    }
  }

  public static class ReqChangePass
  {
    public String oldpass;
    public String newpass;

    public String oldpassField = "oldpass";
    public String newpassField = "newpass";

    public String getOldpass()
    {
      return oldpass;
    }

    public void setOldpass(String oldpass)
    {
      this.oldpass = oldpass;
    }

    public String getNewpass()
    {
      return newpass;
    }

    public void setNewpass(String newpass)
    {
      this.newpass = newpass;
    }
  }

  public static class ReqRestorePass
  {
    public String email;
    public String key;
    public String newpass;

    public String keyFieldFullName = "confirm-key";
    public String newpassField = "newpass";

    public String getEmail()
    {
      return email;
    }

    public void setEmail(String email)
    {
      this.email = email;
    }

    public String getKey()
    {
      return key;
    }

    public void setKey(String key)
    {
      this.key = key;
    }

    public String getNewpass()
    {
      return newpass;
    }

    public void setNewpass(String newpass)
    {
      this.newpass = newpass;
    }
  }

  //
  // JSON
  //

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


  //
  // COMMON
  //

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
}

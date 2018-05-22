package ru.dwfe.net.authtion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.AuthtionUser;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.dao.repository.AuthtionUserRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.util.AuthtionUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static ru.dwfe.net.authtion.dao.AuthtionConsumer.*;
import static ru.dwfe.net.authtion.dao.AuthtionUser.prepareNewUser;
import static ru.dwfe.net.authtion.util.AuthtionUtil.*;

@RestController
@RequestMapping(path = "#{authtionConfigProperties.api}", produces = "application/json; charset=utf-8")
public class AuthtionControllerV1
{
  private final AuthtionConsumerService consumerService;
  private final AuthtionUserRepository userRepository;
  private final AuthtionMailingRepository mailingRepository;
  private final ConsumerTokenServices tokenServices;
  private final RestTemplate restTemplate;
  private final AuthtionUtil authtionUtil;

  @Autowired
  public AuthtionControllerV1(AuthtionConsumerService consumerService, AuthtionUserRepository userRepository, AuthtionMailingRepository mailingRepository, ConsumerTokenServices tokenServices, RestTemplate restTemplate, AuthtionUtil authtionUtil)
  {
    this.consumerService = consumerService;
    this.userRepository = userRepository;
    this.mailingRepository = mailingRepository;
    this.tokenServices = tokenServices;
    this.restTemplate = restTemplate;
    this.authtionUtil = authtionUtil;
  }


  //
  // Consumer & User: Create, Read, Update
  //

  @PostMapping("#{authtionConfigProperties.resource.checkEmail}")
  public String checkConsumerEmail(@RequestBody ReqCheckEmail req)
  {
    List<String> errorCodes = new ArrayList<>();
    canUseEmail(req.email, consumerService, errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.checkPass}")
  public String checkConsumerPass(@RequestBody ReqCheckPass req)
  {
    List<String> errorCodes = new ArrayList<>();
    canUsePassword(req.password, "password", errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.googleCaptchaValidate}")
  public String googleCaptchaValidate(@RequestBody ReqGoogleCaptchaValidate req)
  {
    List<String> errorCodes = new ArrayList<>();

    if (isDefaultCheckOK(req.googleResponse, "google-response", errorCodes))
    {
      String url = String.format(authtionUtil.getGoogleCaptchaSiteVerifyUrl(),
              authtionUtil.getGoogleCaptchaSecretKey(), req.googleResponse);

      FutureTask<ResponseEntity<String>> exchange =
              new FutureTask<>(() -> restTemplate.exchange(url, HttpMethod.POST, null, String.class));
      new Thread(exchange).start();

      try
      {
        ResponseEntity<String> response = exchange.get(7, TimeUnit.SECONDS);
        if (response.getStatusCodeValue() == 200)
        {
          Boolean success = (Boolean) getValueFromJSON(response.getBody(), "success");
          if (!success)
            errorCodes.add("google-captcha-detected-robot");
        }
        else
          errorCodes.add("error-google-captcha-gateway");
      }
      catch (Throwable e)
      {
        errorCodes.add("timeout-google-captcha-gateway");
      }
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.createAccount}")
  public String createAccount(@RequestBody ReqCreateAccount req)
  {
    List<String> errorCodes = new ArrayList<>();
    String data = "";

    String password = req.password;
    String automaticallyGeneratedPassword = "";

    if (canUseEmail(req.email, consumerService, errorCodes))
      if (password == null)
      { // if password wasn't passed
        automaticallyGeneratedPassword = getUniqStrBase64(15);
        password = automaticallyGeneratedPassword;
      }
      else // if password was passed
        canUsePassword(password, "password", errorCodes);

    if (errorCodes.size() == 0)
    {
      // consumer
      AuthtionConsumer consumer = new AuthtionConsumer();
      consumer.setEmail(req.email);
      consumer.setEmailConfirmed(!automaticallyGeneratedPassword.isEmpty());
      consumer.setNewPassword(password);
      prepareNewConsumer(consumer);
      consumerService.save(consumer);
      consumer = consumerService.findByEmail(consumer.getEmail()).get();

      // user
      AuthtionUser user = new AuthtionUser();
      user.setNickName(req.nickName);
      user.setFirstName(req.firstName);
      user.setLastName(req.lastName);
      prepareNewUser(user, consumer);
      userRepository.save(user);
      user = userRepository.findById(consumer.getId()).get();

      // mailing
      mailingRepository.save(automaticallyGeneratedPassword.isEmpty()
              ? AuthtionMailing.of(1, consumer.getEmail())
              : AuthtionMailing.of(2, consumer.getEmail(), automaticallyGeneratedPassword));

      // response data
      data = prepareAccountInfo(consumer, user, false);
    }
    return getResponse(errorCodes, data);
  }

  @GetMapping("#{authtionConfigProperties.resource.getAccount}")
  @PreAuthorize("hasAuthority('USER')")
  public String getAccount(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();

    AuthtionConsumer consumer = consumerService.findById(id).get();
    AuthtionUser user = userRepository.findById(id).get();
    String data = prepareAccountInfo(consumer, user, false);

    return getResponse(errorCodes, data);
  }

  @PostMapping("#{authtionConfigProperties.resource.updateAccount}")
  @PreAuthorize("hasAuthority('USER')")
  public String updateAccount(@RequestBody ReqUpdateAccount req, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();
    AuthtionConsumer consumer = consumerService.findById(id).get();
    AuthtionUser user = userRepository.findById(id).get();
    boolean consumerWasModified = false;
    boolean userWasModified = false;

    if (req.email != null && !consumer.getEmail().equals(req.email))
    {
      consumer.setEmail(req.email);
      consumerWasModified = true;
    }

    if (req.emailNonPublic != null && !req.emailNonPublic.equals(String.valueOf(consumer.isEmailNonPublic())))
    {
      consumer.setEmailNonPublic(!"false".equals(req.emailNonPublic));
      consumerWasModified = true;
    }

    if (req.nickName != null && !user.getNickName().equals(req.nickName))
    {
      user.setNickName(req.nickName);
      userWasModified = true;
    }

    if (req.nickNameNonPublic != null && !req.nickNameNonPublic.equals(String.valueOf(user.isNickNameNonPublic())))
    {
      user.setNickNameNonPublic(!"false".equals(req.nickNameNonPublic));
      userWasModified = true;
    }

    if (req.firstName != null && !user.getFirstName().equals(req.firstName))
    {
      user.setFirstName(req.firstName);
      userWasModified = true;
    }

    if (req.firstNameNonPublic != null && !req.firstNameNonPublic.equals(String.valueOf(user.isFirstNameNonPublic())))
    {
      user.setFirstNameNonPublic(!"false".equals(req.firstNameNonPublic));
      userWasModified = true;
    }

    if (req.middleName != null && !user.getMiddleName().equals(req.middleName))
    {
      user.setMiddleName(req.middleName);
      userWasModified = true;
    }

    if (req.middleNameNonPublic != null && !req.middleNameNonPublic.equals(String.valueOf(user.isMiddleNameNonPublic())))
    {
      user.setMiddleNameNonPublic(!"false".equals(req.middleNameNonPublic));
      userWasModified = true;
    }

    if (req.lastName != null && !user.getLastName().equals(req.lastName))
    {
      user.setLastName(req.lastName);
      userWasModified = true;
    }

    if (req.lastNameNonPublic != null && !req.lastNameNonPublic.equals(String.valueOf(user.isLastNameNonPublic())))
    {
      user.setLastNameNonPublic(!"false".equals(req.lastNameNonPublic));
      userWasModified = true;
    }

    if (req.gender != null && !req.gender.equals(String.valueOf(user.getGender())))
    {
      user.setGender(Integer.parseInt(req.gender));
      userWasModified = true;
    }

    if (req.genderNonPublic != null && !req.genderNonPublic.equals(String.valueOf(user.isGenderNonPublic())))
    {
      user.setGenderNonPublic(!"false".equals(req.genderNonPublic));
      userWasModified = true;
    }

    if (req.dateOfBirth != null && !user.getDateOfBirth().equals(req.dateOfBirth))
    {
      user.setDateOfBirth(req.dateOfBirth);
      userWasModified = true;
    }

    if (req.dateOfBirthNonPublic != null && !req.dateOfBirthNonPublic.equals(String.valueOf(user.isDateOfBirthNonPublic())))
    {
      user.setDateOfBirthNonPublic(!"false".equals(req.dateOfBirthNonPublic));
      userWasModified = true;
    }

    if (consumerWasModified)
    {
      consumerService.save(consumer);
      consumer = consumerService.findById(id).get();
    }

    if (userWasModified)
    {
      userRepository.save(user);
      user = userRepository.findById(id).get();
    }

    String data = prepareAccountInfo(consumer, user, false);

    return getResponse(errorCodes, data);
  }

  @GetMapping("#{authtionConfigProperties.resource.publicAccount}" + "/{id}")
  public String publicAccount(@PathVariable Long id)
  {
    List<String> errorCodes = new ArrayList<>();
    String data = "";

    Optional<AuthtionConsumer> consumerById = consumerService.findById(id);
    if (consumerById.isPresent())
    {
      AuthtionConsumer consumer = consumerById.get();
      AuthtionUser user = userRepository.findById(id).get();
      data = prepareAccountInfo(consumer, user, false);
    }
    else errorCodes.add("id-not-exist");

    return getResponse(errorCodes, data);
  }

  @GetMapping("#{authtionConfigProperties.resource.reqConfirmEmail}")
  @PreAuthorize("hasAuthority('USER')")
  public String reqConfirmEmail(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();

    Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();
    AuthtionConsumer consumer = consumerService.findById(id).get();
    String email = consumer.getEmail();
    int type = 3;

    if (consumer.isEmailConfirmed())
    {
      errorCodes.add("email-is-already-confirmed");
    }
    else if (authtionUtil.isAllowedNewRequestForMailing(type, email, errorCodes))
    {
      mailingRepository.save(AuthtionMailing.of(type, email, getUniqStrBase36(40)));
    }

    return getResponse(errorCodes);
  }

  @GetMapping("#{authtionConfigProperties.resource.confirmEmail}")
  public String confirmEmail(@RequestParam(required = false) String key)
  {
    String fieldName = "confirm-key";
    List<String> errorCodes = new ArrayList<>();

    if (isDefaultCheckOK(key, fieldName, errorCodes))
    {
      Optional<AuthtionMailing> confirmByKey = mailingRepository.findByTypeAndData(3, key);
      if (confirmByKey.isPresent())
      {
        AuthtionMailing confirm = confirmByKey.get();

        // the AuthtionConsumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
        AuthtionConsumer consumer = consumerService.findByEmail(confirm.getEmail()).get();
        consumer.setEmailConfirmed(true); // email now confirmed
        consumerService.save(consumer);

        confirm.clear();
        mailingRepository.save(confirm);
      }
      else errorCodes.add(fieldName + "-not-exist");
    }
    return getResponse(errorCodes);
  }


  //
  // Password management
  //

  @PostMapping("#{authtionConfigProperties.resource.changePass}")
  @PreAuthorize("hasAuthority('USER')")
  public String changePass(@RequestBody String body, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> map = parse(body);

    String oldpassField = "oldpass";
    String newpassField = "newpass";

    String oldpassValue = getValue(map, oldpassField);
    String newpassValue = getValue(map, newpassField);

    if (isDefaultCheckOK(oldpassValue, oldpassField, errorCodes)
            && canUsePassword(newpassValue, newpassField, errorCodes))
    {
      Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();
      AuthtionConsumer consumer = consumerService.findById(id).get();
      if (matchPassword(oldpassValue, consumer.getPassword()))
      {
        consumer.setNewPassword(newpassValue);
        consumerService.save(consumer);

        mailingRepository.save(AuthtionMailing.of(4, consumer.getEmail()));
      }
      else errorCodes.add("wrong-" + oldpassField);
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.reqRestorePass}")
  public String reqRestorePass(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String email = (String) getValueFromJSON(body, "email");
    int type = 5;

    if (isEmailCheckOK(email, errorCodes) && authtionUtil.isAllowedNewRequestForMailing(type, email, errorCodes))
    {
      if (consumerService.existsByEmail(email))
      {
        mailingRepository.save(AuthtionMailing.of(type, email, getUniqStrBase36(40)));
      }
      else errorCodes.add("email-not-exist");
    }
    return getResponse(errorCodes);
  }

  @GetMapping("#{authtionConfigProperties.resource.confirmRestorePass}")
  public String confirmRestorePass(@RequestParam(required = false) String key)
  {
    String fieldName = "confirm-key";
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> data = new HashMap<>();
    int type = 5;

    if (isDefaultCheckOK(key, fieldName, errorCodes))
    {
      Optional<AuthtionMailing> confirmByKey = mailingRepository.findByTypeAndData(type, key);
      if (confirmByKey.isPresent())
      {
        AuthtionMailing confirm = confirmByKey.get();

        data.put("email", confirm.getEmail());
        data.put("key", confirm.getData());

        mailingRepository.save(confirm);
      }
      else errorCodes.add(fieldName + "-not-exist");
    }
    return getResponse(errorCodes, data);
  }

  @PostMapping("#{authtionConfigProperties.resource.restorePass}")
  public String restorePass(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> map = parse(body);
    int type = 5;

    String emailField = "email";
    String keyField = "key";
    String keyFieldFullName = "confirm-key";
    String newpassField = "newpass";

    String emailValue = getValue(map, emailField);
    String keyValue = getValue(map, keyField);
    String newpassValue = getValue(map, newpassField);

    if (canUsePassword(newpassValue, newpassField, errorCodes)
            && isDefaultCheckOK(keyValue, keyFieldFullName, errorCodes)
            && isEmailCheckOK(emailValue, errorCodes))
    {
      Optional<AuthtionMailing> confirmByKey = mailingRepository.findData(type, emailValue, keyValue);
      if (confirmByKey.isPresent())
      {
        AuthtionMailing confirm = confirmByKey.get();

        // the AuthtionConsumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
        AuthtionConsumer consumer = consumerService.findByEmail(emailValue).get();
        consumer.setNewPassword(newpassValue);
        consumerService.save(consumer);

        confirm.clear();
        mailingRepository.save(confirm);
      }
      else errorCodes.add(keyFieldFullName + "-not-exist");
    }
    return getResponse(errorCodes);
  }


  @GetMapping("#{authtionConfigProperties.resource.signOut}")
  @PreAuthorize("hasAuthority('USER')")
  public String signOut(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();

    OAuth2AccessToken accessToken = ((AuthorizationServerTokenServices) tokenServices).getAccessToken(authentication);
    tokenServices.revokeToken(accessToken.getValue());

    return getResponse(errorCodes);
  }
}


//
// Util classes for mapping requests
//

class ReqCheckEmail
{
  String email;

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }
}

class ReqCheckPass
{
  String password;

  public String getPassword()
  {
    return password;
  }

  public void setPassword(String password)
  {
    this.password = password;
  }
}

class ReqGoogleCaptchaValidate
{
  String googleResponse;

  public String getGoogleResponse()
  {
    return googleResponse;
  }

  public void setGoogleResponse(String googleResponse)
  {
    this.googleResponse = googleResponse;
  }
}

class ReqCreateAccount
{
  String email;
  String password;

  String nickName;
  String firstName;
  String lastName;

  int gender;
  LocalDate dateOfBirth;

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

  public String getLastName()
  {
    return lastName;
  }

  public void setLastName(String lastName)
  {
    this.lastName = lastName;
  }

  public int getGender()
  {
    return gender;
  }

  public void setGender(int gender)
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

class ReqUpdateAccount
{
  String email;
  String emailNonPublic;

  String nickName;
  String nickNameNonPublic;

  String firstName;
  String firstNameNonPublic;

  String middleName;
  String middleNameNonPublic;

  String lastName;
  String lastNameNonPublic;

  String gender;
  String genderNonPublic;

  LocalDate dateOfBirth;
  String dateOfBirthNonPublic;

  public String getEmail()
  {
    return email;
  }

  public void setEmail(String email)
  {
    this.email = email;
  }

  public String getEmailNonPublic()
  {
    return emailNonPublic;
  }

  public void setEmailNonPublic(String emailNonPublic)
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

  public String getNickNameNonPublic()
  {
    return nickNameNonPublic;
  }

  public void setNickNameNonPublic(String nickNameNonPublic)
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

  public String getFirstNameNonPublic()
  {
    return firstNameNonPublic;
  }

  public void setFirstNameNonPublic(String firstNameNonPublic)
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

  public String getMiddleNameNonPublic()
  {
    return middleNameNonPublic;
  }

  public void setMiddleNameNonPublic(String middleNameNonPublic)
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

  public String getLastNameNonPublic()
  {
    return lastNameNonPublic;
  }

  public void setLastNameNonPublic(String lastNameNonPublic)
  {
    this.lastNameNonPublic = lastNameNonPublic;
  }

  public String getGender()
  {
    return gender;
  }

  public void setGender(String gender)
  {
    this.gender = gender;
  }

  public String getGenderNonPublic()
  {
    return genderNonPublic;
  }

  public void setGenderNonPublic(String genderNonPublic)
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

  public String getDateOfBirthNonPublic()
  {
    return dateOfBirthNonPublic;
  }

  public void setDateOfBirthNonPublic(String dateOfBirthNonPublic)
  {
    this.dateOfBirthNonPublic = dateOfBirthNonPublic;
  }
}


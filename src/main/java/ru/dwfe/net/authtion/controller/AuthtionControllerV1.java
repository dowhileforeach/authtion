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
  private final AuthtionUtil util;

  @Autowired
  public AuthtionControllerV1(AuthtionConsumerService consumerService, AuthtionUserRepository userRepository, AuthtionMailingRepository mailingRepository, ConsumerTokenServices tokenServices, RestTemplate restTemplate, AuthtionUtil authtionUtil)
  {
    this.consumerService = consumerService;
    this.userRepository = userRepository;
    this.mailingRepository = mailingRepository;
    this.tokenServices = tokenServices;
    this.restTemplate = restTemplate;
    this.util = authtionUtil;
  }


  //
  // Account: Create, Read, Update
  //

  @PostMapping("#{authtionConfigProperties.resource.checkEmail}")
  public String checkConsumerEmail(@RequestBody ReqEmail req)
  {
    List<String> errorCodes = new ArrayList<>();
    canUseEmail(req.email, consumerService, errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.checkPass}")
  public String checkConsumerPass(@RequestBody ReqPassword req)
  {
    List<String> errorCodes = new ArrayList<>();
    canUsePassword(req.password, "password", errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.googleCaptchaValidate}")
  public String googleCaptchaValidate(@RequestBody ReqGoogleCaptchaResponse req)
  {
    List<String> errorCodes = new ArrayList<>();

    if (isDefaultCheckOK(req.googleResponse, "google-response", errorCodes))
    {
      String url = String.format(util.getGoogleCaptchaSiteVerifyUrl(),
              util.getGoogleCaptchaSecretKey(), req.googleResponse);

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

    String email = req.email;
    String password = req.password;
    String automaticallyGeneratedPassword = "";
    String data = "";

    if (canUseEmail(email, consumerService, errorCodes))
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
      consumer.setEmail(email);
      consumer.setNewPassword(password);
      consumer.setEmailConfirmed(!automaticallyGeneratedPassword.isEmpty());
      prepareNewConsumer(consumer);
      consumerService.save(consumer);
      consumer = consumerService.findByEmail(consumer.getEmail()).get();

      // user
      AuthtionUser user = new AuthtionUser();
      prepareNewUser(user, consumer, req);
      userRepository.save(user);
      user = userRepository.findById(consumer.getId()).get();

      // mailing
      mailingRepository.save(automaticallyGeneratedPassword.isEmpty()
              ? AuthtionMailing.of(1, consumer.getEmail())
              : AuthtionMailing.of(2, consumer.getEmail(), automaticallyGeneratedPassword));

      // data for response
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
      data = prepareAccountInfo(consumer, user, true);
    }
    else errorCodes.add("id-not-exist");

    return getResponse(errorCodes, data);
  }


  @PostMapping("#{authtionConfigProperties.resource.updateAccount}")
  @PreAuthorize("hasAuthority('USER')")
  public String updateAccount(@RequestBody ReqUpdateAccount req, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    boolean consumerWasModified = false;
    boolean userWasModified = false;
    boolean emailWasChanged = false;
    String data = "";

    Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();
    AuthtionConsumer consumer = consumerService.findById(id).get();
    AuthtionUser user = userRepository.findById(id).get();

    String newEmail = req.email;
    Boolean newEmailNonPublic = req.emailNonPublic;

    String newNickName = req.nickName;
    Boolean newNickNameNonPublic = req.nickNameNonPublic;

    String newFirstName = req.firstName;
    Boolean newFirstNameNonPublic = req.firstNameNonPublic;

    String newMiddleName = req.middleName;
    Boolean newMiddleNameNonPublic = req.middleNameNonPublic;

    String newLastName = req.lastName;
    Boolean newLastNameNonPublic = req.lastNameNonPublic;

    Integer newGender = req.gender;
    Boolean newGenderNonPublic = req.genderNonPublic;

    LocalDate newDateOfBirth = req.dateOfBirth;
    Boolean newDateOfBirthNonPublic = req.dateOfBirthNonPublic;

    if (newEmail != null && !newEmail.equals(consumer.getEmail()))
    {
      if (canUseEmail(newEmail, consumerService, errorCodes))
      {
        consumer.setEmail(newEmail);
        consumer.setEmailConfirmed(false);
        consumerWasModified = true;
        emailWasChanged = true;
      }
    }

    if (newEmailNonPublic != null && !newEmailNonPublic.equals(consumer.isEmailNonPublic()))
    {
      consumer.setEmailNonPublic(newEmailNonPublic);
      consumerWasModified = true;
    }

    if (newNickName != null && !newNickName.equals(user.getNickName()))
    {
      user.setNickName(newNickName);
      userWasModified = true;
    }

    if (newNickNameNonPublic != null && !newNickNameNonPublic.equals(user.getNickNameNonPublic()))
    {
      user.setNickNameNonPublic(newNickNameNonPublic);
      userWasModified = true;
    }

    if (newFirstName != null && !newFirstName.equals(user.getFirstName()))
    {
      user.setFirstName(newFirstName);
      userWasModified = true;
    }

    if (newFirstNameNonPublic != null && !newFirstNameNonPublic.equals(user.getFirstNameNonPublic()))
    {
      user.setFirstNameNonPublic(newFirstNameNonPublic);
      userWasModified = true;
    }

    if (newMiddleName != null && !newMiddleName.equals(user.getMiddleName()))
    {
      user.setMiddleName(newMiddleName);
      userWasModified = true;
    }

    if (newMiddleNameNonPublic != null && !newMiddleNameNonPublic.equals(user.getMiddleNameNonPublic()))
    {
      user.setMiddleNameNonPublic(newMiddleNameNonPublic);
      userWasModified = true;
    }

    if (newLastName != null && !newLastName.equals(user.getLastName()))
    {
      user.setLastName(newLastName);
      userWasModified = true;
    }

    if (newLastNameNonPublic != null && !newLastNameNonPublic.equals(user.getLastNameNonPublic()))
    {
      user.setLastNameNonPublic(newLastNameNonPublic);
      userWasModified = true;
    }

    if (newGender != null && !newGender.equals(user.getGender()))
    {
      user.setGender(newGender);
      userWasModified = true;
    }

    if (newGenderNonPublic != null && !newGenderNonPublic.equals(user.getGenderNonPublic()))
    {
      user.setGenderNonPublic(newGenderNonPublic);
      userWasModified = true;
    }

    if (newDateOfBirth != null && !newDateOfBirth.equals(user.getDateOfBirth()))
    {
      user.setDateOfBirth(newDateOfBirth);
      userWasModified = true;
    }

    if (newDateOfBirthNonPublic != null && !newDateOfBirthNonPublic.equals(user.getDateOfBirthNonPublic()))
    {
      user.setDateOfBirthNonPublic(newDateOfBirthNonPublic);
      userWasModified = true;
    }

    if (errorCodes.size() == 0)
    {
      if (consumerWasModified)
      {
        consumerService.save(consumer);
        consumer = consumerService.findById(id).get();
        if (emailWasChanged)
        {
          signOut(authentication);
        }
      }
      if (userWasModified)
      {
        userRepository.save(user);
        user = userRepository.findById(id).get();
      }
      data = prepareAccountInfo(consumer, user, false);
    }
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
    else if (util.isAllowedNewRequestForMailing(type, email, errorCodes))
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
  public String changePass(@RequestBody ReqChangePass req, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();

    if (isDefaultCheckOK(req.oldpass, req.oldpassField, errorCodes)
            && canUsePassword(req.newpass, req.newpassField, errorCodes))
    {
      Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();
      AuthtionConsumer consumer = consumerService.findById(id).get();
      if (matchPassword(req.oldpass, consumer.getPassword()))
      {
        consumer.setNewPassword(req.newpass);
        consumerService.save(consumer);

        mailingRepository.save(AuthtionMailing.of(4, consumer.getEmail()));
      }
      else errorCodes.add("wrong-" + req.oldpassField);
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.reqRestorePass}")
  public String reqRestorePass(@RequestBody ReqEmail req)
  {
    List<String> errorCodes = new ArrayList<>();

    String email = req.email;
    int type = 5;

    if (isEmailCheckOK(email, errorCodes)
            && util.isAllowedNewRequestForMailing(type, email, errorCodes))
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
  public String restorePass(@RequestBody ReqRestorePass req)
  {
    List<String> errorCodes = new ArrayList<>();
    int type = 5;

    if (canUsePassword(req.newpass, req.newpassField, errorCodes)
            && isDefaultCheckOK(req.key, req.keyFieldFullName, errorCodes)
            && isEmailCheckOK(req.email, errorCodes))
    {
      Optional<AuthtionMailing> confirmByKey = mailingRepository.findData(type, req.email, req.key);
      if (confirmByKey.isPresent())
      {
        AuthtionMailing confirm = confirmByKey.get();

        // the AuthtionConsumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
        AuthtionConsumer consumer = consumerService.findByEmail(req.email).get();
        consumer.setNewPassword(req.newpass);
        consumerService.save(consumer);

        confirm.clear();
        mailingRepository.save(confirm);
      }
      else errorCodes.add(req.keyFieldFullName + "-not-exist");
    }
    return getResponse(errorCodes);
  }


  //
  // Auth
  //

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
// Util classes for handling requests
//


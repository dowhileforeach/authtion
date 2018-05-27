package ru.dwfe.net.authtion.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.AuthtionUser;
import ru.dwfe.net.authtion.dao.repository.AuthtionCountryRepository;
import ru.dwfe.net.authtion.dao.repository.AuthtionGenderRepository;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.dao.repository.AuthtionUserRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.util.AuthtionUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static ru.dwfe.net.authtion.dao.AuthtionConsumer.*;
import static ru.dwfe.net.authtion.dao.AuthtionCountry.canUseCountry;
import static ru.dwfe.net.authtion.dao.AuthtionGender.canUseGender;
import static ru.dwfe.net.authtion.dao.AuthtionUser.prepareNewUser;
import static ru.dwfe.net.authtion.util.AuthtionUtil.*;

@RestController
@RequestMapping(path = "#{authtionConfigProperties.api}", produces = "application/json; charset=utf-8")
public class AuthtionControllerV1
{
  private final AuthtionConsumerService consumerService;
  private final AuthtionUserRepository userRepository;
  private final AuthtionCountryRepository countryRepository;
  private final AuthtionGenderRepository genderRepository;
  private final AuthtionMailingRepository mailingRepository;
  private final ConsumerTokenServices tokenServices;
  private final RestTemplate restTemplate;
  private final AuthtionUtil util;

  @Autowired
  public AuthtionControllerV1(AuthtionConsumerService consumerService, AuthtionUserRepository userRepository, AuthtionCountryRepository countryRepository, AuthtionGenderRepository genderRepository, AuthtionMailingRepository mailingRepository, ConsumerTokenServices tokenServices, RestTemplate restTemplate, AuthtionUtil authtionUtil)
  {
    this.consumerService = consumerService;
    this.userRepository = userRepository;
    this.countryRepository = countryRepository;
    this.genderRepository = genderRepository;
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
    var errorCodes = new ArrayList<String>();
    canUseEmail(req.email, consumerService, errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.checkPass}")
  public String checkConsumerPass(@RequestBody ReqPassword req)
  {
    var errorCodes = new ArrayList<String>();
    canUsePassword(req.password, "password", errorCodes);
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.googleCaptchaValidate}")
  public String googleCaptchaValidate(@RequestBody ReqGoogleCaptchaResponse req)
  {
    var errorCodes = new ArrayList<String>();

    if (isDefaultCheckOK(req.googleResponse, "google-response", errorCodes))
    {
      var url = String.format(util.getGoogleCaptchaSiteVerifyUrlTemplate(),
              util.getGoogleCaptchaSecretKey(), req.googleResponse);

      FutureTask<ResponseEntity<String>> exchange =
              new FutureTask<>(() -> restTemplate.exchange(url, HttpMethod.POST, null, String.class));
      new Thread(exchange).start();

      try
      {
        var response = exchange.get(7, TimeUnit.SECONDS);
        if (response.getStatusCodeValue() == 200)
        {
          var success = (Boolean) getValueFromJSON(response.getBody(), "success");
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
    var errorCodes = new ArrayList<String>();
    var email = req.email;
    var password = req.password;
    var automaticallyGeneratedPassword = "";

    if (canUseEmail(email, consumerService, errorCodes))
      if (password == null)
      { // if password wasn't passed
        automaticallyGeneratedPassword = getUniqStrBase64(15);
        password = automaticallyGeneratedPassword;
      }
      else // if password was passed
        canUsePassword(password, "password", errorCodes);

    if (errorCodes.size() == 0)
      canUseGender(req.gender, genderRepository, errorCodes);
    if (errorCodes.size() == 0)
      canUseCountry(req.country, countryRepository, errorCodes);

    if (errorCodes.size() == 0)
    {
      // consumer
      var consumer = new AuthtionConsumer();
      consumer.setEmail(email);
      consumer.setNewPassword(password);
      consumer.setEmailConfirmed(!automaticallyGeneratedPassword.isEmpty());
      prepareNewConsumer(consumer);
      consumerService.save(consumer);
      consumer = consumerService.findByEmail(consumer.getEmail()).get();

      // user
      var user = new AuthtionUser();
      prepareNewUser(user, consumer, req);
      userRepository.save(user);
      user = userRepository.findById(consumer.getId()).get();

      // mailing
      mailingRepository.save(automaticallyGeneratedPassword.isEmpty()
              ? AuthtionMailing.of(1, consumer.getEmail())
              : AuthtionMailing.of(2, consumer.getEmail(), automaticallyGeneratedPassword));
    }
    return getResponse(errorCodes);
  }

  @GetMapping("#{authtionConfigProperties.resource.getAccount}")
  @PreAuthorize("hasAuthority('USER')")
  public String getAccount(OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);

    var consumer = consumerService.findById(id).get();
    var user = userRepository.findById(id).get();
    var data = prepareAccountInfo(consumer, user, false);

    return getResponse(errorCodes, data);
  }

  @GetMapping("#{authtionConfigProperties.resource.publicAccount}" + "/{id}")
  public String publicAccount(@PathVariable Long id)
  {
    var errorCodes = new ArrayList<String>();
    var data = "";

    var consumerById = consumerService.findById(id);
    if (consumerById.isPresent())
    {
      var consumer = consumerById.get();
      var user = userRepository.findById(id).get();
      data = prepareAccountInfo(consumer, user, true);
    }
    else errorCodes.add("id-not-exist");

    return getResponse(errorCodes, data);
  }


  @PostMapping("#{authtionConfigProperties.resource.updateAccount}")
  @PreAuthorize("hasAuthority('USER')")
  public String updateAccount(@RequestBody ReqUpdateAccount req, OAuth2Authentication authentication)
  {
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);
    var consumerWasModified = false;
    var userWasModified = false;
    var emailWasChanged = false;
    var data = "";

    var consumer = consumerService.findById(id).get();
    var user = userRepository.findById(id).get();

    var newEmail = req.email;
    var newEmailNonPublic = req.emailNonPublic;

    var newNickName = prepareStringField(req.nickName, 20);
    var newNickNameNonPublic = req.nickNameNonPublic;

    var newFirstName = prepareStringField(req.firstName, 20);
    var newFirstNameNonPublic = req.firstNameNonPublic;

    var newMiddleName = prepareStringField(req.middleName, 20);
    var newMiddleNameNonPublic = req.middleNameNonPublic;

    var newLastName = prepareStringField(req.lastName, 20);
    var newLastNameNonPublic = req.lastNameNonPublic;

    var newGender = req.gender;
    var newGenderNonPublic = req.genderNonPublic;

    var newDateOfBirth = req.dateOfBirth;
    var newDateOfBirthNonPublic = req.dateOfBirthNonPublic;

    var newCountry = req.country;
    var newCountryNonPublic = req.countryNonPublic;

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

    if (errorCodes.size() == 0)
    {
      if (newGender != null
              && !newGender.equals(user.getGender())
              && canUseGender(newGender, genderRepository, errorCodes))
      {
        user.setGender(newGender);
        userWasModified = true;
      }
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
      if (newCountry != null
              && !newCountry.equals(user.getCountry())
              && canUseCountry(newCountry, countryRepository, errorCodes))
      {
        user.setCountry(newCountry);
        userWasModified = true;
      }
    }

    if (newCountryNonPublic != null && !newCountryNonPublic.equals(user.getCountryNonPublic()))
    {
      user.setCountryNonPublic(newCountryNonPublic);
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
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);

    var consumer = consumerService.findById(id).get();
    var email = consumer.getEmail();
    var type = 3;

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
    var errorCodes = new ArrayList<String>();
    var fieldName = "confirm-key";

    if (isDefaultCheckOK(key, fieldName, errorCodes))
    {
      var confirmByKey = mailingRepository.findByTypeAndData(3, key);
      if (confirmByKey.isPresent())
      {
        var confirm = confirmByKey.get();

        // the AuthtionConsumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
        var consumer = consumerService.findByEmail(confirm.getEmail()).get();
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
    var errorCodes = new ArrayList<String>();
    var id = getId(authentication);

    if (isDefaultCheckOK(req.oldpass, req.oldpassField, errorCodes)
            && canUsePassword(req.newpass, req.newpassField, errorCodes))
    {
      var consumer = consumerService.findById(id).get();
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
    var errorCodes = new ArrayList<String>();
    var email = req.email;
    var type = 5;

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
    var errorCodes = new ArrayList<String>();
    var fieldName = "confirm-key";
    var data = new HashMap<String, Object>();
    var type = 5;

    if (isDefaultCheckOK(key, fieldName, errorCodes))
    {
      var confirmByKey = mailingRepository.findByTypeAndData(type, key);
      if (confirmByKey.isPresent())
      {
        var confirm = confirmByKey.get();

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
    var errorCodes = new ArrayList<String>();
    var type = 5;

    if (canUsePassword(req.newpass, req.newpassField, errorCodes)
            && isDefaultCheckOK(req.key, req.keyFieldFullName, errorCodes)
            && isEmailCheckOK(req.email, errorCodes))
    {
      var confirmByKey = mailingRepository.findData(type, req.email, req.key);
      if (confirmByKey.isPresent())
      {
        var confirm = confirmByKey.get();

        // the AuthtionConsumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
        var consumer = consumerService.findByEmail(req.email).get();
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
    var errorCodes = new ArrayList<String>();

    var accessToken = ((AuthorizationServerTokenServices) tokenServices).getAccessToken(authentication);
    tokenServices.revokeToken(accessToken.getValue());

    return getResponse(errorCodes);
  }
}

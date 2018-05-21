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
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.util.AuthtionUtil;

import java.util.*;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static ru.dwfe.net.authtion.dao.AuthtionConsumer.*;
import static ru.dwfe.net.authtion.util.AuthtionUtil.*;

@RestController
@RequestMapping(path = "#{authtionConfigProperties.api}", produces = "application/json; charset=utf-8")
public class AuthtionControllerV1
{
  private final AuthtionConsumerService consumerService;
  private final AuthtionMailingRepository mailingRepository;
  private final ConsumerTokenServices tokenServices;
  private final RestTemplate restTemplate;
  private final AuthtionUtil authtionUtil;

  @Autowired
  public AuthtionControllerV1(AuthtionConsumerService consumerService, AuthtionMailingRepository mailingRepository, ConsumerTokenServices tokenServices, RestTemplate restTemplate, AuthtionUtil authtionUtil)
  {
    this.consumerService = consumerService;
    this.mailingRepository = mailingRepository;
    this.tokenServices = tokenServices;
    this.restTemplate = restTemplate;
    this.authtionUtil = authtionUtil;
  }

  @PostMapping("#{authtionConfigProperties.resource.checkConsumerEmail}")
  public String checkConsumerEmail(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String email = (String) getValueFromJSON(body, "email");
    canUseEmail(email, consumerService, errorCodes);

    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.checkConsumerPass}")
  public String checkConsumerPass(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String password = (String) getValueFromJSON(body, "password");
    canUsePassword(password, "password", errorCodes);

    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.googleCaptchaValidate}")
  public String googleCaptchaValidate(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String googleResponse = (String) getValueFromJSON(body, "googleResponse");

    if (isDefaultCheckOK(googleResponse, "google-response", errorCodes))
    {
      String url = String.format(authtionUtil.getGoogleCaptchaSiteVerifyUrl(),
              authtionUtil.getGoogleCaptchaSecretKey(), googleResponse);

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

  @PostMapping("#{authtionConfigProperties.resource.createConsumer}")
  public String createConsumer(@RequestBody AuthtionConsumer consumer)
  {
    List<String> errorCodes = new ArrayList<>();

    String password = consumer.getPassword();
    String automaticallyGeneratedPassword = "";

    if (canUseEmail(consumer.getEmail(), consumerService, errorCodes))
      if (password == null)
      { // if password wasn't passed
        automaticallyGeneratedPassword = getUniqStrBase64(15);
        password = automaticallyGeneratedPassword;
      }
      else // if password was passed
        canUsePassword(password, "password", errorCodes);

    if (errorCodes.size() == 0)
    {   // prepare
      setNewPassword(consumer, password);
      prepareNewConsumer(consumer);

      consumer.setEmailConfirmed(!automaticallyGeneratedPassword.isEmpty());

      // put consumer into the DataBase
      consumerService.save(consumer);

      if (automaticallyGeneratedPassword.isEmpty())
      {
        mailingRepository.save(AuthtionMailing.of(1, consumer.getEmail()));
      }
      else
      { // if the password was not passed, then it is necessary to send an automatically generated password to the new consumer
        mailingRepository.save(AuthtionMailing.of(2, consumer.getEmail(), automaticallyGeneratedPassword));
      }
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.updateConsumer}")
  @PreAuthorize("hasAuthority('USER')")
  public String updateConsumer(@RequestBody String body, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> map = parse(body);

    if (map.size() > 0)
    {
      AuthtionConsumer consumerOAuth2 = (AuthtionConsumer) authentication.getPrincipal();

      String nickName = getValue(map, "nickName");
      String firstName = getValue(map, "firstName");
      String lastName = getValue(map, "lastName");

      boolean isNickName = nickName != null;
      boolean isFirstName = firstName != null;
      boolean isLastName = lastName != null;

      if (isNickName || isFirstName || isLastName)
      {
        boolean wasModified = false;
        AuthtionConsumer consumer = consumerService.findByEmail(consumerOAuth2.getEmail()).get();

        if (isNickName && !nickName.equals(consumer.getNickName()))
        {
          consumer.setNickName(nickName);
          wasModified = true;
        }
        if (isFirstName && !firstName.equals(consumer.getFirstName()))
        {
          consumer.setFirstName(firstName);
          wasModified = true;
        }
        if (isLastName && !lastName.equals(consumer.getLastName()))
        {
          consumer.setLastName(lastName);
          wasModified = true;
        }

        if (wasModified)
          consumerService.save(consumer);
      }
    }
    return getResponse(errorCodes);
  }

  @GetMapping("#{authtionConfigProperties.resource.getConsumerData}")
  @PreAuthorize("hasAuthority('USER')")
  public String getConsumerData(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Long id = ((AuthtionConsumer) authentication.getPrincipal()).getId();
    return getResponse(errorCodes, consumerService.findById(id).get().toString());
  }

  @GetMapping("#{authtionConfigProperties.resource.publicConsumer}" + "/{id}")
  public String publicConsumer(@PathVariable Long id)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> data = new HashMap<>();

    Optional<AuthtionConsumer> consumerById = consumerService.findById(id);
    if (consumerById.isPresent())
    {
      AuthtionConsumer consumer = consumerById.get();
      data.put("id", consumer.getId());
      data.put("nickName", consumer.getNickName());
    }
    else errorCodes.add("id-not-exist");

    return getResponse(errorCodes, data);
  }

  @GetMapping("#{authtionConfigProperties.resource.listOfConsumers}")
  @PreAuthorize("hasAuthority('ADMIN')")
  public String listOfConsumers()
  {
    List<String> errorCodes = new ArrayList<>();

    String jsonListOfUsers = consumerService.findAll().stream()
            .map(AuthtionConsumer::toString)
            .collect(Collectors.joining(","));

    return getResponse(errorCodes, "[" + jsonListOfUsers + "]");
  }

  @GetMapping("#{authtionConfigProperties.resource.reqConfirmConsumerEmail}")
  @PreAuthorize("hasAuthority('USER')")
  public String requestConfirmEmail(OAuth2Authentication authentication)
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

  @GetMapping("#{authtionConfigProperties.resource.confirmConsumerEmail}")
  public String confirmConsumerEmail(@RequestParam(required = false) String key)
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

  @PostMapping("#{authtionConfigProperties.resource.changeConsumerPass}")
  @PreAuthorize("hasAuthority('USER')")
  public String changeConsumerPass(@RequestBody String body, OAuth2Authentication authentication)
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
        setNewPassword(consumer, newpassValue);
        consumerService.save(consumer);

        mailingRepository.save(AuthtionMailing.of(4, consumer.getEmail()));
      }
      else errorCodes.add("wrong-" + oldpassField);
    }
    return getResponse(errorCodes);
  }

  @PostMapping("#{authtionConfigProperties.resource.reqRestoreConsumerPass}")
  public String reqRestoreConsumerPass(@RequestBody String body)
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

  @GetMapping("#{authtionConfigProperties.resource.confirmRestoreConsumerPass}")
  public String confirmRestoreConsumerPass(@RequestParam(required = false) String key)
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

  @PostMapping("#{authtionConfigProperties.resource.restoreConsumerPass}")
  public String restoreConsumerPass(@RequestBody String body)
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
        setNewPassword(consumer, newpassValue);
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


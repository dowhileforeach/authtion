package ru.dwfe.net.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import ru.dwfe.net.authtion.dao.Consumer;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;
import ru.dwfe.net.authtion.dao.MailingWelcomeWhenPasswordWasNotPassed;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestorePasswordRepository;
import ru.dwfe.net.authtion.dao.repository.MailingWelcomeWhenPasswordWasNotPassedRepository;
import ru.dwfe.net.authtion.service.ConsumerService;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static ru.dwfe.net.authtion.Global.*;
import static ru.dwfe.net.authtion.dao.Consumer.*;
import static ru.dwfe.net.authtion.util.Util.*;

@RestController
@RequestMapping(path = API_V1, produces = "application/json; charset=utf-8")
@PropertySource("classpath:application.properties")
public class ControllerAuthtionV1
{
  @Autowired
  ConsumerService consumerService;

  @Autowired
  MailingWelcomeWhenPasswordWasNotPassedRepository mailingWelcomeWhenPasswordWasNotPassedRepository;
  @Autowired
  MailingConfirmEmailRepository mailingConfirmEmailRepository;
  @Autowired
  MailingRestorePasswordRepository mailingRestorePasswordRepository;

  @Autowired
  ConsumerTokenServices tokenServices;

  @Autowired
  private Environment env;

  @Autowired
  private RestTemplate restTemplate;

  @PostMapping(resource_checkConsumerEmail)
  public String checkConsumerEmail(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String email = (String) getValueFromJSON(body, "email");
    canUseEmail(email, consumerService, errorCodes);

    return getResponse(errorCodes);
  }

  @PostMapping(resource_checkConsumerPass)
  public String checkConsumerPass(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String password = (String) getValueFromJSON(body, "password");
    canUsePassword(password, "password", errorCodes);

    return getResponse(errorCodes);
  }

  @PostMapping(resource_googleCaptchaValidate)
  public String googleCaptchaValidate(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String captchaSecret = env.getProperty("google.captcha.secret-key");
    String googleResponse = (String) getValueFromJSON(body, "googleResponse");

    if (isDefaultCheckOK(googleResponse, "google-response", errorCodes))
    {
      // https://developers.google.com/recaptcha/docs/verify#api-request
      String url = String.format("https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s",
              captchaSecret, googleResponse);

      FutureTask<ResponseEntity<String>> taskForExchangeWithGoogle =
              new FutureTask<>(() -> restTemplate.exchange(url, HttpMethod.POST, null, String.class));
      new Thread(taskForExchangeWithGoogle).start();

      try
      {
        ResponseEntity<String> response = taskForExchangeWithGoogle.get(7, TimeUnit.SECONDS);
        if (response.getStatusCodeValue() == 200)
        {
          Boolean success = (Boolean) getValueFromJSON(response.getBody(), "success");
          if (!success)
            errorCodes.add("google-captcha-detected-robot");
        }
        else
          errorCodes.add("error-google-captcha-gateway");
      }
      catch (InterruptedException | ExecutionException | TimeoutException e)
      {
        errorCodes.add("timeout-google-captcha-gateway");
      }
    }
    return getResponse(errorCodes);
  }

  @PostMapping(resource_createConsumer)
  public String createConsumer(@RequestBody Consumer consumer)
  {
    List<String> errorCodes = new ArrayList<>();

    String password = consumer.getPassword();
    String automaticallyGeneratedPassword = "";

    if (canUseEmail(consumer.getEmail(), consumerService, errorCodes))
      if (password == null)
      { //if password wasn't passed
        automaticallyGeneratedPassword = getUniqStrBase64(10);
        password = automaticallyGeneratedPassword;
      }
      else //if password was passed
        canUsePassword(password, "password", errorCodes);

    if (errorCodes.size() == 0)
    {   //prepare
      setNewPassword(consumer, password);
      prepareNewConsumer(consumer);

      //put consumer into the database
      consumerService.save(consumer);

      if (automaticallyGeneratedPassword.isEmpty())
      {
        //TODO: service alert #1
      }
      else
      { //if the password was not passed, then it is necessary to send an automatically generated password to the new consumer
        mailingWelcomeWhenPasswordWasNotPassedRepository
                .save(MailingWelcomeWhenPasswordWasNotPassed.of(consumer.getEmail(), automaticallyGeneratedPassword));

        //TODO: service alert #2
        //      set Consumer field 'email_confirmed' to true
      }
    }
    return getResponse(errorCodes);
  }

  @PostMapping(resource_updateConsumer)
  @PreAuthorize("hasAuthority('USER')")
  public String updateConsumer(@RequestBody String body, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> map = parse(body);

    if (map.size() > 0)
    {
      Consumer consumerAuth = (Consumer) authentication.getPrincipal();

      String nickName = (String) getValue(map, "nickName");
      String firstName = (String) getValue(map, "firstName");
      String lastName = (String) getValue(map, "lastName");

      boolean isNickName = nickName != null;
      boolean isFirstName = firstName != null;
      boolean isLastName = lastName != null;

      if (isNickName || isFirstName || isLastName)
      {
        boolean wasModified = false;
        Consumer consumer = consumerService.findByEmail(consumerAuth.getEmail()).get();

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
        {
          consumerService.save(consumer);
        }
      }
    }
    return getResponse(errorCodes);
  }

  @GetMapping(resource_getConsumerData)
  @PreAuthorize("hasAuthority('USER')")
  public String getConsumerData(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    System.out.println(authentication.getPrincipal().toString());
    return getResponse(errorCodes, authentication.getPrincipal().toString());
  }

  @GetMapping(resource_publicConsumer + "/{id}")
  public String publicConsumer(@PathVariable Long id)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> data = new HashMap<>();

    Optional<Consumer> consumerById = consumerService.findById(id);
    if (consumerById.isPresent())
    {
      Consumer consumer = consumerById.get();
      data.put("id", consumer.getId());
      data.put("nickName", consumer.getNickName());
    }
    else errorCodes.add("id-not-exist");

    return getResponse(errorCodes, data);
  }

  @GetMapping(resource_listOfConsumers)
  @PreAuthorize("hasAuthority('ADMIN')")
  public String listOfConsumers()
  {
    List<String> errorCodes = new ArrayList<>();

    String jsonListOfUsers = consumerService.findAll().stream()
            .map(Consumer::toString)
            .collect(Collectors.joining(","));

    return getResponse(errorCodes, "[" + jsonListOfUsers + "]");
  }

  @GetMapping(resource_reqConfirmConsumerEmail)
  @PreAuthorize("hasAuthority('USER')")
  public String requestConfirmEmail(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();

    String email = ((Consumer) authentication.getPrincipal()).getEmail();
    mailingConfirmEmailRepository.save(MailingConfirmEmail.of(email));

    //TODO: service alert #3

    return getResponse(errorCodes);
  }

  @GetMapping(resource_confirmConsumerEmail)
  public String confirmConsumerEmail(@RequestParam(required = false) String key)
  {
    String fieldName = "confirm-key";
    List<String> errorCodes = new ArrayList<>();

    if (isDefaultCheckOK(key, fieldName, errorCodes))
    {
      Optional<MailingConfirmEmail> confirmByKey = mailingConfirmEmailRepository.findByConfirmKey(key);
      if (confirmByKey.isPresent())
      {
        MailingConfirmEmail confirm = confirmByKey.get();

        //The Consumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
        Consumer consumer = consumerService.findByEmail(confirm.getConsumer()).get();
        consumer.setEmailConfirmed(true); //Now email is confirmed
        consumerService.save(consumer);

        //delete this confirmation key from database
        mailingConfirmEmailRepository.delete(confirm);
      }
      else errorCodes.add(fieldName + "-not-exist");
    }
    return getResponse(errorCodes);
  }

  @PostMapping(resource_changeConsumerPass)
  @PreAuthorize("hasAuthority('USER')")
  public String changeConsumerPass(@RequestBody String body, OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> map = parse(body);

    String oldpassField = "oldpass";
    String newpassField = "newpass";

    String oldpassValue = (String) getValue(map, oldpassField);
    String newpassValue = (String) getValue(map, newpassField);

    if (isDefaultCheckOK(oldpassValue, oldpassField, errorCodes)
            && canUsePassword(newpassValue, newpassField, errorCodes))
    {
      Long id = ((Consumer) authentication.getPrincipal()).getId();
      Consumer consumer = consumerService.findById(id).get();
      if (matchPassword(oldpassValue, consumer.getPassword()))
      {
        setNewPassword(consumer, newpassValue);
        consumerService.save(consumer);

        //TODO: service alert #4
      }
      else errorCodes.add("wrong-" + oldpassField);
    }
    return getResponse(errorCodes);
  }

  @PostMapping(resource_reqRestoreConsumerPass)
  public String reqRestoreConsumerPass(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();

    String email = (String) getValueFromJSON(body, "email");

    if (isEmailCheckOK(email, errorCodes))
    {
      if (consumerService.existsByEmail(email))
      {
        MailingRestorePassword confirm = MailingRestorePassword.of(email);
        mailingRestorePasswordRepository.save(confirm);

        //TODO: service alert #5
      }
      else errorCodes.add("email-not-exist");
    }
    return getResponse(errorCodes);
  }

  @GetMapping(resource_confirmRestoreConsumerPass)
  public String confirmRestoreConsumerPass(@RequestParam(required = false) String key)
  {
    String fieldName = "confirm-key";
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> data = new HashMap<>();

    if (isDefaultCheckOK(key, fieldName, errorCodes))
    {
      Optional<MailingRestorePassword> confirmByKey = mailingRestorePasswordRepository.findByConfirmKey(key);
      if (confirmByKey.isPresent())
      {
        data.put("email", confirmByKey.get().getConsumer());
        data.put("key", key);
      }
      else errorCodes.add(fieldName + "-not-exist");
    }
    return getResponse(errorCodes, data);
  }

  @PostMapping(resource_restoreConsumerPass)
  public String restoreConsumerPass(@RequestBody String body)
  {
    List<String> errorCodes = new ArrayList<>();
    Map<String, Object> map = parse(body);

    String emailField = "email";
    String keyField = "key";
    String keyFieldFullName = "confirm-key";
    String newpassField = "newpass";

    String emailValue = (String) getValue(map, emailField);
    String keyValue = (String) getValue(map, keyField);
    String newpassValue = (String) getValue(map, newpassField);

    if (canUsePassword(newpassValue, newpassField, errorCodes)
            && isDefaultCheckOK(keyValue, keyFieldFullName, errorCodes)
            && isEmailCheckOK(emailValue, errorCodes))
    {
      Optional<MailingRestorePassword> confirmByKey = mailingRestorePasswordRepository.findByConfirmKey(keyValue);
      if (confirmByKey.isPresent())
      {
        MailingRestorePassword confirm = confirmByKey.get();
        if (emailValue.equals(confirm.getConsumer()))
        {
          //The Consumer is guaranteed to exist because: FOREIGN KEY (`consumer`) REFERENCES `consumers` (`id`) ON DELETE CASCADE
          Consumer consumer = consumerService.findByEmail(emailValue).get();
          setNewPassword(consumer, newpassValue);
          consumerService.save(consumer);

          mailingRestorePasswordRepository.delete(confirm);
        }
        else errorCodes.add(keyFieldFullName + "-for-another-email");
      }
      else errorCodes.add(keyFieldFullName + "-not-exist");
    }
    return getResponse(errorCodes);
  }

  @GetMapping(resource_signOut)
  @PreAuthorize("hasAuthority('USER')")
  public String signOut(OAuth2Authentication authentication)
  {
    List<String> errorCodes = new ArrayList<>();

    OAuth2AccessToken accessToken = ((AuthorizationServerTokenServices) tokenServices).getAccessToken(authentication);
    tokenServices.revokeToken(accessToken.getValue());

    return getResponse(errorCodes);
  }
}


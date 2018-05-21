package ru.dwfe.net.authtion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static ru.dwfe.net.authtion.util.AuthtionUtil.formatMilliseconds;

@Validated
@Configuration
@ConfigurationProperties(prefix = "dwfe.authtion")
public class AuthtionConfigProperties implements InitializingBean
{
  // == http://www.baeldung.com/configuration-properties-in-spring-boot

  private final static Logger log = LoggerFactory.getLogger(AuthtionConfigProperties.class);

  @NotBlank
  private String api;

  private Resource resource = new Resource();

  private GoogleCaptcha googleCaptcha;

  @NotNull
  private ScheduledTaskMailing scheduledTaskMailing;

  @NotNull
  private OAuth2ClientTrusted oauth2ClientTrusted;

  @NotNull
  private OAuth2ClientUntrusted oauth2ClientUntrusted;

  @Override
  public void afterPropertiesSet() throws Exception
  {
    scheduledTaskMailing.setTimeoutForDuplicateRequest(
            scheduledTaskMailing.getSendInterval() * scheduledTaskMailing.getMaxAttemptsToSendIfError()
    );

    log.info(toString());
  }

  public static class Resource
  {
    private String signIn = "/sign-in";
    private String signOut = "/sign-out";
    private String googleCaptchaValidate = "/google-captcha-validate";

    private String checkConsumerEmail = "/check-consumer-email";
    private String checkConsumerPass = "/check-consumer-pass";
    private String createConsumer = "/create-consumer";
    private String updateConsumer = "/update-consumer";
    private String getConsumerData = "/get-consumer-data";
    private String listOfConsumers = "/list-of-consumers";
    private String publicConsumer = "/public/consumer";
    private String reqConfirmConsumerEmail = "/req-confirm-consumer-email";
    private String confirmConsumerEmail = "/confirm-consumer-email";

    private String changeConsumerPass = "/change-consumer-pass";
    private String reqRestoreConsumerPass = "/req-restore-consumer-pass";
    private String confirmRestoreConsumerPass = "/confirm-restore-consumer-pass";
    private String restoreConsumerPass = "/restore-consumer-pass";

    public String getSignIn()
    {
      return signIn;
    }

    public void setSignIn(String signIn)
    {
      this.signIn = signIn;
    }

    public String getSignOut()
    {
      return signOut;
    }

    public void setSignOut(String signOut)
    {
      this.signOut = signOut;
    }

    public String getGoogleCaptchaValidate()
    {
      return googleCaptchaValidate;
    }

    public void setGoogleCaptchaValidate(String googleCaptchaValidate)
    {
      this.googleCaptchaValidate = googleCaptchaValidate;
    }

    public String getCheckConsumerEmail()
    {
      return checkConsumerEmail;
    }

    public void setCheckConsumerEmail(String checkConsumerEmail)
    {
      this.checkConsumerEmail = checkConsumerEmail;
    }

    public String getCheckConsumerPass()
    {
      return checkConsumerPass;
    }

    public void setCheckConsumerPass(String checkConsumerPass)
    {
      this.checkConsumerPass = checkConsumerPass;
    }

    public String getCreateConsumer()
    {
      return createConsumer;
    }

    public void setCreateConsumer(String createConsumer)
    {
      this.createConsumer = createConsumer;
    }

    public String getUpdateConsumer()
    {
      return updateConsumer;
    }

    public void setUpdateConsumer(String updateConsumer)
    {
      this.updateConsumer = updateConsumer;
    }

    public String getGetConsumerData()
    {
      return getConsumerData;
    }

    public void setGetConsumerData(String getConsumerData)
    {
      this.getConsumerData = getConsumerData;
    }

    public String getListOfConsumers()
    {
      return listOfConsumers;
    }

    public void setListOfConsumers(String listOfConsumers)
    {
      this.listOfConsumers = listOfConsumers;
    }

    public String getPublicConsumer()
    {
      return publicConsumer;
    }

    public void setPublicConsumer(String publicConsumer)
    {
      this.publicConsumer = publicConsumer;
    }

    public String getReqConfirmConsumerEmail()
    {
      return reqConfirmConsumerEmail;
    }

    public void setReqConfirmConsumerEmail(String reqConfirmConsumerEmail)
    {
      this.reqConfirmConsumerEmail = reqConfirmConsumerEmail;
    }

    public String getConfirmConsumerEmail()
    {
      return confirmConsumerEmail;
    }

    public void setConfirmConsumerEmail(String confirmConsumerEmail)
    {
      this.confirmConsumerEmail = confirmConsumerEmail;
    }

    public String getChangeConsumerPass()
    {
      return changeConsumerPass;
    }

    public void setChangeConsumerPass(String changeConsumerPass)
    {
      this.changeConsumerPass = changeConsumerPass;
    }

    public String getReqRestoreConsumerPass()
    {
      return reqRestoreConsumerPass;
    }

    public void setReqRestoreConsumerPass(String reqRestoreConsumerPass)
    {
      this.reqRestoreConsumerPass = reqRestoreConsumerPass;
    }

    public String getConfirmRestoreConsumerPass()
    {
      return confirmRestoreConsumerPass;
    }

    public void setConfirmRestoreConsumerPass(String confirmRestoreConsumerPass)
    {
      this.confirmRestoreConsumerPass = confirmRestoreConsumerPass;
    }

    public String getRestoreConsumerPass()
    {
      return restoreConsumerPass;
    }

    public void setRestoreConsumerPass(String restoreConsumerPass)
    {
      this.restoreConsumerPass = restoreConsumerPass;
    }
  }

  public static class GoogleCaptcha
  {
    @NotBlank
    private String secretKey;

    @NotBlank
    private String siteVerifyUrl;

    public String getSecretKey()
    {
      return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
      this.secretKey = secretKey;
    }

    public String getSiteVerifyUrl()
    {
      return siteVerifyUrl;
    }

    public void setSiteVerifyUrl(String siteVerifyUrl)
    {
      this.siteVerifyUrl = siteVerifyUrl;
    }
  }

  public static class ScheduledTaskMailing
  {
    private int initialDelay = 0;

    private int collectFromDbInterval = 60_000; // 1 minute

    private int sendInterval = 30_000; // 30 seconds

    private int maxAttemptsToSendIfError = 3;

    private int timeoutForDuplicateRequest; // calculated field!!! See method: afterPropertiesSet()

    public int getInitialDelay()
    {
      return initialDelay;
    }

    public void setInitialDelay(int initialDelay)
    {
      this.initialDelay = initialDelay;
    }

    public int getCollectFromDbInterval()
    {
      return collectFromDbInterval;
    }

    public void setCollectFromDbInterval(int collectFromDbInterval)
    {
      this.collectFromDbInterval = collectFromDbInterval;
    }

    public int getSendInterval()
    {
      return sendInterval;
    }

    public void setSendInterval(int sendInterval)
    {
      this.sendInterval = sendInterval;
    }

    public int getMaxAttemptsToSendIfError()
    {
      return maxAttemptsToSendIfError;
    }

    public void setMaxAttemptsToSendIfError(int maxAttemptsToSendIfError)
    {
      this.maxAttemptsToSendIfError = maxAttemptsToSendIfError;
    }

    public int getTimeoutForDuplicateRequest()
    {
      return timeoutForDuplicateRequest;
    }

    public void setTimeoutForDuplicateRequest(int timeoutForDuplicateRequest)
    {
      this.timeoutForDuplicateRequest = timeoutForDuplicateRequest;
    }
  }

  public static class OAuth2ClientTrusted
  {
    @NotBlank
    private String id;

    @NotBlank
    private String password;

    private int tokenValiditySeconds = 60 * 60 * 24 * 20; // 20 days

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }

    public int getTokenValiditySeconds()
    {
      return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds)
    {
      this.tokenValiditySeconds = tokenValiditySeconds;
    }
  }

  public static class OAuth2ClientUntrusted
  {
    @NotBlank
    private String id;

    @NotBlank
    private String password;

    private int tokenValiditySeconds = 60 * 3; // 3 minutes

    public String getId()
    {
      return id;
    }

    public void setId(String id)
    {
      this.id = id;
    }

    public String getPassword()
    {
      return password;
    }

    public void setPassword(String password)
    {
      this.password = password;
    }

    public int getTokenValiditySeconds()
    {
      return tokenValiditySeconds;
    }

    public void setTokenValiditySeconds(int tokenValiditySeconds)
    {
      this.tokenValiditySeconds = tokenValiditySeconds;
    }
  }

  public String getApi()
  {
    return api;
  }

  public void setApi(String api)
  {
    this.api = api;
  }

  public Resource getResource()
  {
    return resource;
  }

  public void setResource(Resource resource)
  {
    this.resource = resource;
  }

  public GoogleCaptcha getGoogleCaptcha()
  {
    return googleCaptcha;
  }

  public void setGoogleCaptcha(GoogleCaptcha googleCaptcha)
  {
    this.googleCaptcha = googleCaptcha;
  }

  public ScheduledTaskMailing getScheduledTaskMailing()
  {
    return scheduledTaskMailing;
  }

  public void setScheduledTaskMailing(ScheduledTaskMailing scheduledTaskMailing)
  {
    this.scheduledTaskMailing = scheduledTaskMailing;
  }

  public OAuth2ClientTrusted getOauth2ClientTrusted()
  {
    return oauth2ClientTrusted;
  }

  public void setOauth2ClientTrusted(OAuth2ClientTrusted oauth2ClientTrusted)
  {
    this.oauth2ClientTrusted = oauth2ClientTrusted;
  }

  public OAuth2ClientUntrusted getOauth2ClientUntrusted()
  {
    return oauth2ClientUntrusted;
  }

  public void setOauth2ClientUntrusted(OAuth2ClientUntrusted oauth2ClientUntrusted)
  {
    this.oauth2ClientUntrusted = oauth2ClientUntrusted;
  }


  @Override
  public String toString()
  {
    return String.format("%n%n" +
                    "-====================================================-%n" +
                    "|            ::[Authtion server]::                   |%n" +
                    "|----------------------------------------------------|%n" +
                    "| API                               %-17s|%n" +
                    "|                                                    |%n" +
                    "| Scheduled Task - Mailing:                          |%n" +
                    "|    initial delay                  %-17s|%n" +
                    "|    collect from DB interval       %-17s|%n" +
                    "|    send interval                  %-17s|%n" +
                    "|    max attempts to send if error  %-17s|%n" +
                    "|    timeout for duplicate request  %-17s|%n" +
                    "|                                                    |%n" +
                    "| Resources                                          |%n" +
                    "|   Auth:                                            |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|   Consumer:                                        |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|   Password management:                             |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|      %-44s  |%n" +
                    "|____________________________________________________|%n",
            api,
            formatMilliseconds(scheduledTaskMailing.initialDelay),
            formatMilliseconds(scheduledTaskMailing.collectFromDbInterval),
            formatMilliseconds(scheduledTaskMailing.sendInterval),
            scheduledTaskMailing.maxAttemptsToSendIfError,
            formatMilliseconds(scheduledTaskMailing.timeoutForDuplicateRequest),
            resource.signIn,
            resource.signOut,
            resource.checkConsumerEmail,
            resource.checkConsumerPass,
            resource.googleCaptchaValidate,
            resource.createConsumer,
            resource.updateConsumer,
            resource.getConsumerData,
            resource.publicConsumer,
            resource.listOfConsumers,
            resource.reqConfirmConsumerEmail,
            resource.confirmConsumerEmail,
            resource.changeConsumerPass,
            resource.reqRestoreConsumerPass,
            resource.confirmRestoreConsumerPass,
            resource.restoreConsumerPass);
  }
}

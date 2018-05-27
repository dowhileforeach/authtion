package ru.dwfe.net.authtion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import static ru.dwfe.net.authtion.util.AuthtionUtil.formatMillisecondsToReadableString;

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
  private Frontend frontend = new Frontend();

  @NotNull
  private ScheduledTaskMailing scheduledTaskMailing;

  @NotNull
  private OAuth2ClientTrusted oauth2ClientTrusted;

  @NotNull
  private OAuth2ClientUntrusted oauth2ClientUntrusted;

  @Override
  public void afterPropertiesSet() throws Exception
  {
    if (scheduledTaskMailing.getTimeoutForDuplicateRequest() <= 0)
      scheduledTaskMailing.setTimeoutForDuplicateRequest(
              scheduledTaskMailing.getSendInterval() * scheduledTaskMailing.getMaxAttemptsToSendIfError());

    log.info(toString());
  }

  public static class Resource
  {
    private String signIn = "/sign-in";
    private String signOut = "/sign-out";

    private String checkEmail = "/check-email";
    private String checkPass = "/check-pass";
    private String googleCaptchaValidate = "/google-captcha-validate";
    private String createAccount = "/create-account";
    private String getAccount = "/get-account";
    private String updateAccount = "/update-account";
    private String publicAccount = "/public/account";
    private String reqConfirmEmail = "/req-confirm-email";
    private String confirmEmail = "/confirm-email";

    private String changePass = "/change-pass";
    private String reqRestorePass = "/req-restore-pass";
    private String confirmRestorePass = "/confirm-restore-pass";
    private String restorePass = "/restore-pass";

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

    public String getCheckEmail()
    {
      return checkEmail;
    }

    public void setCheckEmail(String checkEmail)
    {
      this.checkEmail = checkEmail;
    }

    public String getCheckPass()
    {
      return checkPass;
    }

    public void setCheckPass(String checkPass)
    {
      this.checkPass = checkPass;
    }

    public String getGoogleCaptchaValidate()
    {
      return googleCaptchaValidate;
    }

    public void setGoogleCaptchaValidate(String googleCaptchaValidate)
    {
      this.googleCaptchaValidate = googleCaptchaValidate;
    }

    public String getCreateAccount()
    {
      return createAccount;
    }

    public void setCreateAccount(String createAccount)
    {
      this.createAccount = createAccount;
    }

    public String getGetAccount()
    {
      return getAccount;
    }

    public void setGetAccount(String getAccount)
    {
      this.getAccount = getAccount;
    }

    public String getUpdateAccount()
    {
      return updateAccount;
    }

    public void setUpdateAccount(String updateAccount)
    {
      this.updateAccount = updateAccount;
    }

    public String getPublicAccount()
    {
      return publicAccount;
    }

    public void setPublicAccount(String publicAccount)
    {
      this.publicAccount = publicAccount;
    }

    public String getReqConfirmEmail()
    {
      return reqConfirmEmail;
    }

    public void setReqConfirmEmail(String reqConfirmEmail)
    {
      this.reqConfirmEmail = reqConfirmEmail;
    }

    public String getConfirmEmail()
    {
      return confirmEmail;
    }

    public void setConfirmEmail(String confirmEmail)
    {
      this.confirmEmail = confirmEmail;
    }

    public String getChangePass()
    {
      return changePass;
    }

    public void setChangePass(String changePass)
    {
      this.changePass = changePass;
    }

    public String getReqRestorePass()
    {
      return reqRestorePass;
    }

    public void setReqRestorePass(String reqRestorePass)
    {
      this.reqRestorePass = reqRestorePass;
    }

    public String getConfirmRestorePass()
    {
      return confirmRestorePass;
    }

    public void setConfirmRestorePass(String confirmRestorePass)
    {
      this.confirmRestorePass = confirmRestorePass;
    }

    public String getRestorePass()
    {
      return restorePass;
    }

    public void setRestorePass(String restorePass)
    {
      this.restorePass = restorePass;
    }
  }

  public static class GoogleCaptcha
  {
    @NotBlank
    private String secretKey;

    @NotBlank
    private String siteVerifyUrlTemplate;

    public String getSecretKey()
    {
      return secretKey;
    }

    public void setSecretKey(String secretKey)
    {
      this.secretKey = secretKey;
    }

    public String getSiteVerifyUrlTemplate()
    {
      return siteVerifyUrlTemplate;
    }

    public void setSiteVerifyUrlTemplate(String siteVerifyUrlTemplate)
    {
      this.siteVerifyUrlTemplate = siteVerifyUrlTemplate;
    }
  }

  public static class Frontend
  {
    private String host = "http://localhost";
    private String resourceConfirmEmail = "/confirm-email";
    private String resourceConfirmRestorePass = "/confirm-restore-pass";

    public String getHost()
    {
      return host;
    }

    public void setHost(String host)
    {
      this.host = host;
    }

    public String getResourceConfirmEmail()
    {
      return resourceConfirmEmail;
    }

    public void setResourceConfirmEmail(String resourceConfirmEmail)
    {
      this.resourceConfirmEmail = resourceConfirmEmail;
    }

    public String getResourceConfirmRestorePass()
    {
      return resourceConfirmRestorePass;
    }

    public void setResourceConfirmRestorePass(String resourceConfirmRestorePass)
    {
      this.resourceConfirmRestorePass = resourceConfirmRestorePass;
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

  public static Logger getLog()
  {
    return log;
  }

  public Frontend getFrontend()
  {
    return frontend;
  }

  public void setFrontend(Frontend frontend)
  {
    this.frontend = frontend;
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
                    "| API                               %s%n" +
                    "|                                                     %n" +
                    "| Scheduled Task - Mailing:                           %n" +
                    "|    initial delay                  %s%n" +
                    "|    collect from DB interval       %s%n" +
                    "|    send interval                  %s%n" +
                    "|    max attempts to send if error  %s%n" +
                    "|    timeout for duplicate request  %s%n" +
                    "|                                                     %n" +
                    "| Resources                                           %n" +
                    "|   Auth:                                             %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|   Account:                                          %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|   Password management:                              %n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|      %s%n" +
                    "|                                                     %n" +
                    "| Frontend                                            %n" +
                    "|    host                     %s%n" +
                    "|    resources for:                                   %n" +
                    "|      /confirm-email         %s%n" +
                    "|      /confirm-restore-pass  %s%n" +
                    "|_____________________________________________________%n",
            api,
            formatMillisecondsToReadableString(scheduledTaskMailing.initialDelay),
            formatMillisecondsToReadableString(scheduledTaskMailing.collectFromDbInterval),
            formatMillisecondsToReadableString(scheduledTaskMailing.sendInterval),
            scheduledTaskMailing.maxAttemptsToSendIfError,
            formatMillisecondsToReadableString(scheduledTaskMailing.timeoutForDuplicateRequest),
            resource.signIn,
            resource.signOut,
            resource.checkEmail,
            resource.checkPass,
            resource.googleCaptchaValidate,
            resource.createAccount,
            resource.getAccount,
            resource.publicAccount,
            resource.updateAccount,
            resource.reqConfirmEmail,
            resource.confirmEmail,
            resource.changePass,
            resource.reqRestorePass,
            resource.confirmRestorePass,
            resource.restorePass,
            frontend.host,
            frontend.resourceConfirmEmail,
            frontend.resourceConfirmRestorePass);
  }
}

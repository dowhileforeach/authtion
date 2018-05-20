package ru.dwfe.net.authtion;

import org.springframework.stereotype.Component;

@Component
public class AuthtionGlobal
{

  public static final String resource_signIn = "/sign-in";
  public static final String resource_signOut = "/sign-out";
  public static final String resource_googleCaptchaValidate = "/google-captcha-validate";

  public static final String resource_checkConsumerEmail = "/check-consumer-email";
  public static final String resource_checkConsumerPass = "/check-consumer-pass";
  public static final String resource_createConsumer = "/create-consumer";
  public static final String resource_updateConsumer = "/update-consumer";
  public static final String resource_getConsumerData = "/get-consumer-data";
  public static final String resource_listOfConsumers = "/list-of-consumers";
  public static final String resource_publicConsumer = "/public/consumer";
  public static final String resource_reqConfirmConsumerEmail = "/req-confirm-consumer-email";
  public static final String resource_confirmConsumerEmail = "/confirm-consumer-email";

  public static final String resource_changeConsumerPass = "/change-consumer-pass";
  public static final String resource_reqRestoreConsumerPass = "/req-restore-consumer-pass";
  public static final String resource_confirmRestoreConsumerPass = "/confirm-restore-consumer-pass";
  public static final String resource_restoreConsumerPass = "/restore-consumer-pass";

  private AuthtionGlobal()
  {
  }
}

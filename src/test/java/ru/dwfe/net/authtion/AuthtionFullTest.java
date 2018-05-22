package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.junit4.SpringRunner;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.test.AuthtionTestChecker;
import ru.dwfe.net.authtion.test.AuthtionTestClient;
import ru.dwfe.net.authtion.test.AuthtionTestConsumer;
import ru.dwfe.net.authtion.test.AuthtionTestUtil;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.USER;
import static ru.dwfe.net.authtion.test.AuthtionTestResourceAccessingType.USUAL;
import static ru.dwfe.net.authtion.test.AuthtionTestVariablesForAuthTest.TOTAL_ACCESS_TOKEN_COUNT;
import static ru.dwfe.net.authtion.test.AuthtionTestVariablesForConsumerTest.*;

//
// == https://spring.io/guides/gs/testing-web/
//

@RunWith(SpringRunner.class)
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT  // == https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-testing.html#boot-features-testing-spring-boot-applications
)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthtionFullTest
{
  private static Set<String> auth_test_access_tokens = new HashSet<>();

  @Autowired
  private AuthtionConfigProperties authtionConfigProperties;

  @Autowired
  private AuthtionTestConsumer authtionTestConsumer;
  @Autowired
  private AuthtionTestClient authtionTestClient;

  @Autowired
  private AuthtionTestUtil authtionTestUtil;
  @Autowired
  private AuthtionConsumerService consumerService;
  @Autowired
  private AuthtionMailingRepository mailingRepository;

  @Test
  public void _001_auth_USER()
  {
    logHead("USER");

    AuthtionTestConsumer testConsumer = authtionTestConsumer.getUSER();
    auth_test_access_tokens.add(testConsumer.access_token);

    fullAuthTest(testConsumer);
  }

  @Test
  public void _002_auth_ADMIN()
  {
    logHead("ADMIN");

    AuthtionTestConsumer testConsumer = authtionTestConsumer.getADMIN();
    auth_test_access_tokens.add(testConsumer.access_token);

    fullAuthTest(testConsumer);
  }

  @Test
  public void _003_auth_ANY()
  {
    logHead("ANY");

    AuthtionTestConsumer testConsumer = authtionTestConsumer.getAnonymous();
    authtionTestUtil.performResourceAccessing(testConsumer.access_token, testConsumer.level, USUAL);
  }

  @Test
  public void _004_auth_different_access_tokens()
  {
    logHead("list of Access Tokens");
    log.info("\n\n{}", auth_test_access_tokens.stream().collect(Collectors.joining("\n")));

    assertEquals(TOTAL_ACCESS_TOKEN_COUNT, auth_test_access_tokens.size());
  }

  @Test
  public void _005_account_checkEmail()
  {
    logHead("Check Email");
    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getCheckEmail(), authtionTestConsumer.getAnonymous().access_token, checkers_for_checkConsumerEmail);
  }

  @Test
  public void _006_account_checkPass()
  {
    logHead("Check Pass");
    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getCheckPass(), authtionTestConsumer.getAnonymous().access_token, checkers_for_checkConsumerPass);
  }

  @Test
  public void _007_account_createAccount()
  {
    logHead("Create Account");

    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getCreateAccount(), authtionTestConsumer.getAnonymous().access_token, checkers_for_createConsumer());
    //
    // Was created 3 new consumers:
    //  - EMAIL_NEW_Consumer   - password was passed
    //  - EMAIL_2_NEW_Consumer - password was not passed
    //  - EMAIL_3_NEW_Consumer - already encoded password was passed


    // EMAIL_NEW_Consumer
    AuthtionConsumer consumer1 = getConsumerByEmail(EMAIL_NEW_Consumer);
    assertTrue(consumer1.getId() > 999);

    Collection<? extends GrantedAuthority> authorities_consumer1 = consumer1.getAuthorities();
    assertEquals(1, authorities_consumer1.size());
    assertEquals("USER", authorities_consumer1.iterator().next().getAuthority());

// TODO
//    assertEquals("nobody", consumer1.getNickName());
//    assertTrue(consumer1.getFirstName().isEmpty());
//    assertEquals("sunshine", consumer1.getLastName());
    assertTrue(consumer1.isAccountNonExpired());
    assertTrue(consumer1.isAccountNonLocked());
    assertTrue(consumer1.isCredentialsNonExpired());
    assertTrue(consumer1.isEnabled());
    assertFalse(consumer1.isEmailConfirmed());

    List<AuthtionMailing> mailing_consumer1 = mailingRepository.findByTypeAndEmail(1, consumer1.getEmail());
    assertEquals(1, mailing_consumer1.size());

    // EMAIL_2_NEW_Consumer
    AuthtionConsumer consumer2 = getConsumerByEmail(EMAIL_2_NEW_Consumer);
    assertTrue(consumer2.getId() > 999);

    Collection<? extends GrantedAuthority> authorities_consumer2 = consumer2.getAuthorities();
    assertEquals(1, authorities_consumer2.size());
    assertEquals("USER", authorities_consumer2.iterator().next().getAuthority());

//TODO
//    assertEquals(AuthtionConsumer.prepareStringField(AuthtionConsumer.getNickNameFromEmail(consumer2.getEmail()), 20), consumer2.getNickName());
//    assertEquals("ozon", consumer2.getFirstName());
//    assertTrue(consumer2.getLastName().isEmpty());
    assertTrue(consumer2.isAccountNonExpired());
    assertTrue(consumer2.isAccountNonLocked());
    assertTrue(consumer2.isCredentialsNonExpired());
    assertTrue(consumer2.isEnabled());
    assertTrue(consumer2.isEmailConfirmed());

    List<AuthtionMailing> mailing_consumer2 = mailingRepository.findByTypeAndEmail(2, consumer2.getEmail());
    assertEquals(1, mailing_consumer2.size());

    String mailing_password_consumer2 = mailing_consumer2.get(0).getData();
    assertTrue(mailing_password_consumer2.length() >= 9);


    // EMAIL_3_NEW_Consumer
    AuthtionConsumer consumer3 = getConsumerByEmail(EMAIL_3_NEW_Consumer);
    assertTrue(consumer3.getId() > 999);
    assertEquals(consumer3.getPassword(), "{bcrypt}" + PASS_FOR_EMAIL_3_Consumer_Encoded);

    Collection<? extends GrantedAuthority> authorities_consumer3 = consumer3.getAuthorities();
    assertEquals(1, authorities_consumer3.size());
    assertEquals("USER", authorities_consumer3.iterator().next().getAuthority());

//TODO
//    assertEquals("hello world", consumer3.getNickName());
//    assertTrue(consumer3.getFirstName().isEmpty());
//    assertTrue(consumer3.getLastName().isEmpty());
    assertTrue(consumer3.isAccountNonExpired());
    assertTrue(consumer3.isAccountNonLocked());
    assertTrue(consumer3.isCredentialsNonExpired());
    assertTrue(consumer3.isEnabled());
    assertFalse(consumer3.isEmailConfirmed());

    List<AuthtionMailing> mailing_consumer3 = mailingRepository.findByTypeAndEmail(1, consumer3.getEmail());
    assertEquals(1, mailing_consumer3.size());

    // Perform full auth test for New AuthtionConsumer
    fullAuthTest(authtionTestConsumer.of(USER, consumer1.getEmail(), PASS_NEW_Consumer, authtionTestClient.getClientTrusted(), 200));
    fullAuthTest(authtionTestConsumer.of(USER, consumer2.getEmail(), mailing_password_consumer2, authtionTestClient.getClientTrusted(), 200));
    fullAuthTest(authtionTestConsumer.of(USER, consumer3.getEmail(), PASS_FOR_EMAIL_3_Consumer_Decoded, authtionTestClient.getClientTrusted(), 200));
  }

  @Test
  public void _008_account_updateAccount()
  {
    logHead("Update Account");

    AuthtionTestConsumer USER_consumer = authtionTestConsumer.getUSER();

//TODO
//    AuthtionConsumer consumer = getConsumerByEmail(USER_consumer.username);
//    assertEquals("test2", consumer.getNickName());
//    assertEquals("", consumer.getFirstName());
//    assertEquals("", consumer.getLastName());
//
//    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getUpdateUser(), USER_consumer.access_token, checkers_for_updateConsumer1);
//    consumer = getConsumerByEmail(USER_consumer.username);
//    assertEquals("test2", consumer.getNickName());
//    assertEquals("", consumer.getFirstName());
//    assertEquals("", consumer.getLastName());
//
//    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getUpdateUser(), USER_consumer.access_token, checkers_for_updateConsumer5);
//    consumer = getConsumerByEmail(USER_consumer.username);
//    assertEquals("good", consumer.getNickName());
//    assertEquals("alto", consumer.getFirstName());
//    assertEquals("smith", consumer.getLastName());
//
//    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getUpdateUser(), USER_consumer.access_token, checkers_for_updateConsumer3);
//    consumer = getConsumerByEmail(USER_consumer.username);
//    assertEquals("hello", consumer.getNickName());
//    assertEquals("1", consumer.getFirstName());
//    assertEquals("smith", consumer.getLastName());
//
//    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getUpdateUser(), USER_consumer.access_token, checkers_for_updateConsumer4);
//    consumer = getConsumerByEmail(USER_consumer.username);
//    assertEquals("hello", consumer.getNickName());
//    assertEquals("1", consumer.getFirstName());
//    assertEquals("2", consumer.getLastName());
//
//    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getUpdateUser(), USER_consumer.access_token, checkers_for_updateConsumer2);
//    consumer = getConsumerByEmail(USER_consumer.username);
//    assertEquals("user", consumer.getNickName());
//    assertEquals("", consumer.getFirstName());
//    assertEquals("", consumer.getLastName());
  }

  @Test
  public void _009_account_getAccount()
  {
    logHead("Get Account");
    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getGetAccount(), authtionTestConsumer.getUSER().access_token, checkers_for_getConsumerData);
  }

  @Test
  public void _010_account_publicAccount()
  {
    logHead("Public Account");

    AuthtionTestConsumer ANY_consumer = authtionTestConsumer.getAnonymous();
    AuthtionTestConsumer USER_consumer = authtionTestConsumer.getUSER();
    AuthtionTestConsumer ADMIN_consumer = authtionTestConsumer.getADMIN();

//TODO
//    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getPublicUser() + "/9", ANY_consumer.access_token, checkers_for_publicConsumer_9);
//    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getPublicUser() + "/9", USER_consumer.access_token, checkers_for_publicConsumer_9);
//    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getPublicUser() + "/9", ADMIN_consumer.access_token, checkers_for_publicConsumer_9);
//    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getPublicUser() + "/1000", ANY_consumer.access_token, checkers_for_publicConsumer_1);
//    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getPublicUser() + "/1000", USER_consumer.access_token, checkers_for_publicConsumer_1);
//    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getPublicUser() + "/1000", ADMIN_consumer.access_token, checkers_for_publicConsumer_1);
  }

  @Test
  public void _011_account_reqConfirmEmail()
  {
    logHead("Request Confirm Email");

    AuthtionTestConsumer USER_consumer = authtionTestConsumer.getUSER();
    int type = 3;

    mailingRepository.deleteAll();

    // сheck for 'email-is-already-confirmed' error
    AuthtionConsumer consumerFromDB = getConsumerByEmail(USER_consumer.username);
    assertTrue(consumerFromDB.isEmailConfirmed());
    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getReqConfirmEmail(), USER_consumer.access_token,
            checkers_for_reqConfirmConsumerEmail_isConfirmed);

    // add new request
    AuthtionTestConsumer consumer = authtionTestConsumer.of(USER, EMAIL_NEW_Consumer, PASS_NEW_Consumer, authtionTestClient.getClientTrusted(), 200);
    List<AuthtionMailing> confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(0, confirmByEmail.size());
    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getReqConfirmEmail(), consumer.access_token,
            checkers_for_reqConfirmConsumerEmail);

    // check that the request was success added
    confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(1, confirmByEmail.size());

    AuthtionMailing mailing = confirmByEmail.get(0);
    assertFalse(mailing.isSent());
    assertFalse(mailing.isMaxAttemptsReached());
    assertTrue(mailing.getData().length() >= 28);

    // Ok. At the moment we have 1 key and it is not yet time to send a duplicate request
    // Let's try to add one more key ==> сheck for 'delay-between-duplicate-requests' error
    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getReqConfirmEmail(), consumer.access_token,
            checkers_for_reqConfirmConsumerEmail_duplicateDelay);

    try
    {
      log.info("Please wait 9 seconds...");
      TimeUnit.SECONDS.sleep(9);
    }
    catch (InterruptedException ignored)
    {
    }

    Optional<AuthtionMailing> confirmByEmailOpt = mailingRepository.findLastSentNotEmptyData(type, consumer.username);
    assertTrue(confirmByEmailOpt.isPresent()); // new key was success added and sent
    mailing = confirmByEmailOpt.get();
    assertFalse(mailing.isMaxAttemptsReached());
    String alreadySentKeyOld = mailing.getData();

    // At the moment we have a key that has already been sent and is waiting for confirmation
    // Try to add one more key, because duplicate delay should already expire
    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getReqConfirmEmail(), consumer.access_token,
            checkers_for_reqConfirmConsumerEmail);

    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getReqConfirmEmail(), consumer.access_token,
            checkers_for_reqConfirmConsumerEmail_duplicateDelay);

    try
    {
      log.info("Please wait 8 seconds...");
      TimeUnit.SECONDS.sleep(8);
    }
    catch (InterruptedException ignored)
    {
    }

    confirmByEmailOpt = mailingRepository.findLastSentNotEmptyData(type, consumer.username);
    assertTrue(confirmByEmailOpt.isPresent());
    mailing = confirmByEmailOpt.get();
    assertFalse(mailing.isMaxAttemptsReached());
    assertNotEquals(alreadySentKeyOld, mailing.getData());  // another new key was success added and sent

    consumerFromDB = getConsumerByEmail(consumer.username);
    assertFalse(consumerFromDB.isEmailConfirmed());
  }

  @Test
  public void _012_account_confirmEmail()
  {
    logHead("Confirm Email");

    int type = 3;

    assertFalse(getConsumerByEmail(EMAIL_NEW_Consumer).isEmailConfirmed());

    List<AuthtionMailing> confirmList = mailingRepository.findSentNotEmptyData(type, EMAIL_NEW_Consumer);
    assertEquals(2, confirmList.size());
    String confirmKey = confirmList.get(0).getData();
    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getConfirmEmail(), null, checkers_for_confirmConsumerEmail(confirmKey));

    confirmList = mailingRepository.findSentNotEmptyData(type, EMAIL_NEW_Consumer);
    assertEquals(1, confirmList.size());
    assertTrue(getConsumerByEmail(EMAIL_NEW_Consumer).isEmailConfirmed());
  }

  @Test
  public void _013_password_changePass()
  {
    logHead("Change Password");

    changePass(EMAIL_NEW_Consumer, PASS_NEW_Consumer, NEWPASS_NEW_Consumer, checkers_for_changeConsumerPass);

    changePass(EMAIL_3_NEW_Consumer, PASS_FOR_EMAIL_3_Consumer_Decoded, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, checkers_for_changeConsumerPass_3);
  }

  private void changePass(String email, String oldpass, String newpass, List<AuthtionTestChecker> checkers)
  {
    logHead("Change Password = " + email);

    //newpass
    //AuthtionTestConsumer.of(USER, email, newpass, authtionTestClient.getOauth2ClientTrusted(), 400);

    //oldpass
    AuthtionTestConsumer consumerTest = authtionTestConsumer.of(USER, email, oldpass, authtionTestClient.getClientTrusted(), 200);

    //change oldpass
    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getChangePass(), consumerTest.access_token, checkers);

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpass, authtionTestClient.getOauth2ClientTrusted(), 400);

    //newpass
    consumerTest = authtionTestConsumer.of(USER, email, newpass, authtionTestClient.getClientTrusted(), 200);
    fullAuthTest(consumerTest);
  }

  @Test
  public void _014_password_restorePass()
  {
    logHead("Restore Password");

    performRestorePass(EMAIL_NEW_Consumer, NEWPASS_NEW_Consumer, PASS_NEW_Consumer, PASS_NEW_Consumer);

    performRestorePass(EMAIL_3_NEW_Consumer, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, PASS_FOR_EMAIL_3_Consumer_Decoded, PASS_FOR_EMAIL_3_Consumer_Encoded);
  }

  private void performRestorePass(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    reqRestorePass(email);
    confirmRestorePass(email);
    restorePass(email, oldpassDecoded, newpassDecoded, newpassForCheckers);
  }

  private void reqRestorePass(String email)
  {
    logHead("Request Restore Password = " + email);

    AuthtionTestConsumer ANY_consumer = authtionTestConsumer.getAnonymous();
    int type = 5;

    assertEquals(0, mailingRepository.findByTypeAndEmail(type, email).size());

    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getReqRestorePass(), ANY_consumer.access_token,
            checkers_for_reqRestoreConsumerPass(email));

    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getReqRestorePass(), ANY_consumer.access_token,
            checkers_for_reqRestoreConsumerPass_duplicateDelay(email));

    try
    {
      log.info("Please wait 8 seconds...");
      TimeUnit.SECONDS.sleep(8);
    }
    catch (InterruptedException ignored)
    {
    }

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(1, confirmByEmail.size());
    assertFalse(confirmByEmail.get(0).isMaxAttemptsReached());
    assertTrue(confirmByEmail.get(0).getData().length() >= 28);

    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getReqRestorePass(), ANY_consumer.access_token,
            checkers_for_reqRestoreConsumerPass(email));

    try
    {
      log.info("Please wait 8 seconds...");
      TimeUnit.SECONDS.sleep(8);
    }
    catch (InterruptedException ignored)
    {
    }

    confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());
  }

  private void confirmRestorePass(String email)
  {
    logHead("Confirm Restore Password = " + email);

    AuthtionTestConsumer ANY_consumer = authtionTestConsumer.getAnonymous();
    int type = 5;

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    authtionTestUtil.check_send_data(GET, authtionConfigProperties.getResource().getConfirmRestorePass(), ANY_consumer.access_token, checkers_for_confirmRestoreConsumerPass(email, confirmByEmail.get(0).getData()));

    confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());
  }

  private void restorePass(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    logHead("Restore Password = " + email);

    AuthtionTestConsumer ANY_consumer = authtionTestConsumer.getAnonymous();
    int type = 5;

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpassDecoded, authtionTestClient.getOauth2ClientTrusted(), 200);
    //newpass
    //AuthtionTestConsumer.of(USER, email, newpassDecoded, authtionTestClient.getOauth2ClientTrusted(), 400);

    //change password
    authtionTestUtil.check_send_data(POST, authtionConfigProperties.getResource().getRestorePass(), ANY_consumer.access_token,
            checkers_for_restoreConsumerPass(email, newpassForCheckers, confirmByEmail.get(0).getData()));

    assertEquals(1, mailingRepository.findSentNotEmptyData(type, email).size());

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpassDecoded, authtionTestClient.getOauth2ClientTrusted(), 400);

    //newpass
    AuthtionTestConsumer consumerTest = authtionTestConsumer.of(USER, email, newpassDecoded, authtionTestClient.getClientTrusted(), 200);
    fullAuthTest(consumerTest);
  }

  private void fullAuthTest(AuthtionTestConsumer testConsumer)
  {
    authtionTestUtil.performFullAuthTest(testConsumer);
    mailingRepository.deleteAll();
  }

  private AuthtionConsumer getConsumerByEmail(String email)
  {
    Optional<AuthtionConsumer> consumerFromDBOpt = consumerService.findByEmail(email);
    assertTrue(consumerFromDBOpt.isPresent());
    return consumerFromDBOpt.get();
  }

  private static void logHead(String who)
  {
    log.info("\n=============================="
            + "\n {} "
            + "\n------------------------------", who);
  }

  private static final Logger log = LoggerFactory.getLogger(AuthtionFullTest.class);
}

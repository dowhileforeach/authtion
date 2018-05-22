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
import static ru.dwfe.net.authtion.test.AuthtionTestVariablesForAccountPasswordTests.*;
import static ru.dwfe.net.authtion.test.AuthtionTestVariablesForAuthTests.TOTAL_ACCESS_TOKEN_COUNT;

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
  private AuthtionConfigProperties prop;

  @Autowired
  private AuthtionTestConsumer testConsumer;
  @Autowired
  private AuthtionTestClient testClient;

  @Autowired
  private AuthtionTestUtil util;
  @Autowired
  private AuthtionConsumerService consumerService;
  @Autowired
  private AuthtionMailingRepository mailingRepository;

  @Test
  public void _001_auth_USER()
  {
    logHead("USER");

    AuthtionTestConsumer consumer = testConsumer.getUSER();
    auth_test_access_tokens.add(consumer.access_token);

    fullAuthTest(consumer);
  }

  @Test
  public void _002_auth_ADMIN()
  {
    logHead("ADMIN");

    AuthtionTestConsumer consumer = testConsumer.getADMIN();
    auth_test_access_tokens.add(consumer.access_token);

    fullAuthTest(consumer);
  }

  @Test
  public void _003_auth_ANY()
  {
    logHead("ANY");

    AuthtionTestConsumer consumer = testConsumer.getAnonymous();
    util.performResourceAccessing(consumer.access_token, consumer.level, USUAL);
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
    util.check_send_data(POST, prop.getResource().getCheckEmail(),
            testConsumer.getAnonymous_accessToken(), checkers_for_checkEmail);
  }

  @Test
  public void _006_account_checkPass()
  {
    logHead("Check Pass");
    util.check_send_data(POST, prop.getResource().getCheckPass(),
            testConsumer.getAnonymous_accessToken(), checkers_for_checkPass);
  }

  @Test
  public void _007_account_createAccount()
  {
    logHead("Create Account");

    util.check_send_data(POST, prop.getResource().getCreateAccount(),
            testConsumer.getAnonymous_accessToken(), checkers_for_createAccount());
    //
    // Was created 3 new consumers:
    //  - Account3_Email   - password was passed
    //  - Account4_Email - password was not passed
    //  - Account5_Email - already encoded password was passed


    // Account3_Email
    AuthtionConsumer consumer1 = getConsumerByEmail(Account3_Email);
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

    // Account4_Email
    AuthtionConsumer consumer2 = getConsumerByEmail(Account4_Email);
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


    // Account5_Email
    AuthtionConsumer consumer3 = getConsumerByEmail(Account5_Email);
    assertTrue(consumer3.getId() > 999);
    assertEquals(consumer3.getPassword(), "{bcrypt}" + Account5_Pass_Encoded);

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
    fullAuthTest(testConsumer.of(USER, consumer1.getEmail(), Account3_Pass, testClient.getClientTrusted(), 200));
    fullAuthTest(testConsumer.of(USER, consumer2.getEmail(), mailing_password_consumer2, testClient.getClientTrusted(), 200));
    fullAuthTest(testConsumer.of(USER, consumer3.getEmail(), Account5_Pass_Decoded, testClient.getClientTrusted(), 200));
  }

  @Test
  public void _008_account_updateAccount()
  {
    logHead("Update Account");

    AuthtionTestConsumer USER = testConsumer.getUSER();

//TODO
//    AuthtionConsumer consumer = getConsumerByEmail(USER.username);
//    assertEquals("test2", consumer.getNickName());
//    assertEquals("", consumer.getFirstName());
//    assertEquals("", consumer.getLastName());
//
//    util.check_send_data(POST, prop.getResource().getUpdateUser(), USER.access_token, checkers_for_updateAccount1);
//    consumer = getConsumerByEmail(USER.username);
//    assertEquals("test2", consumer.getNickName());
//    assertEquals("", consumer.getFirstName());
//    assertEquals("", consumer.getLastName());
//
//    util.check_send_data(POST, prop.getResource().getUpdateUser(), USER.access_token, checkers_for_updateAccount5);
//    consumer = getConsumerByEmail(USER.username);
//    assertEquals("good", consumer.getNickName());
//    assertEquals("alto", consumer.getFirstName());
//    assertEquals("smith", consumer.getLastName());
//
//    util.check_send_data(POST, prop.getResource().getUpdateUser(), USER.access_token, checkers_for_updateAccount3);
//    consumer = getConsumerByEmail(USER.username);
//    assertEquals("hello", consumer.getNickName());
//    assertEquals("1", consumer.getFirstName());
//    assertEquals("smith", consumer.getLastName());
//
//    util.check_send_data(POST, prop.getResource().getUpdateUser(), USER.access_token, checkers_for_updateAccount4);
//    consumer = getConsumerByEmail(USER.username);
//    assertEquals("hello", consumer.getNickName());
//    assertEquals("1", consumer.getFirstName());
//    assertEquals("2", consumer.getLastName());
//
//    util.check_send_data(POST, prop.getResource().getUpdateUser(), USER.access_token, checkers_for_updateAccount2);
//    consumer = getConsumerByEmail(USER.username);
//    assertEquals("user", consumer.getNickName());
//    assertEquals("", consumer.getFirstName());
//    assertEquals("", consumer.getLastName());
  }

  @Test
  public void _009_account_getAccount()
  {
    logHead("Get Account");
    util.check_send_data(GET, prop.getResource().getGetAccount(),
            testConsumer.getUSER_accessToken(), checkers_for_getAccount);
  }

  @Test
  public void _010_account_publicAccount()
  {
    logHead("Public Account");

    String ANY_accessToken = testConsumer.getAnonymous_accessToken();
    String USER_accessToken = testConsumer.getUSER_accessToken();
    String ADMIN_accessToken = testConsumer.getADMIN_accessToken();

//TODO
//    util.check_send_data(GET, prop.getResource().getPublicUser() + "/9", ANY_accessToken, checkers_for_publicAccount_9);
//    util.check_send_data(GET, prop.getResource().getPublicUser() + "/9", USER_accessToken, checkers_for_publicAccount_9);
//    util.check_send_data(GET, prop.getResource().getPublicUser() + "/9", ADMIN_accessToken, checkers_for_publicAccount_9);
//    util.check_send_data(GET, prop.getResource().getPublicUser() + "/1000", ANY_accessToken, checkers_for_publicAccount_1000);
//    util.check_send_data(GET, prop.getResource().getPublicUser() + "/1000", USER_accessToken, checkers_for_publicAccount_1000);
//    util.check_send_data(GET, prop.getResource().getPublicUser() + "/1000", ADMIN_accessToken, checkers_for_publicAccount_1000);
  }

  @Test
  public void _011_account_reqConfirmEmail()
  {
    logHead("Request Confirm Email");

    String USER_accessToken = testConsumer.getUSER_accessToken();
    int type = 3;

    mailingRepository.deleteAll();

    // сheck for 'email-is-already-confirmed' error
    AuthtionConsumer consumerFromDB = getConsumerByEmail(USER_accessToken);
    assertTrue(consumerFromDB.isEmailConfirmed());
    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            USER_accessToken, checkers_for_reqConfirmEmail_isConfirmed);

    // add new request
    AuthtionTestConsumer consumer = testConsumer.of(USER, Account3_Email, Account3_Pass, testClient.getClientTrusted(), 200);
    List<AuthtionMailing> confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(0, confirmByEmail.size());
    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            consumer.access_token, checkers_for_reqConfirmEmail);

    // check that the request was success added
    confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(1, confirmByEmail.size());

    AuthtionMailing mailing = confirmByEmail.get(0);
    assertFalse(mailing.isSent());
    assertFalse(mailing.isMaxAttemptsReached());
    assertTrue(mailing.getData().length() >= 28);

    // Ok. At the moment we have 1 key and it is not yet time to send a duplicate request
    // Let's try to add one more key ==> сheck for 'delay-between-duplicate-requests' error
    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            consumer.access_token, checkers_for_reqConfirmEmail_duplicateDelay);

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
    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            consumer.access_token, checkers_for_reqConfirmEmail);

    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            consumer.access_token, checkers_for_reqConfirmEmail_duplicateDelay);

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

    assertFalse(getConsumerByEmail(Account3_Email).isEmailConfirmed());

    List<AuthtionMailing> confirmList = mailingRepository.findSentNotEmptyData(type, Account3_Email);
    assertEquals(2, confirmList.size());
    String confirmKey = confirmList.get(0).getData();
    util.check_send_data(GET, prop.getResource().getConfirmEmail(),
            null, checkers_for_confirmEmail(confirmKey));

    confirmList = mailingRepository.findSentNotEmptyData(type, Account3_Email);
    assertEquals(1, confirmList.size());
    assertTrue(getConsumerByEmail(Account3_Email).isEmailConfirmed());
  }

  @Test
  public void _013_password_changePass()
  {
    logHead("Change Password");

    performChangePass(Account3_Email, Account3_Pass, Account3_NewPass, checkers_for_changePass);

    performChangePass(Account5_Email, Account5_Pass_Decoded, Account5_NewPass_Decoded, checkers_for_changePass_2);
  }

  private void performChangePass(String email, String oldpass, String newpass, List<AuthtionTestChecker> checkers)
  {
    logHead("Change Password = " + email);

    //newpass
    //AuthtionTestConsumer.of(USER, email, newpass, testClient.getOauth2ClientTrusted(), 400);

    //oldpass
    AuthtionTestConsumer consumerTest = testConsumer.of(USER, email, oldpass, testClient.getClientTrusted(), 200);

    //change oldpass
    util.check_send_data(POST, prop.getResource().getChangePass(), consumerTest.access_token, checkers);

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpass, testClient.getOauth2ClientTrusted(), 400);

    //newpass
    consumerTest = testConsumer.of(USER, email, newpass, testClient.getClientTrusted(), 200);
    fullAuthTest(consumerTest);
  }

  @Test
  public void _014_password_restorePass()
  {
    logHead("Restore Password");

    performRestorePass(Account3_Email, Account3_NewPass, Account3_Pass, Account3_Pass);

    performRestorePass(Account5_Email, Account5_NewPass_Decoded, Account5_Pass_Decoded, Account5_Pass_Encoded);
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

    String ANY_accessToken = testConsumer.getAnonymous_accessToken();
    int type = 5;

    assertEquals(0, mailingRepository.findByTypeAndEmail(type, email).size());

    util.check_send_data(POST, prop.getResource().getReqRestorePass(),
            ANY_accessToken, checkers_for_reqRestorePass(email));

    util.check_send_data(POST, prop.getResource().getReqRestorePass(),
            ANY_accessToken, checkers_for_reqRestorePass_duplicateDelay(email));

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

    util.check_send_data(POST, prop.getResource().getReqRestorePass(),
            ANY_accessToken, checkers_for_reqRestorePass(email));

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

    String ANY_accessToken = testConsumer.getAnonymous_accessToken();
    int type = 5;

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    util.check_send_data(GET, prop.getResource().getConfirmRestorePass(),
            ANY_accessToken, checkers_for_confirmRestorePass(email, confirmByEmail.get(0).getData()));

    confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());
  }

  private void restorePass(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    logHead("Restore Password = " + email);

    String ANY_accessToken = testConsumer.getAnonymous_accessToken();
    int type = 5;

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpassDecoded, testClient.getOauth2ClientTrusted(), 200);
    //newpass
    //AuthtionTestConsumer.of(USER, email, newpassDecoded, testClient.getOauth2ClientTrusted(), 400);

    //change password
    util.check_send_data(POST, prop.getResource().getRestorePass(), ANY_accessToken,
            checkers_for_restorePass(email, newpassForCheckers, confirmByEmail.get(0).getData()));

    assertEquals(1, mailingRepository.findSentNotEmptyData(type, email).size());

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpassDecoded, testClient.getOauth2ClientTrusted(), 400);

    //newpass
    AuthtionTestConsumer consumerTest = testConsumer.of(USER, email, newpassDecoded, testClient.getClientTrusted(), 200);
    fullAuthTest(consumerTest);
  }

  private void fullAuthTest(AuthtionTestConsumer testConsumer)
  {
    util.performFullAuthTest(testConsumer);
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

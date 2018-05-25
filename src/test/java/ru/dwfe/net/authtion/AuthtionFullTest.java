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
import ru.dwfe.net.authtion.dao.AuthtionUser;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.dao.repository.AuthtionUserRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.test.AuthtionTestChecker;
import ru.dwfe.net.authtion.test.AuthtionTestClient;
import ru.dwfe.net.authtion.test.AuthtionTestConsumer;
import ru.dwfe.net.authtion.test.AuthtionTestUtil;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.dao.AuthtionUser.getNickNameFromEmail;
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
  private AuthtionUserRepository userRepository;
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
    // Was created 4 new accounts:
    //  - Account3_Email - password was passed
    //  - Account4_Email - password was not passed
    //  - Account5_Email - already encoded password was passed
    //  - Account6_Email - only email was passed
    //  - Account7_Email - values for check restrictions


    // Account3_Email
    AuthtionConsumer consumer1 = checkConsumerAfterCreate(Account3_Email);
    assertFalse(consumer1.isEmailConfirmed());
    AuthtionUser user1 = checkUser_ExactMatch(consumer1.getId(), AuthtionUser.of(
            "nobody", true,
            null, true,
            null, true,
            "sunshine", true,
            null, true,
            null, true,
            "US", true)
    );
    List<AuthtionMailing> mailing_consumer1 = mailingRepository.findByTypeAndEmail(1, consumer1.getEmail());
    assertEquals(1, mailing_consumer1.size());


    // Account4_Email
    AuthtionConsumer consumer2 = checkConsumerAfterCreate(Account4_Email);
    assertTrue(consumer2.isEmailConfirmed());
    AuthtionUser user2 = checkUser_ExactMatch(consumer2.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account4_Email), true,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true)
    );
    List<AuthtionMailing> mailing_consumer2 = mailingRepository.findByTypeAndEmail(2, consumer2.getEmail());
    assertEquals(1, mailing_consumer2.size());
    String mailing_password_consumer2 = mailing_consumer2.get(0).getData();
    Account4_Pass = mailing_password_consumer2; //for next tests
    assertTrue(mailing_password_consumer2.length() >= 9);


    // Account5_Email
    AuthtionConsumer consumer3 = checkConsumerAfterCreate(Account5_Email);
    assertFalse(consumer3.isEmailConfirmed());
    assertEquals(consumer3.getPassword(), "{bcrypt}" + Account5_Pass_Encoded);
    AuthtionUser user3 = checkUser_ExactMatch(consumer3.getId(), AuthtionUser.of(
            "hello world", true,
            null, true,
            "john", true,
            null, true,
            "F", true,
            null, true,
            "DE", true)
    );
    List<AuthtionMailing> mailing_consumer3 = mailingRepository.findByTypeAndEmail(1, consumer3.getEmail());
    assertEquals(1, mailing_consumer3.size());


    // Account6_Email
    AuthtionConsumer consumer4 = checkConsumerAfterCreate(Account6_Email);
    assertTrue(consumer4.isEmailConfirmed());
    AuthtionUser user4 = checkUser_ExactMatch(consumer4.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account6_Email), true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true)
    );
    List<AuthtionMailing> mailing_consumer4 = mailingRepository.findByTypeAndEmail(2, consumer4.getEmail());
    assertEquals(1, mailing_consumer4.size());
    String mailing_password_consumer4 = mailing_consumer4.get(0).getData();
    assertTrue(mailing_password_consumer4.length() >= 9);

    // Account7_Email
    AuthtionConsumer consumer5 = checkConsumerAfterCreate(Account7_Email);
    assertTrue(consumer5.isEmailConfirmed());
    AuthtionUser user5 = checkUser_ExactMatch(consumer5.getId(), AuthtionUser.of(
            "12345678901234567890", true,
            "12345678901234567890", true,
            "12345678901234567890", true,
            "12345678901234567890", true,
            null, true,
            null, true,
            null, true)
    );
    List<AuthtionMailing> mailing_consumer5 = mailingRepository.findByTypeAndEmail(2, consumer5.getEmail());
    assertEquals(1, mailing_consumer5.size());
    String mailing_password_consumer5 = mailing_consumer5.get(0).getData();
    assertTrue(mailing_password_consumer5.length() >= 9);
    Account7_Pass = mailing_password_consumer5;


    // Perform full auth test for New AuthtionConsumer
    fullAuthTest(testConsumer.of(USER, consumer1.getEmail(), Account3_Pass, testClient.getClientTrusted(), 200));
    fullAuthTest(testConsumer.of(USER, consumer2.getEmail(), Account4_Pass, testClient.getClientTrusted(), 200));
    fullAuthTest(testConsumer.of(USER, consumer3.getEmail(), Account5_Pass_Decoded, testClient.getClientTrusted(), 200));
    fullAuthTest(testConsumer.of(USER, consumer4.getEmail(), mailing_password_consumer4, testClient.getClientTrusted(), 200));
    fullAuthTest(testConsumer.of(USER, consumer5.getEmail(), Account7_Pass, testClient.getClientTrusted(), 200));
  }


  @Test
  public void _008_account_updateAccount()
  {
    logHead("Update Account");

    AuthtionTestConsumer tConsumer = testConsumer.of(USER, Account4_Email, Account4_Pass, testClient.getClientTrusted(), 200);
    String access_token = tConsumer.access_token;

    // init check
    AuthtionConsumer consumer = getConsumerByEmail(Account4_Email);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    AuthtionUser user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account4_Email), true,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true)
    );


    // (1) empty request
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), access_token, checkers_for_updateAccount1);
    consumer = getConsumerByEmail(Account4_Email);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account4_Email), true,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true)
    );


    // (2) change Email
    String newEmail = "helloworld@oo.com";
    consumerNotPresent(newEmail);
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), access_token, checkers_for_updateAccount2);
    consumerNotPresent(Account4_Email);
    consumer = getConsumerByEmail(newEmail);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertFalse(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account4_Email), true,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true)
    );


    // (3) since I changed email, I need to make sure that account is still functional.
    //
    // After change email /sign-out will be forced
    // sign-in again
    tConsumer = testConsumer.of(USER, newEmail, Account4_Pass, testClient.getClientTrusted(), 200);
    fullAuthTest(tConsumer);
    // sign-in again, becouse during fullAuthTest was perform sign-out
    tConsumer = testConsumer.of(USER, newEmail, Account4_Pass, testClient.getClientTrusted(), 200);
    access_token = tConsumer.access_token;


    // (4) change Email to original AND change: emailNonPublic, nickName, nickNameNonPublic
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), access_token, checkers_for_updateAccount3);
    consumerNotPresent(newEmail);
    consumer = getConsumerByEmail(Account4_Email);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertFalse(consumer.isEmailConfirmed());
    assertFalse(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            "storm", false,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true)
    );
    tConsumer = testConsumer.of(USER, Account4_Email, Account4_Pass, testClient.getClientTrusted(), 200);
    access_token = tConsumer.access_token;


    // (5) change all other fields
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), access_token, checkers_for_updateAccount4);
    consumer = getConsumerByEmail(Account4_Email);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertFalse(consumer.isEmailConfirmed());
    assertFalse(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            "storm", false,
            "adam", false,
            "newton", false,
            "dragon", false,
            "F", false,
            LocalDate.parse("1990-05-01"), false,
            "GB", false)
    );


    // Test restrictions
    tConsumer = testConsumer.of(USER, Account7_Email, Account7_Pass, testClient.getClientTrusted(), 200);
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), tConsumer.access_token, checkers_for_updateAccount5);
    consumer = getConsumerByEmail(Account7_Email);
    assertEquals(Long.valueOf(1006), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            "09876543210987654321", true,
            "09876543210987654321", true,
            "09876543210987654321", true,
            "09876543210987654321", true,
            null, true,
            null, true,
            null, true)
    );
  }


  @Test
  public void _009_account_getAccount()
  {
    logHead("Get Account");

    util.check_send_data(GET, prop.getResource().getGetAccount(),
            testConsumer.getUSER_accessToken(), checkers_for_getAccount1);

    AuthtionTestConsumer tConsumer = testConsumer.of(USER, Account4_Email, Account4_Pass, testClient.getClientTrusted(), 200);
    util.check_send_data(GET, prop.getResource().getGetAccount(),
            tConsumer.access_token, checkers_for_getAccount2);
  }


  @Test
  public void _010_account_publicAccount()
  {
    logHead("Public Account");

    String ANY_accessToken = testConsumer.getAnonymous_accessToken();
    String USER_accessToken = testConsumer.getUSER_accessToken();
    String ADMIN_accessToken = testConsumer.getADMIN_accessToken();

    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/9", ANY_accessToken, checkers_for_publicAccount_9);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/9", USER_accessToken, checkers_for_publicAccount_9);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/9", ADMIN_accessToken, checkers_for_publicAccount_9);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/1000", ANY_accessToken, checkers_for_publicAccount_1000);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/1000", USER_accessToken, checkers_for_publicAccount_1000);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/1000", ADMIN_accessToken, checkers_for_publicAccount_1000);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/1002", ANY_accessToken, checkers_for_publicAccount_1002);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/1002", USER_accessToken, checkers_for_publicAccount_1002);
    util.check_send_data(GET, prop.getResource().getPublicAccount() + "/1002", ADMIN_accessToken, checkers_for_publicAccount_1002);
  }


  @Test
  public void _011_account_reqConfirmEmail()
  {
    logHead("Request Confirm Email");

    AuthtionTestConsumer USER_test = testConsumer.getUSER();
    String USER_accessToken = USER_test.access_token;
    long timeToWait = TimeUnit.MILLISECONDS.toSeconds(prop.getScheduledTaskMailing().getCollectFromDbInterval()) * 2;
    int type = 3;

    mailingRepository.deleteAll();

    // сheck for 'email-is-already-confirmed' error
    AuthtionConsumer consumerFromDB = getConsumerByEmail(USER_test.username);
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
      log.info("Please wait {} seconds...", timeToWait);
      TimeUnit.SECONDS.sleep(timeToWait);
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
      log.info("Please wait {} seconds...", timeToWait);
      TimeUnit.SECONDS.sleep(timeToWait);
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
    long timeToWait = TimeUnit.MILLISECONDS.toSeconds(prop.getScheduledTaskMailing().getCollectFromDbInterval()) * 2;
    int type = 5;

    assertEquals(0, mailingRepository.findByTypeAndEmail(type, email).size());

    util.check_send_data(POST, prop.getResource().getReqRestorePass(),
            ANY_accessToken, checkers_for_reqRestorePass(email));

    util.check_send_data(POST, prop.getResource().getReqRestorePass(),
            ANY_accessToken, checkers_for_reqRestorePass_duplicateDelay(email));

    try
    {
      log.info("Please wait {} seconds...", timeToWait);
      TimeUnit.SECONDS.sleep(timeToWait);
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
      log.info("Please wait {} seconds...", timeToWait);
      TimeUnit.SECONDS.sleep(timeToWait);
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

  private void consumerNotPresent(String email)
  {
    Optional<AuthtionConsumer> consumerFromDBOpt = consumerService.findByEmail(email);
    assertFalse(consumerFromDBOpt.isPresent());
  }

  private AuthtionUser getUserById(Long id)
  {
    Optional<AuthtionUser> userByIdOpt = userRepository.findById(id);
    assertTrue(userByIdOpt.isPresent());
    return userByIdOpt.get();
  }

  private AuthtionConsumer checkConsumerAfterCreate(String email)
  {
    AuthtionConsumer consumer = getConsumerByEmail(email);

    assertTrue(consumer.getId() > 999);
    assertNotEquals(null, consumer.getCreatedOn());
    assertNotEquals(null, consumer.getUpdatedOn());

    Collection<? extends GrantedAuthority> authorities_consumer1 = consumer.getAuthorities();
    assertEquals(1, authorities_consumer1.size());
    assertEquals("USER", authorities_consumer1.iterator().next().getAuthority());

    assertTrue(consumer.isEmailNonPublic());

    assertTrue(consumer.isAccountNonExpired());
    assertTrue(consumer.isCredentialsNonExpired());
    assertTrue(consumer.isAccountNonLocked());
    assertTrue(consumer.isEnabled());

    return consumer;
  }

  private AuthtionUser checkUser_ExactMatch(Long id, AuthtionUser tUser)
  {
    AuthtionUser user = getUserById(id);
    assertNotEquals(null, user.getUpdatedOn());

    String tNickName = tUser.getNickName();
    Boolean tNickNameNonPublic = tUser.getNickNameNonPublic();

    String tFirstName = tUser.getFirstName();
    Boolean tFirstNameNonPublic = tUser.getFirstNameNonPublic();

    String tMiddleName = tUser.getMiddleName();
    Boolean tMiddleNameNonPublic = tUser.getMiddleNameNonPublic();

    String tLastName = tUser.getLastName();
    Boolean tLastNameNonPublic = tUser.getLastNameNonPublic();

    String tGender = tUser.getGender();
    Boolean tGenderNonPublic = tUser.getGenderNonPublic();

    LocalDate tDateOfBirth = tUser.getDateOfBirth();
    Boolean tDateOfBirthNonPublic = tUser.getDateOfBirthNonPublic();

    String tCountry = tUser.getCountry();
    Boolean tCountryNonPublic = tUser.getCountryNonPublic();

    assertEquals(tNickName, user.getNickName());
    assertEquals(tNickNameNonPublic, user.getNickNameNonPublic());

    assertEquals(tFirstName, user.getFirstName());
    assertEquals(tFirstNameNonPublic, user.getFirstNameNonPublic());

    assertEquals(tMiddleName, user.getMiddleName());
    assertEquals(tMiddleNameNonPublic, user.getMiddleNameNonPublic());

    assertEquals(tLastName, user.getLastName());
    assertEquals(tLastNameNonPublic, user.getLastNameNonPublic());

    assertEquals(tGender, user.getGender());
    assertEquals(tGenderNonPublic, user.getGenderNonPublic());

    assertEquals(tDateOfBirth, user.getDateOfBirth());
    assertEquals(tDateOfBirthNonPublic, user.getDateOfBirthNonPublic());

    assertEquals(tCountry, user.getCountry());
    assertEquals(tCountryNonPublic, user.getCountryNonPublic());

    return user;
  }

  private static void logHead(String who)
  {
    log.info("\n=============================="
            + "\n {} "
            + "\n------------------------------", who);
  }

  private static final Logger log = LoggerFactory.getLogger(AuthtionFullTest.class);
}

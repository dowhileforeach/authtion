package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import ru.dwfe.net.authtion.config.AuthtionConfigProperties;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.AuthtionUser;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.dao.repository.AuthtionUserRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.test.AuthtionTestChecker;
import ru.dwfe.net.authtion.test.AuthtionTestClient;
import ru.dwfe.net.authtion.test.AuthtionTestConsumer;
import ru.dwfe.net.authtion.test.AuthtionTestUtil;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

    var consumer = testConsumer.getUSER();
    auth_test_access_tokens.add(consumer.access_token);

    fullAuthTest(consumer);
  }

  @Test
  public void _002_auth_ADMIN()
  {
    logHead("ADMIN");

    var consumer = testConsumer.getADMIN();
    auth_test_access_tokens.add(consumer.access_token);

    fullAuthTest(consumer);
  }

  @Test
  public void _003_auth_ANY()
  {
    logHead("ANY");

    var consumer = testConsumer.getAnonymous();
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
    var consumer1 = checkConsumerAfterCreate(Account3_Email);
    assertFalse(consumer1.isEmailConfirmed());
    var user1 = checkUser_ExactMatch(consumer1.getId(), AuthtionUser.of(
            "nobody", true,
            null, true,
            null, true,
            "sunshine", true,
            null, true,
            null, true,
            "US", true,
            "Dallas", true,
            null, true)
    );
    var mailing_consumer1 = mailingRepository.findByTypeAndEmail(1, consumer1.getEmail());
    assertEquals(1, mailing_consumer1.size());


    // Account4_Email
    var consumer2 = checkConsumerAfterCreate(Account4_Email);
    assertTrue(consumer2.isEmailConfirmed());
    var user2 = checkUser_ExactMatch(consumer2.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account4_Email), true,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true,
            null, true,
            "Home Ltd.", true)
    );
    var mailing_consumer2 = mailingRepository.findByTypeAndEmail(2, consumer2.getEmail());
    assertEquals(1, mailing_consumer2.size());
    var mailing_password_consumer2 = mailing_consumer2.get(0).getData();
    Account4_Pass = mailing_password_consumer2; //for next tests
    assertTrue(mailing_password_consumer2.length() >= 9);


    // Account5_Email
    var consumer3 = checkConsumerAfterCreate(Account5_Email);
    assertFalse(consumer3.isEmailConfirmed());
    assertEquals(consumer3.getPassword(), "{bcrypt}" + Account5_Pass_Encoded);
    var user3 = checkUser_ExactMatch(consumer3.getId(), AuthtionUser.of(
            "hello world", true,
            null, true,
            "john", true,
            null, true,
            "F", true,
            null, true,
            "DE", true,
            null, true,
            null, true)
    );
    var mailing_consumer3 = mailingRepository.findByTypeAndEmail(1, consumer3.getEmail());
    assertEquals(1, mailing_consumer3.size());


    // Account6_Email
    var consumer4 = checkConsumerAfterCreate(Account6_Email);
    assertTrue(consumer4.isEmailConfirmed());
    var user4 = checkUser_ExactMatch(consumer4.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account6_Email), true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true)
    );
    var mailing_consumer4 = mailingRepository.findByTypeAndEmail(2, consumer4.getEmail());
    assertEquals(1, mailing_consumer4.size());
    var mailing_password_consumer4 = mailing_consumer4.get(0).getData();
    assertTrue(mailing_password_consumer4.length() >= 9);

    // Account7_Email
    var consumer5 = checkConsumerAfterCreate(Account7_Email);
    assertTrue(consumer5.isEmailConfirmed());
    var user5 = checkUser_ExactMatch(consumer5.getId(), AuthtionUser.of(
            "12345678901234567890", true,
            "12345678901234567890", true,
            "12345678901234567890", true,
            "12345678901234567890", true,
            "M", true,
            LocalDate.parse("1999-11-22"), true,
            "US", true,
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true,
            "1234567890123456789012345678901234567890123456789012345678901234567890123456789012345678901234567890", true)
    );
    var mailing_consumer5 = mailingRepository.findByTypeAndEmail(2, consumer5.getEmail());
    assertEquals(1, mailing_consumer5.size());
    var mailing_password_consumer5 = mailing_consumer5.get(0).getData();
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

    var tConsumer = testConsumer.of(USER, Account4_Email, Account4_Pass, testClient.getClientTrusted(), 200);
    var access_token = tConsumer.access_token;

    // init check
    var consumer = getConsumerByEmail(Account4_Email);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    var user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            getNickNameFromEmail(Account4_Email), true,
            "ozon", true,
            null, true,
            null, true,
            "M", true,
            LocalDate.parse("1980-11-27"), true,
            null, true,
            null, true,
            "Home Ltd.", true)
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
            null, true,
            null, true,
            "Home Ltd.", true)
    );


    // (2) change all fields
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), access_token, checkers_for_updateAccount2);
    consumer = getConsumerByEmail(Account4_Email);
    assertEquals(Long.valueOf(1002), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertFalse(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            "storm", false,
            "adam", false,
            "newton", false,
            "dragon", false,
            "F", false,
            LocalDate.parse("1990-05-01"), false,
            "GB", false,
            "London", false,
            "Company", false)
    );


    // (3) test restrictions
    tConsumer = testConsumer.of(USER, Account7_Email, Account7_Pass, testClient.getClientTrusted(), 200);
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), tConsumer.access_token, checkers_for_updateAccount3);
    consumer = getConsumerByEmail(Account7_Email);
    assertEquals(Long.valueOf(1006), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            "09876543210987654321", true,
            "09876543210987654321", true,
            "09876543210987654321", true,
            "09876543210987654321", true,
            "M", true,
            LocalDate.parse("1999-11-22"), true,
            "US", true,
            "0987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321", true,
            "0987654321098765432109876543210987654321098765432109876543210987654321098765432109876543210987654321", true)
    );


    // (4) test null's
    util.check_send_data(POST, prop.getResource().getUpdateAccount(), tConsumer.access_token, checkers_for_updateAccount4);
    consumer = getConsumerByEmail(Account7_Email);
    assertEquals(Long.valueOf(1006), consumer.getId());
    assertTrue(consumer.isEmailConfirmed());
    assertTrue(consumer.isEmailNonPublic());
    user = checkUser_ExactMatch(consumer.getId(), AuthtionUser.of(
            null, true,
            null, true,
            null, true,
            null, true,
            null, true,
            null, true,
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

    var tConsumer = testConsumer.of(USER, Account4_Email, Account4_Pass, testClient.getClientTrusted(), 200);
    util.check_send_data(GET, prop.getResource().getGetAccount(),
            tConsumer.access_token, checkers_for_getAccount2);
  }


  @Test
  public void _010_account_publicAccount()
  {
    logHead("Public Account");

    var ANY_accessToken = testConsumer.getAnonymous_accessToken();
    var USER_accessToken = testConsumer.getUSER_accessToken();
    var ADMIN_accessToken = testConsumer.getADMIN_accessToken();

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

    var USER_test = testConsumer.getUSER();
    var USER_accessToken = USER_test.access_token;
    var timeToWait = TimeUnit.MILLISECONDS.toSeconds(prop.getScheduledTaskMailing().getCollectFromDbInterval()) * 2;
    var type = 3;

    mailingRepository.deleteAll();

    // сheck for 'email-is-already-confirmed' error
    var consumerFromDB = getConsumerByEmail(USER_test.username);
    assertTrue(consumerFromDB.isEmailConfirmed());
    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            USER_accessToken, checkers_for_reqConfirmEmail_isConfirmed);

    // add new request
    var consumer = testConsumer.of(USER, Account3_Email, Account3_Pass, testClient.getClientTrusted(), 200);
    var confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(0, confirmByEmail.size());
    util.check_send_data(GET, prop.getResource().getReqConfirmEmail(),
            consumer.access_token, checkers_for_reqConfirmEmail);

    // check that the request was success added
    confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(1, confirmByEmail.size());

    var mailing = confirmByEmail.get(0);
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
    var confirmByEmailOpt = mailingRepository.findLastSentNotEmptyData(type, consumer.username);
    assertTrue(confirmByEmailOpt.isPresent()); // new key was success added and sent
    mailing = confirmByEmailOpt.get();
    assertFalse(mailing.isMaxAttemptsReached());
    var alreadySentKeyOld = mailing.getData();

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

    var type = 3;

    assertFalse(getConsumerByEmail(Account3_Email).isEmailConfirmed());

    var confirmList = mailingRepository.findSentNotEmptyData(type, Account3_Email);
    assertEquals(2, confirmList.size());
    var confirmKey = confirmList.get(0).getData();
    util.check_send_data(POST, prop.getResource().getConfirmEmail(),
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

  private void performChangePass(String email, String curpass, String newpass, List<AuthtionTestChecker> checkers)
  {
    logHead("Change Password = " + email);

    //newpass
    //AuthtionTestConsumer.of(USER, email, newpass, testClient.getOauth2ClientTrusted(), 400);

    //curpass
    var consumerTest = testConsumer.of(USER, email, curpass, testClient.getClientTrusted(), 200);

    //change curpass
    util.check_send_data(POST, prop.getResource().getChangePass(), consumerTest.access_token, checkers);

    //curpass
    //AuthtionTestConsumer.of(USER, email, curpass, testClient.getOauth2ClientTrusted(), 400);

    //newpass
    consumerTest = testConsumer.of(USER, email, newpass, testClient.getClientTrusted(), 200);
    fullAuthTest(consumerTest);
  }

  @Test
  public void _014_password_resetPass()
  {
    logHead("Reset Password");

    performResetPass(Account3_Email, Account3_NewPass, Account3_Pass, Account3_Pass);
    performResetPass(Account5_Email, Account5_NewPass_Decoded, Account5_Pass_Decoded, Account5_Pass_Encoded);
  }

  private void performResetPass(String email, String curpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    reqResetPass(email);
    confirmResetPass(email);
    resetPass(email, curpassDecoded, newpassDecoded, newpassForCheckers);
  }

  private void reqResetPass(String email)
  {
    logHead("Request Reset Password = " + email);

    var ANY_accessToken = testConsumer.getAnonymous_accessToken();
    var timeToWait = TimeUnit.MILLISECONDS.toSeconds(prop.getScheduledTaskMailing().getCollectFromDbInterval()) * 2;
    var type = 5;

    assertEquals(0, mailingRepository.findByTypeAndEmail(type, email).size());

    util.check_send_data(POST, prop.getResource().getReqResetPass(),
            ANY_accessToken, checkers_for_reqResetPass(email));

    util.check_send_data(POST, prop.getResource().getReqResetPass(),
            ANY_accessToken, checkers_for_reqResetPass_duplicateDelay(email));

    try
    {
      log.info("Please wait {} seconds...", timeToWait);
      TimeUnit.SECONDS.sleep(timeToWait);
    }
    catch (InterruptedException ignored)
    {
    }
    var confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(1, confirmByEmail.size());
    assertFalse(confirmByEmail.get(0).isMaxAttemptsReached());
    assertTrue(confirmByEmail.get(0).getData().length() >= 28);

    util.check_send_data(POST, prop.getResource().getReqResetPass(),
            ANY_accessToken, checkers_for_reqResetPass(email));

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

  private void confirmResetPass(String email)
  {
    logHead("Confirm Reset Password = " + email);

    var ANY_accessToken = testConsumer.getAnonymous_accessToken();
    var type = 5;

    var confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    util.check_send_data(POST, prop.getResource().getConfirmResetPass(),
            ANY_accessToken, checkers_for_confirmResetPass(email, confirmByEmail.get(0).getData()));

    confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());
  }

  private void resetPass(String email, String curpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    logHead("Reset Password = " + email);

    var ANY_accessToken = testConsumer.getAnonymous_accessToken();
    var type = 5;

    var confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    //curpass
    //AuthtionTestConsumer.of(USER, email, curpassDecoded, testClient.getOauth2ClientTrusted(), 200);
    //newpass
    //AuthtionTestConsumer.of(USER, email, newpassDecoded, testClient.getOauth2ClientTrusted(), 400);

    //change password
    util.check_send_data(POST, prop.getResource().getResetPass(), ANY_accessToken,
            checkers_for_resetPass(email, newpassForCheckers, confirmByEmail.get(0).getData()));

    assertEquals(1, mailingRepository.findSentNotEmptyData(type, email).size());

    //curpass
    //AuthtionTestConsumer.of(USER, email, curpassDecoded, testClient.getOauth2ClientTrusted(), 400);

    //newpass
    var consumerTest = testConsumer.of(USER, email, newpassDecoded, testClient.getClientTrusted(), 200);
    fullAuthTest(consumerTest);
  }

  private void fullAuthTest(AuthtionTestConsumer testConsumer)
  {
    util.performFullAuthTest(testConsumer);
    mailingRepository.deleteAll();
  }

  private AuthtionConsumer getConsumerByEmail(String email)
  {
    var consumerFromDBOpt = consumerService.findByEmail(email);
    assertTrue(consumerFromDBOpt.isPresent());
    return consumerFromDBOpt.get();
  }

  private void consumerNotPresent(String email)
  {
    var consumerFromDBOpt = consumerService.findByEmail(email);
    assertFalse(consumerFromDBOpt.isPresent());
  }

  private AuthtionUser getUserById(Long id)
  {
    var userByIdOpt = userRepository.findById(id);
    assertTrue(userByIdOpt.isPresent());
    return userByIdOpt.get();
  }

  private AuthtionConsumer checkConsumerAfterCreate(String email)
  {
    var consumer = getConsumerByEmail(email);

    assertTrue(consumer.getId() > 999);
    assertNotEquals(null, consumer.getCreatedOn());
    assertNotEquals(null, consumer.getUpdatedOn());

    var authorities_consumer1 = consumer.getAuthorities();
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
    var user = getUserById(id);
    assertNotEquals(null, user.getUpdatedOn());

    var tNickName = tUser.getNickName();
    var tNickNameNonPublic = tUser.getNickNameNonPublic();

    var tFirstName = tUser.getFirstName();
    var tFirstNameNonPublic = tUser.getFirstNameNonPublic();

    var tMiddleName = tUser.getMiddleName();
    var tMiddleNameNonPublic = tUser.getMiddleNameNonPublic();

    var tLastName = tUser.getLastName();
    var tLastNameNonPublic = tUser.getLastNameNonPublic();

    var tGender = tUser.getGender();
    var tGenderNonPublic = tUser.getGenderNonPublic();

    var tDateOfBirth = tUser.getDateOfBirth();
    var tDateOfBirthNonPublic = tUser.getDateOfBirthNonPublic();

    var tCountry = tUser.getCountry();
    var tCountryNonPublic = tUser.getCountryNonPublic();

    var tCity = tUser.getCity();
    var tCityNonPublic = tUser.getCityNonPublic();

    var tCompany = tUser.getCompany();
    var tCompanyNonPublic = tUser.getCompanyNonPublic();

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

    assertEquals(tCity, user.getCity());
    assertEquals(tCityNonPublic, user.getCityNonPublic());

    assertEquals(tCompany, user.getCompany());
    assertEquals(tCompanyNonPublic, user.getCompanyNonPublic());

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

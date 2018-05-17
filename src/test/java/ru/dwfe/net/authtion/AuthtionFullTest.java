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
import ru.dwfe.net.authtion.dao.AuthtionConsumer;
import ru.dwfe.net.authtion.dao.AuthtionMailing;
import ru.dwfe.net.authtion.dao.repository.AuthtionMailingRepository;
import ru.dwfe.net.authtion.service.AuthtionConsumerService;
import ru.dwfe.net.authtion.test.AuthtionTestChecker;
import ru.dwfe.net.authtion.test.AuthtionTestConsumer;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.AuthtionGlobal.*;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.ADMIN;
import static ru.dwfe.net.authtion.test.AuthtionTestAuthorityLevel.USER;
import static ru.dwfe.net.authtion.test.AuthtionTestGlobalVariables.*;
import static ru.dwfe.net.authtion.test.AuthtionTestResourceAccessingType.USUAL;
import static ru.dwfe.net.authtion.test.AuthtionTestUtil.*;
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
  AuthtionConsumerService consumerService;

  @Autowired
  AuthtionMailingRepository mailingRepository;

  @Test
  public void _001_auth_USER()
  {
    logHead("USER");

    AuthtionTestConsumer testConsumer = USER_consumer;
    auth_test_access_tokens.add(testConsumer.access_token);

    fullAuthTest(testConsumer);
  }

  @Test
  public void _002_auth_ADMIN()
  {
    logHead("ADMIN");

    AuthtionTestConsumer testConsumer = ADMIN_consumer;
    auth_test_access_tokens.add(testConsumer.access_token);

    fullAuthTest(testConsumer);
  }

  @Test
  public void _003_auth_ANY()
  {
    logHead("ANY");

    AuthtionTestConsumer testConsumer = ANY_consumer;
    performResourceAccessing(testConsumer.access_token, testConsumer.level, USUAL);
  }

  @Test
  public void _004_auth_different_access_tokens()
  {
    logHead("list of Access Tokens");
    log.info("\n\n{}", auth_test_access_tokens.stream().collect(Collectors.joining("\n")));

    assertEquals(TOTAL_ACCESS_TOKEN_COUNT, auth_test_access_tokens.size());
  }

  @Test
  public void _005_consumer_checkConsumerEmail()
  {
    logHead("Check AuthtionConsumer E-mail");
    check_send_data(POST, resource_checkConsumerEmail, ANY_consumer.access_token, checkers_for_checkConsumerEmail);
  }

  @Test
  public void _006_consumer_checkConsumerPass()
  {
    logHead("Check AuthtionConsumer Pass");
    check_send_data(POST, resource_checkConsumerPass, ANY_consumer.access_token, checkers_for_checkConsumerPass);
  }

  @Test
  public void _007_consumer_createConsumer()
  {
    logHead("Create AuthtionConsumer");

    check_send_data(POST, resource_createConsumer, ANY_consumer.access_token, checkers_for_createConsumer());
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

    assertEquals("nobody", consumer1.getNickName());
    assertTrue(consumer1.getFirstName().isEmpty());
    assertEquals("sunshine", consumer1.getLastName());
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

    assertEquals(AuthtionConsumer.prepareStringField(AuthtionConsumer.getNickNameFromEmail(consumer2.getEmail()), 20), consumer2.getNickName());
    assertEquals("ozon", consumer2.getFirstName());
    assertTrue(consumer2.getLastName().isEmpty());
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

    assertEquals("hello world", consumer3.getNickName());
    assertTrue(consumer3.getFirstName().isEmpty());
    assertTrue(consumer3.getLastName().isEmpty());
    assertTrue(consumer3.isAccountNonExpired());
    assertTrue(consumer3.isAccountNonLocked());
    assertTrue(consumer3.isCredentialsNonExpired());
    assertTrue(consumer3.isEnabled());
    assertFalse(consumer3.isEmailConfirmed());

    List<AuthtionMailing> mailing_consumer3 = mailingRepository.findByTypeAndEmail(1, consumer3.getEmail());
    assertEquals(1, mailing_consumer3.size());

    // Perform full auth test for New AuthtionConsumer
    fullAuthTest(AuthtionTestConsumer.of(USER, consumer1.getEmail(), PASS_NEW_Consumer, client_TRUSTED, 200));
    fullAuthTest(AuthtionTestConsumer.of(USER, consumer2.getEmail(), mailing_password_consumer2, client_TRUSTED, 200));
    fullAuthTest(AuthtionTestConsumer.of(USER, consumer3.getEmail(), PASS_FOR_EMAIL_3_Consumer_Decoded, client_TRUSTED, 200));
  }

  @Test
  public void _008_consumer_updateConsumer()
  {
    logHead("AuthtionConsumer Update");

    USER_consumer = AuthtionTestConsumer.of(USER, "test2@dwfe.ru", "test22", client_TRUSTED, 200);
    ADMIN_consumer = AuthtionTestConsumer.of(ADMIN, "test1@dwfe.ru", "test11", client_UNTRUSTED, 200);

    AuthtionConsumer consumer = getConsumerByEmail(USER_consumer.username);
    assertEquals("test2", consumer.getNickName());
    assertEquals("", consumer.getFirstName());
    assertEquals("", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer1);
    consumer = getConsumerByEmail(USER_consumer.username);
    assertEquals("test2", consumer.getNickName());
    assertEquals("", consumer.getFirstName());
    assertEquals("", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer5);
    consumer = getConsumerByEmail(USER_consumer.username);
    assertEquals("good", consumer.getNickName());
    assertEquals("alto", consumer.getFirstName());
    assertEquals("smith", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer3);
    consumer = getConsumerByEmail(USER_consumer.username);
    assertEquals("hello", consumer.getNickName());
    assertEquals("1", consumer.getFirstName());
    assertEquals("smith", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer4);
    consumer = getConsumerByEmail(USER_consumer.username);
    assertEquals("hello", consumer.getNickName());
    assertEquals("1", consumer.getFirstName());
    assertEquals("2", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer2);
    consumer = getConsumerByEmail(USER_consumer.username);
    assertEquals("user", consumer.getNickName());
    assertEquals("", consumer.getFirstName());
    assertEquals("", consumer.getLastName());
  }

  @Test
  public void _009_consumer_getConsumerData()
  {
    logHead("AuthtionConsumer Data");
    check_send_data(GET, resource_getConsumerData, USER_consumer.access_token, checkers_for_getConsumerData);
  }

  @Test
  public void _010_consumer_publicConsumer()
  {
    logHead("Public AuthtionConsumer");
    check_send_data(GET, resource_publicConsumer + "/9", ANY_consumer.access_token, checkers_for_publicConsumer_9);
    check_send_data(GET, resource_publicConsumer + "/9", USER_consumer.access_token, checkers_for_publicConsumer_9);
    check_send_data(GET, resource_publicConsumer + "/9", ADMIN_consumer.access_token, checkers_for_publicConsumer_9);
    check_send_data(GET, resource_publicConsumer + "/1000", ANY_consumer.access_token, checkers_for_publicConsumer_1);
    check_send_data(GET, resource_publicConsumer + "/1000", USER_consumer.access_token, checkers_for_publicConsumer_1);
    check_send_data(GET, resource_publicConsumer + "/1000", ADMIN_consumer.access_token, checkers_for_publicConsumer_1);
  }

  @Test
  public void _011_consumer_requestConfirmConsumerEmail()
  {
    logHead("Request Confirm Email");

    int type = 3;

    mailingRepository.deleteAll();

    // сheck for 'email-is-already-confirmed' error
    AuthtionConsumer consumerFromDB = getConsumerByEmail(USER_consumer.username);
    assertTrue(consumerFromDB.isEmailConfirmed());
    check_send_data(GET, resource_reqConfirmConsumerEmail, USER_consumer.access_token,
            checkers_for_reqConfirmConsumerEmail_isConfirmed);

    // add new request
    AuthtionTestConsumer consumer = AuthtionTestConsumer.of(USER, EMAIL_NEW_Consumer, PASS_NEW_Consumer, client_TRUSTED, 200);
    List<AuthtionMailing> confirmByEmail = mailingRepository.findByTypeAndEmail(type, consumer.username);
    assertEquals(0, confirmByEmail.size());
    check_send_data(GET, resource_reqConfirmConsumerEmail, consumer.access_token,
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
    check_send_data(GET, resource_reqConfirmConsumerEmail, consumer.access_token,
            checkers_for_reqConfirmConsumerEmail_duplicateDelay);

    try
    {
      log.info("Please wait 8 seconds...");
      TimeUnit.SECONDS.sleep(8);
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
    check_send_data(GET, resource_reqConfirmConsumerEmail, consumer.access_token,
            checkers_for_reqConfirmConsumerEmail);

    check_send_data(GET, resource_reqConfirmConsumerEmail, consumer.access_token,
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
  public void _012_consumer_confirmConsumerEmail()
  {
    logHead("Confirm Email");

    int type = 3;

    assertFalse(getConsumerByEmail(EMAIL_NEW_Consumer).isEmailConfirmed());

    List<AuthtionMailing> confirmList = mailingRepository.findSentNotEmptyData(type, EMAIL_NEW_Consumer);
    assertEquals(2, confirmList.size());
    String confirmKey = confirmList.get(0).getData();
    check_send_data(GET, resource_confirmConsumerEmail, null, checkers_for_confirmConsumerEmail(confirmKey));

    confirmList = mailingRepository.findSentNotEmptyData(type, EMAIL_NEW_Consumer);
    assertEquals(1, confirmList.size());
    assertTrue(getConsumerByEmail(EMAIL_NEW_Consumer).isEmailConfirmed());
  }

  @Test
  public void _013_consumer_changeConsumerPass()
  {
    changeConsumerPass(EMAIL_NEW_Consumer, PASS_NEW_Consumer, NEWPASS_NEW_Consumer, checkers_for_changeConsumerPass);

    changeConsumerPass(EMAIL_3_NEW_Consumer, PASS_FOR_EMAIL_3_Consumer_Decoded, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, checkers_for_changeConsumerPass_3);
  }

  private void changeConsumerPass(String email, String oldpass, String newpass, List<AuthtionTestChecker> checkers)
  {
    logHead("Change AuthtionConsumer Pass = " + email);

    //newpass
    //AuthtionTestConsumer.of(USER, email, newpass, client_TRUSTED, 400);

    //oldpass
    AuthtionTestConsumer consumerTest = AuthtionTestConsumer.of(USER, email, oldpass, client_TRUSTED, 200);

    //change oldpass
    check_send_data(POST, resource_changeConsumerPass, consumerTest.access_token, checkers);

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpass, client_TRUSTED, 400);

    //newpass
    consumerTest = AuthtionTestConsumer.of(USER, email, newpass, client_TRUSTED, 200);
    fullAuthTest(consumerTest);
  }

  @Test
  public void _014_consumer_restorePassword()
  {
    restorePassword(EMAIL_NEW_Consumer, NEWPASS_NEW_Consumer, PASS_NEW_Consumer, PASS_NEW_Consumer);

    restorePassword(EMAIL_3_NEW_Consumer, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, PASS_FOR_EMAIL_3_Consumer_Decoded, PASS_FOR_EMAIL_3_Consumer_Encoded);
  }

  private void restorePassword(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    reqRestoreConsumerPass(email);
    confirmRestoreConsumerPass(email);
    restoreConsumerPass(email, oldpassDecoded, newpassDecoded, newpassForCheckers);
  }

  private void reqRestoreConsumerPass(String email)
  {
    logHead("Request Restore AuthtionConsumer Password = " + email);

    int type = 5;

    assertEquals(0, mailingRepository.findByTypeAndEmail(type, email).size());

    check_send_data(POST, resource_reqRestoreConsumerPass, ANY_consumer.access_token,
            checkers_for_reqRestoreConsumerPass(email));

    check_send_data(POST, resource_reqRestoreConsumerPass, ANY_consumer.access_token,
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

    check_send_data(POST, resource_reqRestoreConsumerPass, ANY_consumer.access_token,
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

  private void confirmRestoreConsumerPass(String email)
  {
    logHead("Confirm Restore AuthtionConsumer Password = " + email);

    int type = 5;

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    check_send_data(GET, resource_confirmRestoreConsumerPass, ANY_consumer.access_token, checkers_for_confirmRestoreConsumerPass(email, confirmByEmail.get(0).getData()));

    confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());
  }

  private void restoreConsumerPass(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
    logHead("Restore AuthtionConsumer Password = " + email);

    int type = 5;

    List<AuthtionMailing> confirmByEmail = mailingRepository.findSentNotEmptyData(type, email);
    assertEquals(2, confirmByEmail.size());

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpassDecoded, client_TRUSTED, 200);
    //newpass
    //AuthtionTestConsumer.of(USER, email, newpassDecoded, client_TRUSTED, 400);

    //change password
    check_send_data(POST, resource_restoreConsumerPass, ANY_consumer.access_token,
            checkers_for_restoreConsumerPass(email, newpassForCheckers, confirmByEmail.get(0).getData()));

    assertEquals(1, mailingRepository.findSentNotEmptyData(type, email).size());

    //oldpass
    //AuthtionTestConsumer.of(USER, email, oldpassDecoded, client_TRUSTED, 400);

    //newpass
    AuthtionTestConsumer consumerTest = AuthtionTestConsumer.of(USER, email, newpassDecoded, client_TRUSTED, 200);
    fullAuthTest(consumerTest);
  }

  private void fullAuthTest(AuthtionTestConsumer testConsumer)
  {
    performFullAuthTest(testConsumer);
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

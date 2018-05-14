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
import ru.dwfe.net.authtion.dao.Consumer;
import ru.dwfe.net.authtion.dao.Mailing;
import ru.dwfe.net.authtion.dao.repository.MailingRepository;
import ru.dwfe.net.authtion.service.ConsumerService;
import ru.dwfe.net.authtion.test_util.Checker;
import ru.dwfe.net.authtion.test_util.ConsumerTest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.Global.*;
import static ru.dwfe.net.authtion.test_util.AuthorityLevel.USER;
import static ru.dwfe.net.authtion.test_util.UtilTest.check_send_data;
import static ru.dwfe.net.authtion.test_util.UtilTest.performFullAuthTest;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;
import static ru.dwfe.net.authtion.test_util.Variables_for_ConsumerPassword_CRU_Test.*;

//
//  https://spring.io/guides/gs/testing-web/
//

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsumerPassword_CRU_Test
{
  @Autowired
  ConsumerService consumerService;

  @Autowired
  MailingRepository mailingRepository;

  @Test
  public void _01_checkConsumerEmail()
  {
    logHead("Check Consumer E-mail");
    check_send_data(POST, resource_checkConsumerEmail, ANY_consumer.access_token, checkers_for_checkConsumerEmail);
  }

  @Test
  public void _02_checkConsumerPass()
  {
    logHead("Check Consumer Pass");
    check_send_data(POST, resource_checkConsumerPass, ANY_consumer.access_token, checkers_for_checkConsumerPass);
  }

  @Test
  public void _03_createConsumer()
  {
    logHead("Create Consumer");

    mailingRepository.deleteAll();
    check_send_data(POST, resource_createConsumer, ANY_consumer.access_token, checkers_for_createConsumer());
    //
    // Was created 3 new consumers:
    //  - EMAIL_NEW_Consumer   - password was passed
    //  - EMAIL_2_NEW_Consumer - password was not passed
    //  - EMAIL_3_NEW_Consumer - already encoded password was passed


    // EMAIL_NEW_Consumer
    Optional<Consumer> consumer1ByEmail = getConsumerByEmail(EMAIL_NEW_Consumer);
    assertTrue(consumer1ByEmail.isPresent());

    Consumer consumer1 = consumer1ByEmail.get();
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

    List<Mailing> mailing_consumer1 = mailingRepository.findByEmail(consumer1.getEmail());
    assertEquals(1, mailing_consumer1.size());
    assertEquals(1, mailing_consumer1.get(0).getType());


    // EMAIL_2_NEW_Consumer
    Optional<Consumer> consumer2ByEmail = getConsumerByEmail(EMAIL_2_NEW_Consumer);
    assertTrue(consumer2ByEmail.isPresent());

    Consumer consumer2 = consumer2ByEmail.get();
    assertTrue(consumer2.getId() > 999);

    Collection<? extends GrantedAuthority> authorities_consumer2 = consumer2.getAuthorities();
    assertEquals(1, authorities_consumer2.size());
    assertEquals("USER", authorities_consumer2.iterator().next().getAuthority());

    assertEquals(Consumer.prepareStringField(Consumer.getNickNameFromEmail(consumer2.getEmail()), 20), consumer2.getNickName());
    assertEquals("ozon", consumer2.getFirstName());
    assertTrue(consumer2.getLastName().isEmpty());
    assertTrue(consumer2.isAccountNonExpired());
    assertTrue(consumer2.isAccountNonLocked());
    assertTrue(consumer2.isCredentialsNonExpired());
    assertTrue(consumer2.isEnabled());
    assertTrue(consumer2.isEmailConfirmed());

    List<Mailing> mailing_consumer2 = mailingRepository.findByEmail(consumer2.getEmail());
    assertEquals(1, mailing_consumer2.size());
    assertEquals(2, mailing_consumer2.get(0).getType());

    String mailing_password_consumer2 = mailing_consumer2.get(0).getData();
    assertTrue(mailing_password_consumer2.length() >= 9);


    // EMAIL_3_NEW_Consumer
    Optional<Consumer> consumer3ByEmail = getConsumerByEmail(EMAIL_3_NEW_Consumer);
    assertTrue(consumer3ByEmail.isPresent());

    Consumer consumer3 = consumer3ByEmail.get();
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

    List<Mailing> mailing_consumer3 = mailingRepository.findByEmail(consumer3.getEmail());
    assertEquals(1, mailing_consumer3.size());
    assertEquals(1, mailing_consumer3.get(0).getType());


    // Perform full auth test for New Consumer
    fullAuthTest(ConsumerTest.of(USER, consumer1.getEmail(), PASS_NEW_Consumer, client_TRUSTED, 200));
    fullAuthTest(ConsumerTest.of(USER, consumer2.getEmail(), mailing_password_consumer2, client_TRUSTED, 200));
    fullAuthTest(ConsumerTest.of(USER, consumer3.getEmail(), PASS_FOR_EMAIL_3_Consumer_Decoded, client_TRUSTED, 200));
  }

  @Test
  public void _04_updateConsumer()
  {
    logHead("Consumer Update");

    Optional<Consumer> consumerByEmail = getConsumerByEmail(USER_consumer.username);
    assertTrue(consumerByEmail.isPresent());

    Consumer consumer = consumerByEmail.get();
    assertEquals("user", consumer.getNickName());
    assertEquals("", consumer.getFirstName());
    assertEquals("", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer1);
    consumerByEmail = getConsumerByEmail(USER_consumer.username);
    consumer = consumerByEmail.get();
    assertEquals("user", consumer.getNickName());
    assertEquals("", consumer.getFirstName());
    assertEquals("", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer2);
    consumerByEmail = getConsumerByEmail(USER_consumer.username);
    consumer = consumerByEmail.get();
    assertEquals("Consumer", consumer.getNickName());
    assertEquals("", consumer.getFirstName());
    assertEquals("", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer3);
    consumerByEmail = getConsumerByEmail(USER_consumer.username);
    consumer = consumerByEmail.get();
    assertEquals("Consumer1", consumer.getNickName());
    assertEquals("1", consumer.getFirstName());
    assertEquals("", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer4);
    consumerByEmail = getConsumerByEmail(USER_consumer.username);
    consumer = consumerByEmail.get();
    assertEquals("Consumer1", consumer.getNickName());
    assertEquals("1", consumer.getFirstName());
    assertEquals("2", consumer.getLastName());

    check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer5);
    consumerByEmail = getConsumerByEmail(USER_consumer.username);
    consumer = consumerByEmail.get();
    assertEquals("user", consumer.getNickName());
    assertEquals("alto", consumer.getFirstName());
    assertEquals("smith", consumer.getLastName());
  }

  @Test
  public void _05_getConsumerData()
  {
    logHead("Consumer Data");
    check_send_data(GET, resource_getConsumerData, USER_consumer.access_token, checkers_for_getConsumerData);
  }

  @Test
  public void _06_publicConsumer()
  {
    logHead("Public Consumer");
    check_send_data(GET, resource_publicConsumer + "/9", ANY_consumer.access_token, checkers_for_publicConsumer_9);
    check_send_data(GET, resource_publicConsumer + "/9", USER_consumer.access_token, checkers_for_publicConsumer_9);
    check_send_data(GET, resource_publicConsumer + "/9", ADMIN_consumer.access_token, checkers_for_publicConsumer_9);
    check_send_data(GET, resource_publicConsumer + "/1", ANY_consumer.access_token, checkers_for_publicConsumer_1);
    check_send_data(GET, resource_publicConsumer + "/1", USER_consumer.access_token, checkers_for_publicConsumer_1);
    check_send_data(GET, resource_publicConsumer + "/1", ADMIN_consumer.access_token, checkers_for_publicConsumer_1);
  }

  @Test
  public void _07_requestConfirmConsumerEmail()
  {
    logHead("Request Confirm Email");

    mailingRepository.deleteAll();
    ConsumerTest consumer = ConsumerTest.of(USER, EMAIL_NEW_Consumer, PASS_NEW_Consumer, client_TRUSTED, 200);

    List<Mailing> confirmByEmail = mailingRepository.findByEmail(consumer.username);
    assertEquals(0, confirmByEmail.size());

    check_send_data(GET, resource_reqConfirmConsumerEmail, consumer.access_token, checkers_for_reqConfirmConsumerEmail);

    confirmByEmail = mailingRepository.findByEmail(consumer.username);
    assertEquals(1, confirmByEmail.size());

    Mailing mailing = confirmByEmail.get(0);
    assertEquals(3, mailing.getType());
    assertFalse(mailing.isSended());
    assertFalse(mailing.isMaxAttemptsReached());
    assertTrue(mailing.getData().length() >= 28);

    try
    {
      TimeUnit.SECONDS.sleep(15);
      log.info("Please wait 15 seconds...");
    }
    catch (InterruptedException ignored)
    {
    }

    confirmByEmail = mailingRepository.findByEmail(consumer.username);
    mailing = confirmByEmail.get(0);
    assertTrue(mailing.isSended());
    assertFalse(mailing.isMaxAttemptsReached());
  }

  @Test
  public void _08_confirmConsumerEmail()
  {
    logHead("Confirm Email");

    List<Mailing> confirmByEmail = mailingRepository.findByEmail(EMAIL_NEW_Consumer);
    assertEquals(1, confirmByEmail.size());
    String confirmKey = confirmByEmail.get(0).getData();

    assertFalse(getConsumerByEmail(EMAIL_NEW_Consumer).get().isEmailConfirmed());

    check_send_data(GET, resource_confirmConsumerEmail, null, checkers_for_confirmConsumerEmail(confirmKey));

    assertEquals(0, mailingRepository.findByEmail(EMAIL_NEW_Consumer).size());
    assertTrue(getConsumerByEmail(EMAIL_NEW_Consumer).get().isEmailConfirmed());
  }

  @Test
  public void _09_changeConsumerPass()
  {
//    changeConsumerPass(EMAIL_NEW_Consumer, PASS_NEW_Consumer, NEWPASS_NEW_Consumer, checkers_for_changeConsumerPass);
//
//    changeConsumerPass(EMAIL_3_NEW_Consumer, PASS_FOR_EMAIL_3_Consumer_Decoded, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, checkers_for_changeConsumerPass_3);
  }

  private void changeConsumerPass(String email, String oldpass, String newpass, List<Checker> checkers)
  {
//    logHead("Change Consumer Pass = " + email);
//
//    //newpass
//    //ConsumerTest.of(USER, email, newpass, client_TRUSTED, 400);
//
//    //oldpass
//    ConsumerTest consumerTest = ConsumerTest.of(USER, email, oldpass, client_TRUSTED, 200);
//
//    //change oldpass
//    check_send_data(POST, resource_changeConsumerPass, consumerTest.access_token, checkers);
//
//    //oldpass
//    //ConsumerTest.of(USER, email, oldpass, client_TRUSTED, 400);
//
//    //newpass
//    consumerTest = ConsumerTest.of(USER, email, newpass, client_TRUSTED, 200);
//    fullAuthTest(consumerTest);
  }

  @Test
  public void _10_restorePassword()
  {
//    restorePassword(EMAIL_NEW_Consumer, NEWPASS_NEW_Consumer, PASS_NEW_Consumer, PASS_NEW_Consumer);
//
//    restorePassword(EMAIL_3_NEW_Consumer, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, PASS_FOR_EMAIL_3_Consumer_Decoded, PASS_FOR_EMAIL_3_Consumer_Encoded);
  }

  private void restorePassword(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
//    reqRestoreConsumerPass(email);
//    confirmRestoreConsumerPass(email);
//    restoreConsumerPass(email, oldpassDecoded, newpassDecoded, newpassForCheckers);
  }

  private void reqRestoreConsumerPass(String email)
  {
//    logHead("Request Restore Consumer Password = " + email);
//
//    assertEquals(0, mailingRepository.findByEmail(email).size());
//
//    check_send_data(POST, resource_reqRestoreConsumerPass, ANY_consumer.access_token, checkers_for_reqRestoreConsumerPass(email));
//
//    List<Mailing> confirmByEmail = mailingRepository.findByEmail(email);
//    assertEquals(1, confirmByEmail.size());
//    assertFalse(confirmByEmail.get(0).isSended());
  }

  private void confirmRestoreConsumerPass(String email)
  {
//    logHead("Confirm Restore Consumer Password = " + email);
//
//    List<Mailing> confirmByEmail = mailingRepository.findByEmail(email);
//    assertEquals(1, confirmByEmail.size());
//
//    check_send_data(GET, resource_confirmRestoreConsumerPass, ANY_consumer.access_token, checkers_for_confirmRestoreConsumerPass(email, confirmByEmail.get(0).getData()));
//
//    confirmByEmail = mailingRepository.findByEmail(email);
//    assertEquals(1, confirmByEmail.size());
  }

  private void restoreConsumerPass(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
  {
//    logHead("Restore Consumer Password = " + email);
//
//    List<Mailing> confirmByEmail = mailingRepository.findByEmail(email);
//    assertEquals(1, confirmByEmail.size());
//
//    //oldpass
//    //ConsumerTest.of(USER, email, oldpassDecoded, client_TRUSTED, 200);
//    //newpass
//    //ConsumerTest.of(USER, email, newpassDecoded, client_TRUSTED, 400);
//
//    //change password
//    check_send_data(POST, resource_restoreConsumerPass, ANY_consumer.access_token,
//            checkers_for_restoreConsumerPass(email, newpassForCheckers, confirmByEmail.get(0).getData()));
//
//    assertEquals(0, mailingRepository.findByEmail(email).size());
//
//    //oldpass
//    //ConsumerTest.of(USER, email, oldpassDecoded, client_TRUSTED, 400);
//
//    //newpass
//    ConsumerTest consumerTest = ConsumerTest.of(USER, email, newpassDecoded, client_TRUSTED, 200);
//    fullAuthTest(consumerTest);
  }

  private void fullAuthTest(ConsumerTest consumerTest)
  {
    performFullAuthTest(consumerTest);
//    mailingRepository.delete(mailingRepository.findById(consumerTest.username).get());
  }

  private Optional<Consumer> getConsumerByEmail(String email)
  {
    return consumerService.findByEmail(email);
  }

  private static void logHead(String who)
  {
    log.info("\n=============================="
            + "\n {} "
            + "\n------------------------------", who);
  }

  private static final Logger log = LoggerFactory.getLogger(ConsumerPassword_CRU_Test.class);
}

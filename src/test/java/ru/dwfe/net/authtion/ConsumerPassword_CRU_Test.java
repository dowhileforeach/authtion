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
import ru.dwfe.net.authtion.dao.Consumer;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;
import ru.dwfe.net.authtion.dao.MailingWelcomeWhenPasswordWasNotPassed;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingWelcomeWhenPasswordWasNotPassedRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestorePasswordRepository;
import ru.dwfe.net.authtion.service.ConsumerService;
import ru.dwfe.net.authtion.test_util.Checker;
import ru.dwfe.net.authtion.test_util.ConsumerTest;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.Global.*;
import static ru.dwfe.net.authtion.test_util.AuthorityLevel.USER;
import static ru.dwfe.net.authtion.test_util.UtilTest.check_send_data;
import static ru.dwfe.net.authtion.test_util.UtilTest.performFullAuthTest;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;
import static ru.dwfe.net.authtion.test_util.Variables_for_ConsumerPassword_CRU_Test.*;

/*
    https://spring.io/guides/gs/testing-web/
*/

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ConsumerPassword_CRU_Test
{
    @Autowired
    ConsumerService consumerService;

    @Autowired
    MailingWelcomeWhenPasswordWasNotPassedRepository mailingWelcomeWhenPasswordWasNotPassedRepository;
    @Autowired
    MailingConfirmEmailRepository mailingConfirmEmailRepository;
    @Autowired
    MailingRestorePasswordRepository mailingRestorePasswordRepository;

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

        assertFalse(mailingWelcomeWhenPasswordWasNotPassedRepository.findById(EMAIL_NEW_Consumer).isPresent());
        assertFalse(mailingWelcomeWhenPasswordWasNotPassedRepository.findById(EMAIL_2_NEW_Consumer).isPresent());

        check_send_data(POST, resource_createConsumer, ANY_consumer.access_token, checkers_for_createConsumer());

        //just new user
        Optional<Consumer> consumer1ByEmail = getConsumerByEmail(EMAIL_NEW_Consumer);
        assertTrue(consumer1ByEmail.isPresent());

        Consumer consumer1 = consumer1ByEmail.get();
        assertTrue(consumer1.getId() > 1000);
        assertEquals("nobody", consumer1.getNickName());
        assertTrue(consumer1.getFirstName().isEmpty());
        assertTrue(consumer1.isAccountNonExpired() && consumer1.isAccountNonLocked() && consumer1.isCredentialsNonExpired() && consumer1.isEnabled());
        assertFalse(consumer1.isEmailConfirmed());
        assertFalse(mailingWelcomeWhenPasswordWasNotPassedRepository.findById(EMAIL_NEW_Consumer).isPresent());

        //new user, password was not passed
        Optional<Consumer> consumer2ByEmail = getConsumerByEmail(EMAIL_2_NEW_Consumer);
        assertTrue(consumer2ByEmail.isPresent());

        Consumer consumer2 = consumer2ByEmail.get();
        assertTrue(consumer2.getId() >= 1000);
        assertEquals(Consumer.prepareStringField(Consumer.getNickNameFromEmail(consumer2.getEmail()), 20), consumer2.getNickName());
        assertTrue(consumer2.isAccountNonExpired() && consumer2.isAccountNonLocked() && consumer2.isCredentialsNonExpired() && consumer2.isEnabled());
        assertFalse(consumer2.isEmailConfirmed());

        Optional<MailingWelcomeWhenPasswordWasNotPassed> mailingNewConsumerPasswordByEmail = mailingWelcomeWhenPasswordWasNotPassedRepository.findById(EMAIL_2_NEW_Consumer);
        assertTrue(mailingNewConsumerPasswordByEmail.isPresent());

        String PASS_2_notExistedConsumer = mailingNewConsumerPasswordByEmail.get().getPassword();
        assertTrue(PASS_2_notExistedConsumer.length() >= 9);

        //new user, already encoded password was passed
        Optional<Consumer> consumer3ByEmail = getConsumerByEmail(EMAIL_3_NEW_Consumer);
        assertTrue(consumer3ByEmail.isPresent());

        Consumer consumer3 = consumer3ByEmail.get();
        assertEquals(consumer3.getPassword(), "{bcrypt}" + PASS_FOR_EMAIL_3_Consumer_Encoded);
        assertEquals("hello world", consumer3.getNickName());
        assertTrue(consumer3.isAccountNonExpired() && consumer3.isAccountNonLocked() && consumer3.isCredentialsNonExpired() && consumer3.isEnabled());
        assertFalse(consumer3.isEmailConfirmed());
        assertFalse(mailingWelcomeWhenPasswordWasNotPassedRepository.findById(consumer3.getEmail()).isPresent());


        //perform full auth test for new Consumer
        fullAuthTest(ConsumerTest.of(USER, consumer1.getEmail(), PASS_NEW_Consumer, client_TRUSTED, 200));
        fullAuthTest(ConsumerTest.of(USER, consumer2.getEmail(), PASS_2_notExistedConsumer, client_TRUSTED, 200));
        fullAuthTest(ConsumerTest.of(USER, consumer3.getEmail(), PASS_FOR_EMAIL_3_Consumer_Decoded, client_TRUSTED, 200));
    }

    @Test
    public void _04_updateConsumer()
    {
        logHead("Consumer Update");
        check_send_data(POST, resource_updateConsumer, USER_consumer.access_token, checkers_for_updateConsumer);
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

        ConsumerTest consumerTest = ConsumerTest.of(USER, EMAIL_NEW_Consumer, PASS_NEW_Consumer, client_TRUSTED, 200);

        Optional<MailingConfirmEmail> confirmById = mailingConfirmEmailRepository.findById(consumerTest.username);
        assertFalse(confirmById.isPresent());

        check_send_data(GET, resource_reqConfirmConsumerEmail, consumerTest.access_token, checkers_for_reqConfirmConsumerEmail);

        confirmById = mailingConfirmEmailRepository.findById(consumerTest.username);
        assertTrue(confirmById.isPresent());
        assertFalse(confirmById.get().isAlreadySent());
        assertTrue(confirmById.get().getConfirmKey().length() >= 28);
    }

    @Test
    public void _08_confirmConsumerEmail()
    {
        logHead("Confirm Email");

        Optional<MailingConfirmEmail> confirmById = mailingConfirmEmailRepository.findById(EMAIL_NEW_Consumer);
        assertTrue(confirmById.isPresent());
        String confirmKey = confirmById.get().getConfirmKey();

        assertFalse(getConsumerByEmail(EMAIL_NEW_Consumer).get().isEmailConfirmed());

        check_send_data(GET, resource_confirmConsumerEmail, null, checkers_for_confirmConsumerEmail(confirmKey));

        assertFalse(mailingConfirmEmailRepository.findById(EMAIL_NEW_Consumer).isPresent());
        assertTrue(getConsumerByEmail(EMAIL_NEW_Consumer).get().isEmailConfirmed());
    }

    @Test
    public void _09_changeConsumerPass()
    {
        changeConsumerPass(EMAIL_NEW_Consumer, PASS_NEW_Consumer, NEWPASS_NEW_Consumer, checkers_for_changeConsumerPass);

        changeConsumerPass(EMAIL_3_NEW_Consumer, PASS_FOR_EMAIL_3_Consumer_Decoded, NEWPASS_FOR_EMAIL_3_Consumer_Decoded, checkers_for_changeConsumerPass_3);
    }

    private void changeConsumerPass(String email, String oldpass, String newpass, List<Checker> checkers)
    {
        logHead("Change Consumer Pass = " + email);

        //newpass
        //ConsumerTest.of(USER, email, newpass, client_TRUSTED, 400);

        //oldpass
        ConsumerTest consumerTest = ConsumerTest.of(USER, email, oldpass, client_TRUSTED, 200);

        //change oldpass
        check_send_data(POST, resource_changeConsumerPass, consumerTest.access_token, checkers);

        //oldpass
        //ConsumerTest.of(USER, email, oldpass, client_TRUSTED, 400);

        //newpass
        consumerTest = ConsumerTest.of(USER, email, newpass, client_TRUSTED, 200);
        fullAuthTest(consumerTest);
    }

    @Test
    public void _10_restorePassword()
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
        logHead("Request Restore Consumer Password = " + email);

        assertFalse(mailingRestorePasswordRepository.findById(email).isPresent());

        check_send_data(POST, resource_reqRestoreConsumerPass, ANY_consumer.access_token, checkers_for_reqRestoreConsumerPass(email));

        Optional<MailingRestorePassword> confirmById = mailingRestorePasswordRepository.findById(email);
        assertTrue(confirmById.isPresent());
        assertFalse(confirmById.get().isAlreadySent());
    }

    private void confirmRestoreConsumerPass(String email)
    {
        logHead("Confirm Restore Consumer Password = " + email);

        Optional<MailingRestorePassword> confirmById = mailingRestorePasswordRepository.findById(email);
        assertTrue(confirmById.isPresent());

        check_send_data(GET, resource_confirmRestoreConsumerPass, ANY_consumer.access_token, checkers_for_confirmRestoreConsumerPass(email, confirmById.get().getConfirmKey()));

        confirmById = mailingRestorePasswordRepository.findById(email);
        assertTrue(confirmById.isPresent());
    }

    private void restoreConsumerPass(String email, String oldpassDecoded, String newpassDecoded, String newpassForCheckers)
    {
        logHead("Restore Consumer Password = " + email);

        Optional<MailingRestorePassword> confirmById = mailingRestorePasswordRepository.findById(email);
        assertTrue(confirmById.isPresent());

        //oldpass
        //ConsumerTest.of(USER, email, oldpassDecoded, client_TRUSTED, 200);
        //newpass
        //ConsumerTest.of(USER, email, newpassDecoded, client_TRUSTED, 400);

        //change password
        check_send_data(POST, resource_restoreConsumerPass, ANY_consumer.access_token,
                checkers_for_restoreConsumerPass(email, newpassForCheckers, confirmById.get().getConfirmKey()));

        assertFalse(mailingRestorePasswordRepository.findById(email).isPresent());

        //oldpass
        //ConsumerTest.of(USER, email, oldpassDecoded, client_TRUSTED, 400);

        //newpass
        ConsumerTest consumerTest = ConsumerTest.of(USER, email, newpassDecoded, client_TRUSTED, 200);
        fullAuthTest(consumerTest);
    }

    private void fullAuthTest(ConsumerTest consumerTest)
    {
        performFullAuthTest(consumerTest);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(consumerTest.username).get());
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

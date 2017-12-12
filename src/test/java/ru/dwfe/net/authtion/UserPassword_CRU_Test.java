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
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingNewUserPasswordRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestorePasswordRepository;
import ru.dwfe.net.authtion.service.UserService;
import ru.dwfe.net.authtion.test_util.UserTest;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.test_util.AuthorityType.USER;
import static ru.dwfe.net.authtion.test_util.UtilTest.checkAllResources;
import static ru.dwfe.net.authtion.test_util.UtilTest.check_send_data;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;
import static ru.dwfe.net.authtion.test_util.Variables_for_CreateUserTest.*;

/*
    https://spring.io/guides/gs/testing-web/
*/

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserPassword_CRU_Test
{
    @Autowired
    UserService userService;

    @Autowired
    MailingNewUserPasswordRepository mailingNewUserPasswordRepository;
    @Autowired
    MailingConfirmEmailRepository mailingConfirmEmailRepository;
    @Autowired
    MailingRestorePasswordRepository mailingRestorePasswordRepository;

    @Test
    public void _01_checkUserEmail()
    {
        logHead("Check User E-mail");
        check_send_data(POST, resource_checkUserEmail, FRONTEND_user.access_token, checkers_for_checkUserEmail);
    }

    @Test
    public void _02_checkUserPass()
    {
        logHead("Check User Pass");
        check_send_data(POST, resource_checkUserPass, FRONTEND_user.access_token, checkers_for_checkUserPass);

    }

    @Test
    public void _03_createUser()
    {
        logHead("Create User");

        assertEquals(false, mailingNewUserPasswordRepository.findById(EMAIL_NEW_User).isPresent());
        assertEquals(false, mailingNewUserPasswordRepository.findById(EMAIL_2_NEW_User).isPresent());

        check_send_data(POST, resource_createUser, FRONTEND_user.access_token, checkers_for_createUser());

        Optional<User> user1ByEmail = getUserByEmail(EMAIL_NEW_User);
        assertEquals(true, user1ByEmail.isPresent());
        User user1 = user1ByEmail.get();
        assertEquals(true, user1.getId() > 1000);
        assertEquals(true, "nobody".equals(user1.getPublicName()));
        assertEquals(true, user1.getFirstName().isEmpty());
        assertEquals(true, user1.isAccountNonExpired() && user1.isAccountNonLocked() && user1.isCredentialsNonExpired() && user1.isEnabled());
        assertEquals(false, user1.isEmailConfirmed());

        Optional<User> user2ByEmail = getUserByEmail(EMAIL_2_NEW_User);
        assertEquals(true, user2ByEmail.isPresent());
        User user2 = user2ByEmail.get();
        assertEquals(true, user2.getId() >= 1000);
        assertEquals(true, "ozon".equals(user2.getPublicName()));
        assertEquals(true, user2.getPublicName().equals(user2.getFirstName()));
        assertEquals(true, user2.isAccountNonExpired() && user2.isAccountNonLocked() && user2.isCredentialsNonExpired() && user2.isEnabled());
        assertEquals(false, user2.isEmailConfirmed());

        assertEquals(false, mailingNewUserPasswordRepository.findById(EMAIL_NEW_User).isPresent());

        Optional<MailingNewUserPassword> mailingNewUserPasswordByEmail = mailingNewUserPasswordRepository.findById(EMAIL_2_NEW_User);
        assertEquals(true, mailingNewUserPasswordByEmail.isPresent());
        String PASS_2_notExistedUser = mailingNewUserPasswordByEmail.get().getPassword();
        assertEquals(true, PASS_2_notExistedUser.length() >= 9);

        //Test for new User access to all resources
        UserTest user1Test = UserTest.of(USER, user1.getEmail(), PASS_NEW_User, client_TRUSTED, 200);
        checkAllResources(user1Test);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(user1.getEmail()).get());

        UserTest user2Test = UserTest.of(USER, user2.getEmail(), PASS_2_notExistedUser, client_TRUSTED, 200);
        checkAllResources(user2Test);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(user2.getEmail()).get());
    }

    @Test
    public void _04_userUpdate()
    {
        logHead("User Update");
        check_send_data(POST, resource_userUpdate, USER_user.access_token, checkers_for_userUpdate);
    }

    @Test
    public void _05_userData()
    {
        logHead("User Data");
        check_send_data(GET, resource_userData, USER_user.access_token, checkers_for_userData);
    }

    @Test
    public void _06_publicUser()
    {
        logHead("Public User");
        check_send_data(GET, resource_publicUser9, null, checkers_for_publicUser9);
        check_send_data(GET, resource_publicUser9, USER_user.access_token, checkers_for_publicUser9);
        check_send_data(GET, resource_publicUser9, FRONTEND_user.access_token, checkers_for_publicUser9);
        check_send_data(GET, resource_publicUser1, null, checkers_for_publicUser1);
        check_send_data(GET, resource_publicUser1, ADMIN_user.access_token, checkers_for_publicUser1);
    }

    @Test
    public void _07_requestConfirmEmail()
    {
        logHead("Request Confirm Email");

        UserTest userTest = UserTest.of(USER, EMAIL_NEW_User, PASS_NEW_User, client_TRUSTED, 200);

        Optional<MailingConfirmEmail> confirmById = mailingConfirmEmailRepository.findById(userTest.username);
        assertEquals(false, confirmById.isPresent());

        check_send_data(GET, resource_reqConfirmEmail, userTest.access_token, checkers_for_reqConfirmEmail);

        confirmById = mailingConfirmEmailRepository.findById(userTest.username);
        assertEquals(true, confirmById.isPresent());
        assertEquals(false, confirmById.get().isAlreadySent());
        assertEquals(true, confirmById.get().getConfirmKey().length() >= 29);
    }

    @Test
    public void _08_confirmEmail()
    {
        logHead("Confirm Email");

        Optional<MailingConfirmEmail> confirmById = mailingConfirmEmailRepository.findById(EMAIL_NEW_User);
        assertEquals(true, confirmById.isPresent());
        String confirmKey = confirmById.get().getConfirmKey();

        assertEquals(false, getUserByEmail(EMAIL_NEW_User).get().isEmailConfirmed());

        check_send_data(GET, resource_confirmEmail, null, checkers_for_confirmEmail(confirmKey));

        assertEquals(false, mailingConfirmEmailRepository.findById(EMAIL_NEW_User).isPresent());
        assertEquals(true, getUserByEmail(EMAIL_NEW_User).get().isEmailConfirmed());
    }

    @Test
    public void _09_changeUserPass()
    {
        logHead("Change User Pass");

        //newpass
        UserTest.of(USER, EMAIL_NEW_User, NEWPASS_NEW_User, client_TRUSTED,
                400);
        //oldpass
        UserTest userTest = UserTest.of(USER, EMAIL_NEW_User, PASS_NEW_User, client_TRUSTED,
                200);
        //change oldpass
        check_send_data(POST, resource_changeUserPass, userTest.access_token, checkers_for_changeUserPass);

        //oldpass
        UserTest.of(USER, EMAIL_NEW_User, PASS_NEW_User, client_TRUSTED,
                400);
        //newpass
        userTest = UserTest.of(USER, EMAIL_NEW_User, NEWPASS_NEW_User, client_TRUSTED,
                200);
        checkAllResources(userTest);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(userTest.username).get());
    }

    @Test
    public void _10_reqRestoreUserPass()
    {
        logHead("Request Restore User Password");

        assertEquals(false, mailingRestorePasswordRepository.findById(EMAIL_NEW_User).isPresent());

        check_send_data(POST, resource_reqRestoreUserPass, FRONTEND_user.access_token, checkers_for_reqRestoreUserPass);

        Optional<MailingRestorePassword> confirmById = mailingRestorePasswordRepository.findById(EMAIL_NEW_User);
        assertEquals(true, confirmById.isPresent());
        assertEquals(false, confirmById.get().isAlreadySent());
    }

    @Test
    public void _11_confirmRestoreUserPass()
    {
        logHead("Confirm Restore User Password");

        Optional<MailingRestorePassword> confirmById = mailingRestorePasswordRepository.findById(EMAIL_NEW_User);
        assertEquals(true, confirmById.isPresent());

        check_send_data(GET, resource_confirmRestoreUserPass, null, checkers_for_confirmRestoreUserPass(confirmById.get().getConfirmKey()));

        confirmById = mailingRestorePasswordRepository.findById(EMAIL_NEW_User);
        assertEquals(true, confirmById.isPresent());
    }

    @Test
    public void _12_restoreUserPass()
    {
        logHead("Restore User Password");

        Optional<MailingRestorePassword> confirmById = mailingRestorePasswordRepository.findById(EMAIL_NEW_User);
        assertEquals(true, confirmById.isPresent());

        //oldpass
        UserTest.of(USER, EMAIL_NEW_User, NEWPASS_NEW_User, client_TRUSTED,
                200);
        //newpass
        UserTest.of(USER, EMAIL_NEW_User, PASS_NEW_User, client_TRUSTED,
                400);

        //change password
        check_send_data(POST, resource_restoreUserPass, FRONTEND_user.access_token, checkers_for_restoreUserPass(confirmById.get().getConfirmKey()));

        assertEquals(false, mailingRestorePasswordRepository.findById(EMAIL_NEW_User).isPresent());

        //oldpass
        UserTest.of(USER, EMAIL_NEW_User, NEWPASS_NEW_User, client_TRUSTED,
                400);
        //newpass
        UserTest userTest = UserTest.of(USER, EMAIL_NEW_User, PASS_NEW_User, client_TRUSTED,
                200);
        checkAllResources(userTest);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(userTest.username).get());

        userService.delete(getUserByEmail(EMAIL_NEW_User).get());
        userService.delete(getUserByEmail(EMAIL_2_NEW_User).get());
        assertEquals(false, getUserByEmail(EMAIL_NEW_User).isPresent());
        assertEquals(false, getUserByEmail(EMAIL_2_NEW_User).isPresent());
    }


    private Optional<User> getUserByEmail(String email)
    {
        return userService.findByEmail(email);
    }

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n {} "
                + "\n------------------------------", who);
    }

    private static final Logger log = LoggerFactory.getLogger(UserPassword_CRU_Test.class);
}

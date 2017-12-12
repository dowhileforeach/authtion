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
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingNewUserPasswordRepository;
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
public class CreateUserTest
{
    @Autowired
    UserService userService;

    @Autowired
    MailingNewUserPasswordRepository mailingNewUserPasswordRepository;
    @Autowired
    MailingConfirmEmailRepository mailingConfirmEmailRepository;

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
        check_send_data(POST, resource_createUser, FRONTEND_user.access_token, checkers_for_createUser());

        Optional<User> user1ByEmail = getUserByEmail(EMAIL_notExistedUser);
        assertEquals(true, user1ByEmail.isPresent());
        User user1 = user1ByEmail.get();
        assertEquals(true, user1.getId() > 1000);
        assertEquals(true, "nobody".equals(user1.getPublicName()));
        assertEquals(true, user1.getFirstName().isEmpty());
        assertEquals(true, user1.isAccountNonExpired() && user1.isAccountNonLocked() && user1.isCredentialsNonExpired() && user1.isEnabled());
        assertEquals(false, user1.isEmailConfirmed());

        Optional<User> user2ByEmail = getUserByEmail(EMAIL_2_notExistedUser);
        assertEquals(true, user2ByEmail.isPresent());
        User user2 = user2ByEmail.get();
        assertEquals(true, user2.getId() > 1000);
        assertEquals(true, "ozon".equals(user2.getPublicName()));
        assertEquals(true, user2.getPublicName().equals(user2.getFirstName()));
        assertEquals(true, user2.isAccountNonExpired() && user2.isAccountNonLocked() && user2.isCredentialsNonExpired() && user2.isEnabled());
        assertEquals(false, user2.isEmailConfirmed());

        assertEquals(false, mailingNewUserPasswordRepository.findById(EMAIL_notExistedUser).isPresent());

        Optional<MailingNewUserPassword> mailingNewUserPasswordByEmail = mailingNewUserPasswordRepository.findById(EMAIL_2_notExistedUser);
        assertEquals(true, mailingNewUserPasswordByEmail.isPresent());
        String PASS_2_notExistedUser = mailingNewUserPasswordByEmail.get().getPassword();
        assertEquals(true, PASS_2_notExistedUser.length() >= 9);

        //Test for new User access to all resources
        UserTest user1Test = UserTest.of(USER, user1.getEmail(), PASS_notExistedUser, client_TRUSTED, 200);
        checkAllResources(user1Test);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(user1.getEmail()).get());

        UserTest user2Test = UserTest.of(USER, user2.getEmail(), PASS_2_notExistedUser, client_TRUSTED, 200);
        checkAllResources(user2Test);
        mailingConfirmEmailRepository.delete(mailingConfirmEmailRepository.findById(user2.getEmail()).get());
    }

    @Test
    public void _04_userData()
    {
        logHead("User Data");
        check_send_data(GET, resource_userData, USER_user.access_token, checkers_for_userData);
    }

    @Test
    public void _05_publicUser()
    {
        logHead("Public User");
        check_send_data(GET, resource_publicUser9, null, checkers_for_publicUser9);
        check_send_data(GET, resource_publicUser9, USER_user.access_token, checkers_for_publicUser9);
        check_send_data(GET, resource_publicUser9, FRONTEND_user.access_token, checkers_for_publicUser9);
        check_send_data(GET, resource_publicUser1, null, checkers_for_publicUser1);
        check_send_data(GET, resource_publicUser1, ADMIN_user.access_token, checkers_for_publicUser1);
    }

    @Test
    public void _06_requestConfirmEmail()
    {
        logHead("Request Confirm Email");

        UserTest userTest = UserTest.of(USER, EMAIL_notExistedUser, PASS_notExistedUser, client_TRUSTED, 200);

        assertEquals(false, mailingConfirmEmailRepository.findById(userTest.username).isPresent());

        check_send_data(GET, resource_reqConfirmEmail, userTest.access_token, checkers_for_reqConfirmEmail);

        assertEquals(true, mailingConfirmEmailRepository.findById(userTest.username).isPresent());
    }

//    @Test
//    public void _04_confirmUser()
//    {
//        logHead("Confirm User");
//
//        Optional<ConfirmationKey> confirmationKey = getConfirmationKey(EMAIL_notExistedUser);
//        assertEquals(true, confirmationKey.isPresent());
//        assertEquals(true, confirmationKey.get().isCreateNewUser());
//        assertEquals(false, confirmationKey.get().isRestoreUserPass());
//
//        ConfirmationKey key_NONisCreateNewUser = ConfirmationKey.of(confirm_NONisCreateNewUser_user, confirm_NONisCreateNewUser_key, false, true);
//        ConfirmationKey key_SomethingWentWrong = ConfirmationKey.of(confirm_SomethingWentWrong_user, confirm_SomethingWentWrong_key, true, false);
//        confirmationKeyService.save(key_NONisCreateNewUser);
//        confirmationKeyService.save(key_SomethingWentWrong);
//
//        //confirmation process
//        check_send_data(GET, resource_confirmUser, null, checkers_for_confirmUser(confirmationKey.get().getKey()));
//
//        //Confirmation key must be removed after confirmation
//        assertEquals(false, getConfirmationKey(EMAIL_notExistedUser).isPresent());
//
//        Optional<User> user = getUserById(EMAIL_notExistedUser);
//
//        //The new User must be unlocked after confirmation
//        assertEquals(true, user.get().isAccountNonLocked());
//
//        //Test for new User access to all resources
//        UserTest userTest = UserTest.of(USER, user.get().getUsername(), PASS_notExistedUser, client_TRUSTED, 200);
//        checkAllResources(userTest);
//
//        confirmationKeyService.delete(key_NONisCreateNewUser);
//        confirmationKeyService.delete(key_SomethingWentWrong);
//    }
//
//    @Test
//    public void _05_changeUserPass()
//    {
//        logHead("Change User Pass");
//
//        //oldpass
//        UserTest userTest = UserTest.of(USER, EMAIL_notExistedUser, PASS_notExistedUser, client_TRUSTED, 200);
//        //change oldpass
//        check_send_data(POST, resource_changeUserPass, userTest.access_token, checkers_for_changeUserPass);
//
//        //oldpass
//        UserTest.of(USER, EMAIL_notExistedUser, PASS_notExistedUser, client_TRUSTED, 400);
//        //newpass
//        userTest = UserTest.of(USER, EMAIL_notExistedUser, NEWPASS_notExistedUser, client_TRUSTED, 200);
//        checkAllResources(userTest);
//
//        Optional<User> user = getUserById(EMAIL_notExistedUser);
//        userService.delete(user.get());
//        assertEquals(false, getUserById(EMAIL_notExistedUser).isPresent());
//    }
//
//    @Test
//    public void _06_restoreUserPass()
//    {
//        logHead("Restore User Pass");
//
//
//    }
//
//
//
//
//    /*
//        UTILs
//    */
//
//    private Optional<ConfirmationKey> getConfirmationKey(String id)
//    {
//        return confirmationKeyService.findById(id);
//    }

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

    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

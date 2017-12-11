package ru.dwfe.net.authtion;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.test_util.UserTest;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.test_util.UtilTest.check_send_data;
import static ru.dwfe.net.authtion.test_util.Variables_Global.*;
import static ru.dwfe.net.authtion.test_util.Variables_for_CreateUserTest.*;

/*
    https://spring.io/guides/gs/testing-web/
*/

//@RunWith(SpringRunner.class)
//@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class CreateUserTest
{
//    @Autowired
//    UserService userService;
//    @Autowired
//    ConfirmationKeyService confirmationKeyService;

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

//        Optional<User> user = getUserByE(EMAIL_notExistedUser);
//        assertEquals(true, user.isPresent());

//        //New user is locked. Will be unlocked after confirmation
//        assertEquals(false, user.get().isAccountNonLocked());

//        //Test for new User access to all resources
//        UserTest userTest = UserTest.of(USER, user.get().getUsername(), PASS_notExistedUser, client_TRUSTED, 400);
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
//
//    private Optional<User> getUserById(String id)
//    {
//        return userService.findById(id);
//    }
//
    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n {} "
                + "\n------------------------------", who);
    }

    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

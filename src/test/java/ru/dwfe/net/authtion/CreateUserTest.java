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
import ru.dwfe.net.authtion.dao.ConfirmationKey;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.service.ConfirmationKeyService;
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
    ConfirmationKeyService confirmationKeyService;

    private static String access_token = USERtest_FRONTEND.access_token;

    @Test
    public void _01_checkUserId() throws Exception
    {
        logHead("Check User ID");
        check_send_data(POST, resource_checkUserId, access_token, checkers_for_checkUserId);
    }

    @Test
    public void _02_checkUserPass() throws Exception
    {
        logHead("Check User Pass");
        check_send_data(POST, resource_checkUserPass, access_token, checkers_for_checkUserPass);

    }

    @Test
    public void _03_createUser() throws Exception
    {
        logHead("Create User");
        check_send_data(POST, resource_createUser, access_token, checkers_for_createUser());

        Optional<User> user = getUserById(ID_notExistedUser);
        assertEquals(true, user.isPresent());

        //New user is locked. Will be unlocked after confirmation
        assertEquals(false, user.get().isAccountNonLocked());

        //Test for new User access to all resources
        UserTest userTest = UserTest.of(USER, user.get().getUsername(), PASS_notExistedUser, client_TRUSTED, 400);
    }

    @Test
    public void _04_confirmUser() throws Exception
    {
        logHead("Confirm User");

        Optional<ConfirmationKey> confirmationKey = getConfirmationKey(ID_notExistedUser);
        assertEquals(true, confirmationKey.isPresent());
        assertEquals(true, confirmationKey.get().isCreateNewUser());
        assertEquals(false, confirmationKey.get().isRestoreUserPass());

        ConfirmationKey key_NONisCreateNewUser = ConfirmationKey.of(confirm_NONisCreateNewUser_user, confirm_NONisCreateNewUser_key, false, true);
        ConfirmationKey key_SomethingWentWrong = ConfirmationKey.of(confirm_SomethingWentWrong_user, confirm_SomethingWentWrong_key, true, false);
        confirmationKeyService.save(key_NONisCreateNewUser);
        confirmationKeyService.save(key_SomethingWentWrong);

        //confirmation process
        check_send_data(GET, resource_confirmUser, null, checkers_for_confirmUser(confirmationKey.get().getKey()));

        //Confirmation key must be removed after confirmation
        assertEquals(false, getConfirmationKey(ID_notExistedUser).isPresent());

        Optional<User> user = getUserById(ID_notExistedUser);

        //The new User must be unlocked after confirmation
        assertEquals(true, user.get().isAccountNonLocked());

        //Test for new User access to all resources
        UserTest userTest = UserTest.of(USER, user.get().getUsername(), PASS_notExistedUser, client_TRUSTED, 200);
        checkAllResources(userTest);

        confirmationKeyService.delete(key_NONisCreateNewUser);
        confirmationKeyService.delete(key_SomethingWentWrong);

        userService.delete(user.get());
        assertEquals(false, getUserById(ID_notExistedUser).isPresent());
    }

    @Test
    public void _05_changeUserPass()
    {
        logHead("Change User Pass");


    }

    @Test
    public void _06_restoreUserPass()
    {
        logHead("Restore User Pass");


    }




    /*
        UTILs
    */

    private Optional<ConfirmationKey> getConfirmationKey(String id)
    {
        return confirmationKeyService.findById(id);
    }

    private Optional<User> getUserById(String id)
    {
        return userService.findById(id);
    }

    private static void logHead(String who)
    {
        log.info("\n=============================="
                + "\n {} "
                + "\n------------------------------", who);
    }

    private static final Logger log = LoggerFactory.getLogger(CreateUserTest.class);
}

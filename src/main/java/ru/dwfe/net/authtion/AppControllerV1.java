package ru.dwfe.net.authtion;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestorePasswordRepository;
import ru.dwfe.net.authtion.dao.repository.MailingNewUserPasswordRepository;
import ru.dwfe.net.authtion.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.web.bind.annotation.RequestMethod.POST;
import static ru.dwfe.net.authtion.Util.*;
import static ru.dwfe.net.authtion.dao.User.*;

@RestController
public class AppControllerV1
{
    private static final String API = "/v1";

    @Autowired
    UserService userService;

    @Autowired
    MailingNewUserPasswordRepository mailingNewUserPasswordRepository;
    @Autowired
    MailingConfirmEmailRepository mailingConfirmEmailRepository;
    @Autowired
    MailingRestorePasswordRepository mailingRestorePasswordRepository;

    @RequestMapping(API + "/public")
    public String publicResource()
    {
        return "{\"public\": true}";
    }

    @RequestMapping(API + "/cities")
    @PreAuthorize("hasAuthority('USER')")
    public String cities()
    {
        return "{\"cities\": true}";
    }

    @RequestMapping(API + "/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> users()
    {
        return userService.findAll();
    }

    @RequestMapping(value = API + "/check-user-email", method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkUserId(@RequestBody String body)
    {
        boolean result;
        Map<String, Object> details = new HashMap<>();

        String email = (String) getValueFromJSON(body, "email");
        result = canUseEmail(email, userService, details);

        return getResponse("canUse", result, details);
    }

    @RequestMapping(value = API + "/check-user-pass", method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkUserPass(@RequestBody String body)
    {
        boolean result;
        Map<String, Object> details = new HashMap<>();

        String password = (String) getValueFromJSON(body, "password");
        result = canUsePassword(password, "password", details);

        return getResponse("canUse", result, details);
    }

    @RequestMapping(value = API + "/create-user", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String createUser(@RequestBody User user)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();
        String automaticallyGeneratedPassword = "";

        if (canUseEmail(user.getEmail(), userService, details))
        {
            String receivedPassword = user.getPassword();
            if (isDefaultCheckOK(receivedPassword))
            {   //if password was passed
                canUsePassword(receivedPassword, "password", details);
            }
            else
            {  //if password wasn't passed
                automaticallyGeneratedPassword = getUniqStr(7);
                user.setPassword(automaticallyGeneratedPassword);
            }
        }
        if (details.size() == 0)
        {
            //prepare
            prepareNewUser(user);

            //put user to the database
            userService.save(user);

            if (!automaticallyGeneratedPassword.isEmpty())
            { //If the password was not passed, then it is necessary to send an automatically generated password to the user
                mailingNewUserPasswordRepository
                        .save(MailingNewUserPassword.of(user.getEmail(), automaticallyGeneratedPassword));

                //TODO send e-mail
            }
            result = true;
        }
        return getResponse("success", result, details);
    }

//    @RequestMapping(value = API + "/req-confirm-email")
//    @PreAuthorize("hasAuthority('USER')")
//    public String requestConfirmEmail(OAuth2Authentication authentication) throws JsonProcessingException
//    {
//        String id = ((User) authentication.getPrincipal()).getId();
//        mailingConfirmEmailRepository.save(MailingConfirmEmail.of(id));
//
//        //TODO send e-mail
//
//        return getResponse("success", true, null);
//    }
//
//    @RequestMapping(API + "/confirm-email")
//    public String confirmEmail(@RequestParam String key) throws JsonProcessingException
//    {
//        boolean result = false;
//        String fieldName = "error";
//        Map<String, Object> details = new HashMap<>();
//
//        if (isDefaultCheckOK(key, fieldName, details))
//        {
//            MailingConfirmEmail confirm = mailingConfirmEmailRepository.findByConfirmKey(key);
//            if (confirm != null)
//            {
//                //The User is guaranteed to exist because: FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE
//                User user = userService.findById(confirm.getUser()).get();
//                user.setEmailConfirmed(true); //Now email is confirmed
//                userService.save(user);
//
//                //delete this confirmation key from database
//                mailingConfirmEmailRepository.delete(confirm);
//
//                result = true;
//            }
//            else details.put(fieldName, "key does not exist");
//        }
//        return getResponse("success", result, details);
//    }
//
//    @RequestMapping(value = API + "/change-user-pass", method = RequestMethod.POST)
//    @PreAuthorize("hasAuthority('USER')")
//    public String changeUserPass(@RequestBody String body, OAuth2Authentication authentication) throws JsonProcessingException
//    {
//        boolean result = false;
//        String fieldName = "oldpass";
//        Map<String, Object> details = new HashMap<>();
//        Map<String, Object> map = parse(body);
//
//        String oldpass = (String) getValue(map, "oldpass");
//        String newpass = (String) getValue(map, "newpass");
//
//        if (canUsePassword(newpass, "newpass", details)
//                && isDefaultCheckOK(oldpass, "oldpass", details))
//        {
//            String id = ((User) authentication.getPrincipal()).getId();
//            User user = userService.findById(id).get();
//            if (matchPassword("{bcrypt}", oldpass, user.getPassword()))
//            {
//                user.setPassword(getBCryptEncodedPassword(newpass));
//                userService.save(user);
//
//                result = true;
//            }
//            else details.put(fieldName, "incorrect");
//        }
//        return getResponse("success", result, details);
//    }
//
//    @RequestMapping(value = API + "/req-restore-user-pass", method = RequestMethod.POST)
//    @PreAuthorize("hasAuthority('FRONTEND')")
//    public String requestRestoreUserPass(@RequestBody String body) throws JsonProcessingException
//    {
//        boolean result = false;
//        String fieldName = "error";
//        Map<String, Object> details = new HashMap<>();
//
//        String id = (String) getValueFromJSON(body, "id");
//
//        if (isDefaultCheckOK(id, "id", details))
//        {
//            Optional<User> userById = userService.findById(id);
//            if (userById.isPresent())
//            {
//                MailingRestorePassword confirm = MailingRestorePassword.of(id);
//                mailingRestorePasswordRepository.save(confirm);
//
//                //TODO send e-mail
//            }
//            else details.put(fieldName, "user doesn't exist");
//        }
//        return getResponse("success", result, details);
//    }
//
//    @RequestMapping(API + "/confirm-restore-user-pass")
//    public String confirmRestoreUserPass(@RequestParam String key) throws JsonProcessingException
//    {
//        boolean result = false;
//        String fieldName = "error";
//        Map<String, Object> details = new HashMap<>();
//
//        if (isDefaultCheckOK(key, fieldName, details))
//        {
//            MailingRestorePassword confirm = mailingRestorePasswordRepository.findByConfirmKey(key);
//            if (confirm != null)
//            {
//                details.put("id", confirm.getUser());
//                result = true;
//            }
//            else details.put(fieldName, "key does not exist");
//        }
//        return getResponse("success", result, details);
//    }
//
//    @RequestMapping(value = API + "/restore-user-pass", method = RequestMethod.POST)
//    @PreAuthorize("hasAuthority('FRONTEND')")
//    public String restoreUserPass(@RequestBody String body) throws JsonProcessingException
//    {
//        boolean result = false;
//        String fieldName = "error";
//        Map<String, Object> details = new HashMap<>();
//        Map<String, Object> map = parse(body);
//
//        String id = (String) getValue(map, "id");
//        String key = (String) getValue(map, "key");
//        String newpass = (String) getValue(map, "newpass");
//
//        if (isDefaultCheckOK(id, "id", details)
//                && isDefaultCheckOK(key, "key", details)
//                && canUsePassword(newpass, "newpass", details))
//        {
//            MailingRestorePassword confirm = mailingRestorePasswordRepository.findByConfirmKey(key);
//            if (confirm != null)
//            {
//                //The User is guaranteed to exist because: FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE
//                User user = userService.findById(id).get();
//                user.setPassword(getBCryptEncodedPassword(newpass));
//                userService.save(user);
//
//                mailingRestorePasswordRepository.delete(confirm);
//
//                result = true;
//            }
//            else details.put(fieldName, "key does not exist");
//        }
//        return getResponse("success", result, details);
//    }
}

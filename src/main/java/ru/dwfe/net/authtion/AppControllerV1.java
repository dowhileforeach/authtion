package ru.dwfe.net.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import ru.dwfe.net.authtion.dao.MailingConfirmEmail;
import ru.dwfe.net.authtion.dao.MailingNewUserPassword;
import ru.dwfe.net.authtion.dao.MailingRestorePassword;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.dao.repository.MailingConfirmEmailRepository;
import ru.dwfe.net.authtion.dao.repository.MailingNewUserPasswordRepository;
import ru.dwfe.net.authtion.dao.repository.MailingRestorePasswordRepository;
import ru.dwfe.net.authtion.service.UserService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.web.bind.annotation.RequestMethod.*;
import static ru.dwfe.net.authtion.dao.User.*;
import static ru.dwfe.net.authtion.util.Util.*;

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

    @RequestMapping(value = API + "/create-user", method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String createUser(@RequestBody User user)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();
        String automaticallyGeneratedPassword = "";

        if (canUseEmail(user.getEmail(), userService, details))
        {
            String receivedPassword = user.getPassword();
            if (receivedPassword == null)
            {   //if password wasn't passed
                automaticallyGeneratedPassword = getUniqStr(10);
                user.setPassword(automaticallyGeneratedPassword);
            }
            else
            {  //if password was passed
                canUsePassword(receivedPassword, "password", details);
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

    @RequestMapping(value = API + "/user-data")
    @PreAuthorize("hasAuthority('USER')")
    public String userData(OAuth2Authentication authentication)
    {
        return getResponse("success", true, authentication.getPrincipal().toString());
    }

    @RequestMapping(value = API + "/public/user/{id}")
    public String getPublicUserInfo(@PathVariable Long id)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();

        Optional<User> userById = userService.findById(id);
        if (userById.isPresent())
        {
            User user = userById.get();
            details.put("id", user.getId());
            details.put("publicName", user.getPublicName());
            result = true;
        }
        else details.put("error", "user doesn't exist");

        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + "/req-confirm-email")
    @PreAuthorize("hasAuthority('USER')")
    public String requestConfirmEmail(OAuth2Authentication authentication)
    {
        String email = ((User) authentication.getPrincipal()).getEmail();
        mailingConfirmEmailRepository.save(MailingConfirmEmail.of(email));

        //TODO send e-mail

        return getResponse("success", true, Map.of());
    }

    @RequestMapping(API + "/confirm-email")
    public String confirmEmail(@RequestParam String key)
    {
        boolean result = false;
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();

        if (isDefaultCheckOK(key, fieldName, details))
        {
            Optional<MailingConfirmEmail> confirmByKey = mailingConfirmEmailRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                MailingConfirmEmail confirm = confirmByKey.get();

                //The User is guaranteed to exist because: FOREIGN KEY (`user`) REFERENCES `users` (`id`) ON DELETE CASCADE
                User user = userService.findByEmail(confirm.getUser()).get();
                user.setEmailConfirmed(true); //Now email is confirmed
                userService.save(user);

                //delete this confirmation key from database
                mailingConfirmEmailRepository.delete(confirm);

                result = true;
            }
            else details.put(fieldName, "key does not exist");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + "/change-user-pass", method = POST)
    @PreAuthorize("hasAuthority('USER')")
    public String changeUserPass(@RequestBody String body, OAuth2Authentication authentication)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        String oldpass = (String) getValue(map, "oldpass");
        String newpass = (String) getValue(map, "newpass");

        if (isDefaultCheckOK(oldpass, "oldpass", details)
                && canUsePassword(newpass, "newpass", details))
        {
            Long id = ((User) authentication.getPrincipal()).getId();
            User user = userService.findById(id).get();
            if (matchPassword("{bcrypt}", oldpass, user.getPassword()))
            {
                user.setPassword(getBCryptEncodedPassword(newpass));
                userService.save(user);

                result = true;
            }
            else details.put("oldpass", "wrong");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + "/req-restore-user-pass", method = POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String requestRestoreUserPass(@RequestBody String body)
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();

        String email = (String) getValueFromJSON(body, "email");

        if (isDefaultEmailCheckOK(email, details))
        {
            if (userService.existsByEmail(email))
            {
                MailingRestorePassword confirm = MailingRestorePassword.of(email);
                mailingRestorePasswordRepository.save(confirm);

                //TODO send e-mail

                result = true;
            }
            else details.put("error", "user doesn't exist");
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(API + "/confirm-restore-user-pass")
    public String confirmRestoreUserPass(@RequestParam String key)
    {
        boolean result = false;
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();

        if (isDefaultCheckOK(key, fieldName, details))
        {
            Optional<MailingRestorePassword> confirmByKey = mailingRestorePasswordRepository.findByConfirmKey(key);
            if (confirmByKey.isPresent())
            {
                details.put("email", confirmByKey.get().getUser());
                details.put("key", key);
                result = true;
            }
            else details.put(fieldName, "key does not exist");
        }
        return getResponse("success", result, details);
    }

//    @RequestMapping(value = API + "/restore-user-pass", method = POST)
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

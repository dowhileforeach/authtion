package ru.dwfe.net.authtion;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;
import ru.dwfe.net.authtion.dao.ConfirmationKey;
import ru.dwfe.net.authtion.dao.User;
import ru.dwfe.net.authtion.service.ConfirmationKeyService;
import ru.dwfe.net.authtion.service.UserService;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static ru.dwfe.net.authtion.Util.*;

@RestController
public class AppControllerV1
{
    private static final String API = "/v1";

    @Autowired
    UserService userService;
    @Autowired
    ConfirmationKeyService confirmationKeyService;

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

    @RequestMapping(value = API + "/check-user-id", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkUserId(@RequestBody String body) throws JsonProcessingException
    {
        boolean result;
        Map<String, Object> details = new HashMap<>();

        String id = (String) getValueFromJSON(body, "id");
        result = User.canUseID(id, userService, details);

        return getResponse("canUse", result, details);
    }

    @RequestMapping(value = API + "/check-user-pass", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkUserPass(@RequestBody String body) throws JsonProcessingException
    {
        boolean result;
        Map<String, Object> details = new HashMap<>();

        String password = (String) getValueFromJSON(body, "password");
        result = User.canUsePassword(password, details);

        return getResponse("canUse", result, details);
    }

    @RequestMapping(value = API + "/create-user", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String createUser(@RequestBody User user) throws IOException
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();

        if (User.isFieldsCorrect(user, userService, details))
        {
            //prepare
            User.prepareNewUser(user, confirmationKeyService);

            //put user to the database
            userService.save(user);

            result = true;
        }
        return getResponse("success", result, details);
    }

    @RequestMapping(API + "/confirm-user")
    public String confirmUser(@RequestParam String key) throws JsonProcessingException
    {
        boolean result = false;
        String fieldName = "error";
        Map<String, Object> details = new HashMap<>();

        if (key != null && !key.isEmpty())
        {
            ConfirmationKey confirmationKey = confirmationKeyService.findByKey(key);
            if (confirmationKey != null)
            {
                if (confirmationKey.isCreateNewUser()) //key is special for create new user
                {
                    Optional<User> userById = userService.findById(confirmationKey.getUser());
                    if (userById.isPresent())
                    {
                        if (!userById.get().isAccountNonLocked()) //must be locked
                        {
                            User user = userById.get();
                            user.setAccountNonLocked(true); //The user is now unlocked
                            userService.save(user);

                            //delete this confirmation key from database
                            confirmationKeyService.delete(confirmationKey);

                            result = true;
                        }
                        else details.put(fieldName, "user is non locked. Something went wrong...");
                    }
                    else
                    {
                        details.put(fieldName, "user does not exist");

                        //delete this confirmation key from database
                        confirmationKeyService.delete(confirmationKey);
                    }
                }
                else details.put(fieldName, "key is not valid for confirmation after a new user is created");
            }
            else details.put(fieldName, "key does not exist");
        }
        else details.put(fieldName, "bad key");

        return getResponse("success", result, details);
    }

    @RequestMapping(value = API + "/change-user-pass", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('USER')")
    public String createUser(@RequestBody String body, OAuth2Authentication authentication) throws JsonProcessingException
    {
        boolean result = false;
        String fieldName = "oldpass";
        Map<String, Object> details = new HashMap<>();
        Map<String, Object> map = parse(body);

        String oldpass = (String) getValue(map, "oldpass");
        String newpass = (String) getValue(map, "newpass");

        if (isDefaultValueCheckOK(oldpass, "oldpass", details))
        {
            String id = ((User) authentication.getPrincipal()).getId();
            User user = userService.findById(id).get();
            if (User.preparePassword(oldpass).equals(user.getPassword()))
            {
                if (User.canUsePassword(newpass, details))
                {
                    user.setPassword(User.preparePassword(newpass));
                    userService.save(user);

                    result = true;
                }
            }
            else details.put(fieldName, "incorrect");
        }
        return getResponse("success", result, details);
    }
}

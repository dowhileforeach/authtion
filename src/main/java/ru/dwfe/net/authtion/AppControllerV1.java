package ru.dwfe.net.authtion;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.access.prepost.PreAuthorize;
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
            User.prepareNewUser(user);

            //put user to the database
            userService.save(user);

            result = true;
        }

        //вернуть результат операции

        return getResponse("success", result, details);

    }

    @RequestMapping(API + "/confirm-user")
    public String confirmUser(@RequestParam String key) throws JsonProcessingException
    {
        boolean result = false;
        Map<String, Object> details = new HashMap<>();

        ConfirmationKey confirmationKey = confirmationKeyService.findByKey(key);
        if (confirmationKey != null)
        {
            Optional<User> byId = userService.findById(confirmationKey.getUser());
            if (byId.isPresent())
            {
                User user = byId.get();
                user.setAccountNonLocked(true);
                userService.save(user);

                //удалить key from database

                result = true;
            }
            else details.put("error", "user does not exist");
        }
        else details.put("error", "key does not exist");

        return getResponse("success", result, details);
    }


    /*
        UTILs
    */

    private Object getValueFromJSON(String body, String fieldName)
    {
        return JsonParserFactory.getJsonParser().parseMap(body).get(fieldName);
    }

    private static String getResponse(String resultFieldName, boolean responseResult, Map<String, Object> details) throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();

        if (details.size() == 0)

            return String.format("{" +
                    "\"%s\": %s" +
                    "}", resultFieldName, responseResult);

        else

            return String.format("{" +
                    "\"%s\": %s, " +
                    "\"details\": %s" +
                    "}", resultFieldName, responseResult, mapper.writeValueAsString(details));
    }
}

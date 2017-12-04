package ru.dwfe.authtion;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.dwfe.authtion.dao.User;
import ru.dwfe.authtion.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class AppControllerV1
{
    private static final String API = "/v1";

    @Autowired
    UserService userService;

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
    public String checkUserId(@RequestBody String body)
    {
        String id = (String) JsonParserFactory.getJsonParser().parseMap(body).get("id");
        boolean result = userService.existsById(id);

        return String.format("{" +
                "\"isFree\": %s" +
                "}", !result);
    }

    @RequestMapping(value = API + "/create-user", method = RequestMethod.POST)
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String addUser(@RequestBody User user) throws IOException
    {
        boolean result = false;

        //fields validation
        Map<String, String> check = User.check(user);
        if (check.isEmpty())

            //check user id
            if (userService.existsById(user.getId()))
                check.put("error", "user is present");
            else
            {
                //prepare
                User.prepareNewUser(user);

                //put user to the database

                result = true;
            }


        //вернуть результат операции

        return String.format("{" +
                "\"success\": %s, " +
                "\"details\": %s" +
                "}", result, new ObjectMapper().writeValueAsString(check));
    }

    @RequestMapping(API + "/confirm-user")
    public String confirmUser(@RequestParam String confirmkey)
    {
        boolean result = false;

        return String.format("{" +
                "\"success\": %s" +
                "}", result);
    }

}

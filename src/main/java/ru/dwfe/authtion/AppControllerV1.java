package ru.dwfe.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.dwfe.authtion.dao.User;
import ru.dwfe.authtion.service.UserService;

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
        boolean result = userService.findById(id).isPresent();

        return String.format("{" +
                "\"isFree\": %s" +
                "}", !result);
    }

    @RequestMapping(API + "/create-user")
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String addUser(@RequestBody String body)
    {
        boolean result = false;

        //parsing
        Map<String, Object> user = JsonParserFactory.getJsonParser().parseMap(body);

        //fields validation
        String id = (String) user.get("id");


        //put user to the database

        //вернуть результат операции

        return String.format("{" +
                "\"success\": %s," +
                "\"details\":" +
                "{" +
                "\"id\": \"%s\"" +
                "}" +
                "}", result, id);
    }

}

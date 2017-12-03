package ru.dwfe.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dwfe.authtion.dao.User;
import ru.dwfe.authtion.service.UserService;

import java.util.List;

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

    @RequestMapping(API + "/check-user-id")
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String checkUserId()
    {
        return "{\"success\": true}";
    }

    @RequestMapping(API + "/add-user")
    @PreAuthorize("hasAuthority('FRONTEND')")
    public String addUser()
    {
        return "{\"success\": true}";
    }

}

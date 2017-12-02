package ru.dwfe.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dwfe.authtion.dao.User;
import ru.dwfe.authtion.service.UserService;

import java.util.List;

@RestController
public class AppController
{
    @Autowired
    UserService userService;

    @RequestMapping("/public")
    public String publicResource()
    {
        return "{\"public\": true}";
    }

    @RequestMapping("/cities")
    @PreAuthorize("hasAuthority('USER')")
    public String cities()
    {
        return "{\"cities\": true}";
    }

    @RequestMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> users()
    {
        return userService.findAll();
    }

}

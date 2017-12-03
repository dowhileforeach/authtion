package ru.dwfe.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.dwfe.authtion.dao.User;
import ru.dwfe.authtion.service.UserService;

import java.util.List;

import static ru.dwfe.authtion.config.GlobalVariables.APIv1;

@RestController
public class AppController
{
    @Autowired
    UserService userService;

    @RequestMapping(APIv1 + "/public")
    public String publicResource()
    {
        return "{\"public\": true}";
    }

    @RequestMapping(APIv1 + "/cities")
    @PreAuthorize("hasAuthority('USER')")
    public String cities()
    {
        return "{\"cities\": true}";
    }

    @RequestMapping(APIv1 + "/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> users()
    {
        return userService.findAll();
    }

}

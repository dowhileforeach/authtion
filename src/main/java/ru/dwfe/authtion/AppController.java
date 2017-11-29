package ru.dwfe.authtion;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController
{
    @RequestMapping("/cities")
    @PreAuthorize("hasAuthority('USER')")
    public String getUser()
    {
        return "{\"successful\": true}";
    }

    @RequestMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public String getUsers()
    {
        return "{\"successful\": true}";
    }

}

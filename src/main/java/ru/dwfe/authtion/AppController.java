package ru.dwfe.authtion;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController
{
    @RequestMapping("/public")
    public String publicRecource()
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
    public String users()
    {
        return "{\"users\": true}";
    }

}

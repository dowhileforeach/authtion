package ru.dwfe.authtion;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.boot.actuate.audit.InMemoryAuditEventRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AppController
{
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
    public String users()
    {
        return "{\"users\": true}";
    }

}

package ru.dwfe.net.authtion.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.service.UserService;

import java.util.Date;
import java.util.List;

@Component
public class DevNULL_AuditEventRepository implements AuditEventRepository
{
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    @Override
    public void add(AuditEvent event)
    {
        log.error("rised Audit event ==>> {}", event);
    }

    @Override
    public List<AuditEvent> find(Date after)
    {
        return List.of();
    }

    @Override
    public List<AuditEvent> find(String principal, Date after)
    {
        return List.of();
    }

    @Override
    public List<AuditEvent> find(String principal, Date after, String type)
    {
        return List.of();
    }
}

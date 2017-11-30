package ru.dwfe.authtion.event;

import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class DevNULL_AuditEventRepository implements AuditEventRepository
{
    @Override
    public void add(AuditEvent event)
    {
        System.out.println("!ATTENTION! Audit event ==>> " + event);
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

package ru.dwfe.net.authtion.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.AuditEventRepository;
import org.springframework.stereotype.Component;
import ru.dwfe.net.authtion.service.ConsumerService;

import java.time.Instant;
import java.util.List;

@Component
public class DevNULL_AuditEventRepository implements AuditEventRepository
{
    private static final Logger log = LoggerFactory.getLogger(ConsumerService.class);

    @Override
    public void add(AuditEvent event)
    {
        log.error("rised Audit event ==>> {}", event);
    }

    @Override
    public List<AuditEvent> find(String principal, Instant after, String type)
    {
        return List.of();
    }
}

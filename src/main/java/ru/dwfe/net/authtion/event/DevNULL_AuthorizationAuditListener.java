package ru.dwfe.net.authtion.event;

import org.springframework.boot.actuate.security.AbstractAuthorizationAuditListener;
import org.springframework.security.access.event.AbstractAuthorizationEvent;
import org.springframework.stereotype.Component;

@Component
public class DevNULL_AuthorizationAuditListener extends AbstractAuthorizationAuditListener
{
  @Override
  public void onApplicationEvent(AbstractAuthorizationEvent event)
  {

  }
}

package ru.dwfe.net.authtion.event;

import org.springframework.boot.actuate.security.AbstractAuthenticationAuditListener;
import org.springframework.security.authentication.event.AbstractAuthenticationEvent;
import org.springframework.stereotype.Component;

@Component
public class DevNULL_AuthenticationAuditListener extends AbstractAuthenticationAuditListener
{
  @Override
  public void onApplicationEvent(AbstractAuthenticationEvent event)
  {
  }
}

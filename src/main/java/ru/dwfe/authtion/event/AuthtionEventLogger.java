package ru.dwfe.authtion.event;

import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.event.PublicInvocationEvent;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
public class AuthtionEventLogger
{
    /*
        https://docs.spring.io/spring/docs/5.0.2.RELEASE/spring-framework-reference/core.html#context-functionality-events
        http://www.baeldung.com/spring-boot-authentication-audit
        http://blog.codeleak.pl/2017/03/spring-boot-and-security-events-with-actuator.html
    */

    private void log(Map<String, String> map, String event, boolean success)
    {
        String result = map.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining(" \n"));

        System.out.printf("%n======================================================");
        System.out.printf("%n= %s, isSuccess -> %s %n", event, success);
        System.out.println("------------------------------------------------------");
        System.out.println(result);
    }


    private ZoneId zoneId = TimeZone.getDefault().toZoneId();
    private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String getDateTimeStr(long timestamp)
    {
        LocalDateTime triggerTime =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp),
                        zoneId);

        return triggerTime.format(dateTimeFormatter);
    }

    /*
        AUTHENTIFICATION events
    */

    @EventListener
    public void badCredentials(AuthenticationFailureBadCredentialsEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureBadCredentials");
    }

    @EventListener
    public void failureCredentialsExpired(AuthenticationFailureCredentialsExpiredEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureCredentialsExpired");
    }

    @EventListener
    public void failureDisabled(AuthenticationFailureDisabledEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureDisabled");
    }

    @EventListener
    public void failureExpired(AuthenticationFailureExpiredEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureExpired");
    }

    @EventListener
    public void failureLocked(AuthenticationFailureLockedEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureLocked");
    }

    @EventListener
    public void failureProviderNotFound(AuthenticationFailureProviderNotFoundEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureProviderNotFound");
    }

    @EventListener
    public void failureProxyUntrusted(AuthenticationFailureProxyUntrustedEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureProxyUntrusted");
    }

    @EventListener
    public void failureServiceException(AuthenticationFailureServiceExceptionEvent event)
    {
        authenticationFailureEvent(event, "AuthenticationFailureServiceException");
    }

    @EventListener
    public void authenticationSuccess(AuthenticationSuccessEvent event)
    {
        authenticationSuccessEvent(event, "AuthenticationSuccess");
    }

    @EventListener
    public void interactiveAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event)
    {
        authenticationSuccessEvent(event, "InteractiveAuthenticationSuccess");
    }

    private Map<String, String> parseSuccessAuthentication(AbstractAuthenticationEvent event)
    {
        Map<String, String> map = new HashMap<>();

        map.put("timestamp", getDateTimeStr(event.getTimestamp()));

        Authentication authentication = event.getAuthentication();
        if (authentication != null)
        {
            //map.put("principal", authentication.getPrincipal().toString());
            //map.put("authorities", authentication.getAuthorities().toString());
            map.put("details", authentication.getDetails().toString());
        }

        map.put("source", event.getSource().toString());

        return map;
    }

    private Map<String, String> parseFailureAuthentication(AbstractAuthenticationFailureEvent event)
    {
        Map<String, String> map = new HashMap<>();

        map.put("timestamp", getDateTimeStr(event.getTimestamp()));

        Authentication authentication = event.getAuthentication();
        if (authentication != null)
        {
            map.put("principal", authentication.getPrincipal().toString());
            map.put("authorities", authentication.getAuthorities().toString());
            map.put("details", authentication.getDetails().toString());
        }

        map.put("source", event.getSource().toString());

        AuthenticationException exception = event.getException();
        if (exception != null)
        {
            map.put("exceptionClass", exception.getClass().getSimpleName());
            map.put("exceptionMessage", exception.getMessage());
        }

        return map;
    }

    private void authenticationSuccessEvent(AbstractAuthenticationEvent event, String eventStr)
    {
        Map<String, String> map = parseSuccessAuthentication(event);
        log(map, eventStr, true);
    }

    private void authenticationFailureEvent(AbstractAuthenticationFailureEvent event, String eventStr)
    {
        Map<String, String> map = parseFailureAuthentication(event);
        log(map, eventStr, false);
    }


    /*
        ACCESS events
    */


    @EventListener
    public void credentialsNotFound(AuthenticationCredentialsNotFoundEvent event)
    {
        Map<String, String> map = new HashMap<>();
        map.put("timestamp", getDateTimeStr(event.getTimestamp()));
        map.put("source", event.getSource().toString());
        map.put("configAttributes", event.getConfigAttributes().toString());

        Exception exception = event.getCredentialsNotFoundException();
        if (exception != null)
        {
            map.put("exceptionClass", exception.getClass().getSimpleName());
            map.put("exceptionMessage", exception.getMessage());
        }

        log(map, "CredentialsNotFound", false);
    }

    @EventListener
    public void authorizationFailure(AuthorizationFailureEvent event)
    {
        Map<String, String> map = new HashMap<>();
        map.put("timestamp", getDateTimeStr(event.getTimestamp()));
        map.put("source", event.getSource().toString());
        map.put("configAttributes", event.getConfigAttributes().toString());

        Authentication authentication = event.getAuthentication();
        if (authentication != null)
        {
            map.put("principal", authentication.getPrincipal().toString());
            map.put("authorities", authentication.getAuthorities().toString());
            map.put("details", authentication.getDetails().toString());
        }

        Exception exception = event.getAccessDeniedException();
        if (exception != null)
        {
            map.put("exceptionClass", exception.getClass().getSimpleName());
            map.put("exceptionMessage", exception.getMessage());
        }

        log(map, "AuthorizationFailure", false);
    }


    @EventListener
    public void authorizedEvent(AuthorizedEvent event)
    {
        Map<String, String> map = new HashMap<>();
        map.put("timestamp", getDateTimeStr(event.getTimestamp()));
        map.put("source", event.getSource().toString());
        map.put("configAttributes", event.getConfigAttributes().toString());

        Authentication authentication = event.getAuthentication();
        if (authentication != null)
        {
            map.put("principal", authentication.getPrincipal().toString());
            map.put("authorities", authentication.getAuthorities().toString());
            map.put("details", authentication.getDetails().toString());
        }

        log(map, "Authorized", true);
    }

    @EventListener
    public void publicInvocation(PublicInvocationEvent event)
    {
        Map<String, String> map = new HashMap<>();

        map.put("timestamp", getDateTimeStr(event.getTimestamp()));
        map.put("source", event.getSource().toString());

        log(map, "PublicInvocation", true);
    }

}

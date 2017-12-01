package ru.dwfe.authtion.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.event.AuthenticationCredentialsNotFoundEvent;
import org.springframework.security.access.event.AuthorizationFailureEvent;
import org.springframework.security.access.event.AuthorizedEvent;
import org.springframework.security.access.event.PublicInvocationEvent;
import org.springframework.security.authentication.event.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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

        Справедливо для всех положительных Authentication событий:
            - для Client'а не работают
            - возникают при каждом успешном авторизованном доступе к ресурсу
                -- но при этом event не содержит имени ресурса.
                   То есть успешный доступ есть, но к чему был успешно получен доступ - не известно
        в связи с этим анализировать успешные Authentication события я сейчас не вижу смысла.
    */

    private void log(Map<String, String> map, String event, boolean success)
    {
        String result = map.entrySet().stream()
                .map(e -> e.getKey() + " = " + e.getValue())
                .collect(Collectors.joining("\r\n"));

        System.out.printf("%n======================================================");
        System.out.printf("%n= %s, isSuccess -> %s %n", event, success);
        System.out.println("------------------------------------------------------");
        System.out.println(result);
    }


    /*
        AUTHENTIFICATION events
    */

    @EventListener
    public void badCredentials(AuthenticationFailureBadCredentialsEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureBadCredentials");
    }

    @EventListener
    public void failureCredentialsExpired(AuthenticationFailureCredentialsExpiredEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureCredentialsExpired");
    }

    @EventListener
    public void failureDisabled(AuthenticationFailureDisabledEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureDisabled");
    }

    @EventListener
    public void failureExpired(AuthenticationFailureExpiredEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureExpired");
    }

    @EventListener
    public void failureLocked(AuthenticationFailureLockedEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureLocked");
    }

    @EventListener
    public void failureProviderNotFound(AuthenticationFailureProviderNotFoundEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureProviderNotFound");
    }

    @EventListener
    public void failureProxyUntrusted(AuthenticationFailureProxyUntrustedEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureProxyUntrusted");
    }

    @EventListener
    public void failureServiceException(AuthenticationFailureServiceExceptionEvent event) throws Exception
    {
        authenticationFailureEvent(event, "AuthenticationFailureServiceException");
    }

//    @EventListener
//    public void authenticationSuccess(AuthenticationSuccessEvent event) throws Exception
//    {
//        authenticationSuccessEvent(event, "AuthenticationSuccess");
//    }
//
//    @EventListener
//    public void interactiveAuthenticationSuccess(InteractiveAuthenticationSuccessEvent event) throws Exception
//    {
//        authenticationSuccessEvent(event, "InteractiveAuthenticationSuccess");
//    }

    private Map<String, String> parseSuccessAuthentication(AbstractAuthenticationEvent event) throws Exception
    {
        return commonParse(new String[]{"timestamp", "source", "details"}, event);
    }

    private Map<String, String> parseFailureAuthentication(AbstractAuthenticationFailureEvent event) throws Exception
    {
        Map<String, String> map = commonParse(new String[]{"timestamp", "source", "principal", "authorities", "details"}, event);
        addException(map, event.getException());
        return map;
    }

    private void authenticationSuccessEvent(AbstractAuthenticationEvent event, String eventStr) throws Exception
    {
        Map<String, String> map = parseSuccessAuthentication(event);
        log(map, eventStr, true);
    }

    private void authenticationFailureEvent(AbstractAuthenticationFailureEvent event, String eventStr) throws Exception
    {
        Map<String, String> map = parseFailureAuthentication(event);
        log(map, eventStr, false);
    }


    /*
        ACCESS events
    */

    @EventListener
    public void access1(AuthenticationCredentialsNotFoundEvent event) throws Exception
    {
        Map<String, String> map = commonParse(new String[]{"timestamp", "source", "configAttributes"}, event);
        addException(map, event.getCredentialsNotFoundException());
        log(map, "CredentialsNotFound", false);
    }

    @EventListener
    public void access2(AuthorizationFailureEvent event) throws Exception
    {
        Map<String, String> map = commonParse(new String[]{"timestamp", "source", "configAttributes", "principal", "authorities", "details"}, event);
        addException(map, event.getAccessDeniedException());
        log(map, "AuthorizationFailure", false);
    }

    @EventListener
    public void access3(AuthorizedEvent event) throws Exception
    {
        Map<String, String> map = commonParse(new String[]{"timestamp", "source", "configAttributes", "principal", "authorities", "details"}, event);
        log(map, "Authorized", true);
    }

    @EventListener
    public void access4(PublicInvocationEvent event) throws Exception
    {
        Map<String, String> map = commonParse(new String[]{"timestamp", "source"}, event);
        log(map, "PublicInvocation", true);
    }


    /*
        UTILs
    */

    private Map<String, String> commonParse(String[] arr, ApplicationEvent event) throws Exception
    {
        Map<String, String> map = new HashMap<>();
        Authentication authentication = null;

        for (String next : arr)

            if ("timestamp".equals(next))
            {
                map.put("timestamp", getDateTimeStr(event.getTimestamp()));
            }
            else if ("source".equals(next))
            {
                map.put("source", event.getSource().toString());
            }
            else if ("configAttributes".equals(next))
            {
                String configAttributes = event.getClass().getMethod("getConfigAttributes").invoke(event).toString();
                map.put("configAttributes", configAttributes);
            }
            else if ("principal".equals(next))
            {
                if (authentication == null) authentication = getAuthentication(event);
                if (authentication != null)
                    map.put("principal", authentication.getPrincipal().toString());
            }
            else if ("authorities".equals(next))
            {
                if (authentication == null) authentication = getAuthentication(event);
                if (authentication != null)
                    map.put("authorities", authentication.getAuthorities().toString());
            }
            else if ("details".equals(next))
            {
                if (authentication == null) authentication = getAuthentication(event);
                if (authentication != null)
                    map.put("details", authentication.getDetails().toString());
            }
        return map;
    }

    private Authentication getAuthentication(ApplicationEvent event) throws Exception
    {
        return (Authentication) event.getClass().getMethod("getAuthentication").invoke(event);
    }

    private void addException(Map<String, String> map, Exception exception)
    {
        if (exception != null)
        {
            map.put("exceptionClass", exception.getClass().getSimpleName());
            map.put("exceptionMessage", exception.getMessage());
        }
    }

    private static final ZoneId zoneId = TimeZone.getDefault().toZoneId();
    private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private String getDateTimeStr(long timestamp)
    {
        LocalDateTime triggerTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), zoneId);
        return triggerTime.format(dateTimeFormatter);
    }


}

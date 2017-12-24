package ru.dwfe.net.authtion.test_util;

import ru.dwfe.net.authtion.Global;

import static ru.dwfe.net.authtion.test_util.AuthorityLevel.*;

public class Variables_Global
{
    public static final String PROTOCOL_HOST_PORT = "http://localhost:8080";
    public static final String ALL_BEFORE_RESOURCE = PROTOCOL_HOST_PORT + Global.API_CURRENT_VERSION;

    /*
        Clents to get access to resource /sign-in
    */
    public static final Client client_TRUSTED = Client.of("Trusted", "trPass", 864_000, 180);
    public static final Client client_UNTRUSTED = Client.of("Untrusted", "untrPass", 180, 0);
    public static final Client client_FRONTEND = Client.of("Frontend", "frntndPass", 1_728_000, 864_000);

    /*
        Consumers from backend database
    */
    public static final ConsumerTest USER_consumer = ConsumerTest.of(USER, "user@ya.ru", "passUser", client_TRUSTED, 200);
    public static final ConsumerTest ADMIN_consumer = ConsumerTest.of(ADMIN, "admin@ya.ru", "passAdmin", client_UNTRUSTED, 200);
    public static final ConsumerTest FRONTEND_consumer = ConsumerTest.of(FRONTEND, "shop@ya.ru", "passFrontend", client_FRONTEND, 200);
    public static final ConsumerTest ANONYMOUS_consumer = ConsumerTest.getAnonymous();
}

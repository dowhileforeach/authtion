package ru.dwfe.net.authtion.test;

import org.springframework.boot.json.JsonParserFactory;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthtionTestVariablesForConsumerTest
{
  public static final String EMAIL_NEW_Consumer = "notExistedConsumer@ya.ru";
  public static final String EMAIL_2_NEW_Consumer = "not_02_ExistedConsumer@ya.ru";
  public static final String PASS_NEW_Consumer = "123456";
  public static final String NEWPASS_NEW_Consumer = "1234567890";

  public static final String EMAIL_3_NEW_Consumer = "not_3_ExistedConsumer@ya.ru";
  public static final String PASS_FOR_EMAIL_3_Consumer_Encoded = "$2a$10$AvHrvvqQNOyUZxg7XMfDleDLjR3AV5C1KEwsa29EC4Eo7CYIe0eoy"; //hello123world
  public static final String PASS_FOR_EMAIL_3_Consumer_Decoded = "hello123world";
  public static final String NEWPASS_FOR_EMAIL_3_Consumer_Encoded = "$2a$10$EGQlh6wWYUFrVbnZJzMvwOnGSxlS65Oap.6l92nA3PLskkipat7Di"; //56789900aloha
  public static final String NEWPASS_FOR_EMAIL_3_Consumer_Decoded = "56789900aloha";



  /* BODIES */

  public static final List<AuthtionTestChecker> checkers_for_checkConsumerEmail = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-email"),
          AuthtionTestChecker.of(false, Map.of("email", ""), 200, "empty-email"),
          AuthtionTestChecker.of(false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "exceeded-max50-email-length"),
          AuthtionTestChecker.of(false, Map.of("email", "user"), 200, "invalid-email"),
          AuthtionTestChecker.of(false, Map.of("email", ".uuqu@mail.ru"), 200, "invalid-email"),
          AuthtionTestChecker.of(false, Map.of("email", "test2@dwfe.ru"), 200, "email-present-in-database"),
          AuthtionTestChecker.of(true, Map.of("email", EMAIL_NEW_Consumer), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_checkConsumerPass = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-password"),
          AuthtionTestChecker.of(false, Map.of("password", ""), 200, "empty-password"),
          AuthtionTestChecker.of(false, Map.of("password", "12345"), 200, "exceeded-min6-or-max55-password-length"),
          AuthtionTestChecker.of(false, Map.of("password", "12345678901234567890123456789012345678901234567890123456"), 200, "exceeded-min6-or-max55-password-length"),
          AuthtionTestChecker.of(true, Map.of("password", "123456"), 200)
  );

  public static List<AuthtionTestChecker> checkers_for_createConsumer()
  {
    List<AuthtionTestChecker> list = new ArrayList<>(List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-email"),
            AuthtionTestChecker.of(false, Map.of("email", ""), 200, "empty-email"),
            AuthtionTestChecker.of(false, Map.of("email", "ogygyg_bnmkkskslwlwllogygyg_bnmkkskslwlwll@gmail.com"), 200, "exceeded-max50-email-length"),
            AuthtionTestChecker.of(false, Map.of("email", "user"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("email", "@puqu@mail.ru"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("email", "test1@dwfe.ru"), 200, "email-present-in-database")
    ));
    list.addAll(List.of(
            AuthtionTestChecker.of(true, Map.of("email", EMAIL_2_NEW_Consumer, "firstName", "ozon"), 200),
            AuthtionTestChecker.of(false, Map.of("email", EMAIL_2_NEW_Consumer), 200, "email-present-in-database"),
            AuthtionTestChecker.of(false, Map.of("email", EMAIL_NEW_Consumer, "password", ""), 200, "empty-password"),
            AuthtionTestChecker.of(false, Map.of("email", EMAIL_NEW_Consumer, "password", "54321"), 200, "exceeded-min6-or-max55-password-length"),
            AuthtionTestChecker.of(false, Map.of("email", EMAIL_NEW_Consumer, "password", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "exceeded-min6-or-max55-password-length"),
            AuthtionTestChecker.of(true, Map.of("email", EMAIL_NEW_Consumer, "password", PASS_NEW_Consumer, "nickName", "nobody", "lastName", "sunshine"), 200),
            AuthtionTestChecker.of(true, Map.of("email", EMAIL_3_NEW_Consumer, "password", PASS_FOR_EMAIL_3_Consumer_Encoded, "nickName", "hello world"), 200)
    ));
    return list;
  }

  public static final List<AuthtionTestChecker> checkers_for_updateConsumer1 = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateConsumer2 = List.of(
          AuthtionTestChecker.of(true, Map.of("nickName", "user", "firstName", "", "lastName", ""), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateConsumer3 = List.of(
          AuthtionTestChecker.of(true, Map.of("nickName", "hello", "firstName", "1"), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateConsumer4 = List.of(
          AuthtionTestChecker.of(true, Map.of("lastName", "2"), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateConsumer5 = List.of(
          AuthtionTestChecker.of(true, Map.of("nickName", "good", "firstName", "alto", "lastName", "smith"), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_getConsumerData = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200, JsonParserFactory.getJsonParser().parseMap("{\n" +
                  " \"id\": 1001,\n" +
                  " \"email\": \"test2@dwfe.ru\",\n" +
                  " \"password\": \"****\",\n" +
                  " \"authorities\": [\"USER\"],\n" +
                  " \"nickName\": \"user\",\n" +
                  " \"firstName\": \"\",\n" +
                  " \"lastName\": \"\",\n" +
                  " \"accountNonExpired\": true,\n" +
                  " \"credentialsNonExpired\": true,\n" +
                  " \"accountNonLocked\": true,\n" +
                  " \"enabled\": true,\n" +
                  " \"emailConfirmed\": true,\n" +
                  " \"createdOn\": " + "\"date\",\n" +
                  " \"updatedOn\": " + "\"date\"\n" +
                  "}"))
  );

  public static final List<AuthtionTestChecker> checkers_for_publicConsumer_9 = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "id-not-exist")
  );
  public static final List<AuthtionTestChecker> checkers_for_publicConsumer_1 = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200, Map.of("id", 1000, "nickName", "test1"))
  );

  public static final List<AuthtionTestChecker> checkers_for_reqConfirmConsumerEmail = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200)
  );

  public static List<AuthtionTestChecker> checkers_for_confirmConsumerEmail(String existedKey)
  {
    return List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", ""), 200, "empty-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", "123"), 200, "confirm-key-not-exist"),
            AuthtionTestChecker.of(true, Map.of("key", existedKey), 200)
    );
  }

  public static final List<AuthtionTestChecker> checkers_for_changeConsumerPass = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", ""), 200, "empty-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_NEW_Consumer), 200, "missing-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_NEW_Consumer, "newpass", ""), 200, "empty-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_NEW_Consumer, "newpass", "12345"), 200, "exceeded-min6-or-max55-newpass-length"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_NEW_Consumer + "1", "newpass", "1234567"), 200, "wrong-oldpass"),
          AuthtionTestChecker.of(true, Map.of("oldpass", PASS_NEW_Consumer, "newpass", NEWPASS_NEW_Consumer), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_changeConsumerPass_3 = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", ""), 200, "empty-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded), 200, "missing-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded, "newpass", ""), 200, "empty-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded + "1", "newpass", "1234567"), 200, "wrong-oldpass"),
          AuthtionTestChecker.of(true, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded, "newpass", NEWPASS_FOR_EMAIL_3_Consumer_Encoded), 200)
  );

  public static List<AuthtionTestChecker> checkers_for_reqRestoreConsumerPass(String email)
  {
    return List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-email"),
            AuthtionTestChecker.of(false, Map.of("email", ""), 200, "empty-email"),
            AuthtionTestChecker.of(false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "exceeded-max50-email-length"),
            AuthtionTestChecker.of(false, Map.of("email", "admin"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("email", "@uu@mail.ru"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("email", "!uu@mail.ru"), 200, "email-not-exist"),
            AuthtionTestChecker.of(true, Map.of("email", email), 200)
    );
  }

  public static List<AuthtionTestChecker> checkers_for_confirmRestoreConsumerPass(String email, String existedKey)
  {
    return List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", ""), 200, "empty-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", "657"), 200, "confirm-key-not-exist"),
            AuthtionTestChecker.of(true, Map.of("key", existedKey), 200, Map.of("email", email, "key", existedKey))
    );
  }

  public static List<AuthtionTestChecker> checkers_for_restoreConsumerPass(String email, String newpass, String existedKey)
  {
    List<AuthtionTestChecker> list = new ArrayList<>(List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-newpass"),
            AuthtionTestChecker.of(false, Map.of("newpass", ""), 200, "empty-newpass")
    ));

    if (!AuthtionConsumer.isPasswordBcrypted(newpass))
    {
      list.addAll(List.of(
              AuthtionTestChecker.of(false, Map.of("newpass", "54321"), 200, "exceeded-min6-or-max55-newpass-length"),
              AuthtionTestChecker.of(false, Map.of("newpass", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "exceeded-min6-or-max55-newpass-length")
      ));
    }
    list.addAll(List.of(
            AuthtionTestChecker.of(false, Map.of("newpass", newpass), 200, "missing-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", ""), 200, "empty-confirm-key")
    ));
    list.addAll(List.of(
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", "123"), 200, "missing-email"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", "123", "email", ""), 200, "empty-email"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", "123", "email", "lllgyg_bnmkksk12345llogygyg_bnmkkskslwlwll@gmail.com"), 200, "exceeded-max50-email-length"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", "123", "email", "shop"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", "123", "email", "..puqu@mail.ru"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", "123", "email", "ehlo@mail.ru"), 200, "confirm-key-not-exist"),
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", existedKey, "email", "ehlo@mail.ru"), 200, "confirm-key-for-another-email"),
            AuthtionTestChecker.of(true, Map.of("newpass", newpass, "key", existedKey, "email", email), 200)
    ));
    return list;
  }
}
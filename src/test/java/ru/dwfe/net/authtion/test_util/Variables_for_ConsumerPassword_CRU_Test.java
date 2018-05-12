package ru.dwfe.net.authtion.test_util;

import org.springframework.boot.json.JsonParserFactory;
import ru.dwfe.net.authtion.dao.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Variables_for_ConsumerPassword_CRU_Test
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

  public static final List<Checker> checkers_for_checkConsumerEmail = List.of(
          Checker.of(false, Map.of(), 200, "missing-email"),
          Checker.of(false, Map.of("email", ""), 200, "empty-email"),
          Checker.of(false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "exceeded-max50-email-length"),
          Checker.of(false, Map.of("email", "user"), 200, "invalid-email"),
          Checker.of(false, Map.of("email", ".uuqu@mail.ru"), 200, "invalid-email"),
          Checker.of(false, Map.of("email", "user@ya.ru"), 200, "email-present-in-database"),
          Checker.of(true, Map.of("email", EMAIL_NEW_Consumer), 200)
  );

  public static final List<Checker> checkers_for_checkConsumerPass = List.of(
          Checker.of(false, Map.of(), 200, "missing-password"),
          Checker.of(false, Map.of("password", ""), 200, "empty-password"),
          Checker.of(false, Map.of("password", "12345"), 200, "exceeded-min6-or-max55-password-length"),
          Checker.of(false, Map.of("password", "12345678901234567890123456789012345678901234567890123456"), 200, "exceeded-min6-or-max55-password-length"),
          Checker.of(true, Map.of("password", "123456"), 200)
  );

  public static List<Checker> checkers_for_createConsumer()
  {
    List<Checker> list = new ArrayList<>(List.of(
            Checker.of(false, Map.of(), 200, "missing-email"),
            Checker.of(false, Map.of("email", ""), 200, "empty-email"),
            Checker.of(false, Map.of("email", "ogygyg_bnmkkskslwlwllogygyg_bnmkkskslwlwll@gmail.com"), 200, "exceeded-max50-email-length"),
            Checker.of(false, Map.of("email", "user"), 200, "invalid-email"),
            Checker.of(false, Map.of("email", "@puqu@mail.ru"), 200, "invalid-email"),
            Checker.of(false, Map.of("email", "admin@ya.ru"), 200, "email-present-in-database")
    ));
    list.addAll(List.of(
            Checker.of(true, Map.of("email", EMAIL_2_NEW_Consumer, "firstName", "ozon"), 200),
            Checker.of(false, Map.of("email", EMAIL_2_NEW_Consumer), 200, "email-present-in-database"),
            Checker.of(false, Map.of("email", EMAIL_NEW_Consumer, "password", ""), 200, "empty-password"),
            Checker.of(false, Map.of("email", EMAIL_NEW_Consumer, "password", "54321"), 200, "exceeded-min6-or-max55-password-length"),
            Checker.of(false, Map.of("email", EMAIL_NEW_Consumer, "password", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "exceeded-min6-or-max55-password-length"),
            Checker.of(true, Map.of("email", EMAIL_NEW_Consumer, "password", PASS_NEW_Consumer, "nickName", "nobody"), 200),
            Checker.of(true, Map.of("email", EMAIL_3_NEW_Consumer, "password", PASS_FOR_EMAIL_3_Consumer_Encoded, "nickName", "hello world"), 200)
    ));
    return list;
  }

  public static final List<Checker> checkers_for_updateConsumer = List.of(
//            Checker.of(false, Map.of(), 200, "details", "warning", "no changes found"),
//            Checker.of(false, Map.of("nickName", "user"), 200, "details", "warning", "no changes found"),
//            Checker.of(false, Map.of("nickName", "user", "firstName", ""), 200, "details", "warning", "no changes found"),
//            Checker.of(true, Map.of("nickName", "Consumer", "firstName", "", "lastName", ""), 200, Map.of("nickName", "change saved")),
//            Checker.of(true, Map.of("nickName", "Consumer", "firstName", "1", "lastName", ""), 200, Map.of("firstName", "change saved")),
//            Checker.of(true, Map.of("nickName", "Consumer", "firstName", "1", "lastName", "2"), 200, Map.of("lastName", "change saved")),
//            Checker.of(true, Map.of("nickName", "user", "firstName", "", "lastName", ""), 200, Map.of("lastName", "change saved", "nickName", "change saved", "firstName", "change saved"))
  );

  public static final List<Checker> checkers_for_getConsumerData = List.of(
          Checker.of(true, Map.of(), 200, JsonParserFactory.getJsonParser().parseMap("{\n" +
                  " \"id\": 555,\n" +
                  " \"email\": \"user@ya.ru\",\n" +
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

  public static final List<Checker> checkers_for_publicConsumer_9 = List.of(
          Checker.of(false, Map.of(), 200, "id-not-exist")
  );
  public static final List<Checker> checkers_for_publicConsumer_1 = List.of(
          Checker.of(true, Map.of(), 200, Map.of("id", 1, "nickName", "admin"))
  );

  public static final List<Checker> checkers_for_reqConfirmConsumerEmail = List.of(
          Checker.of(true, Map.of(), 200)
  );

  public static List<Checker> checkers_for_confirmConsumerEmail(String existedKey)
  {
    return List.of(
            Checker.of(false, Map.of(), 200, "missing-confirm-key"),
            Checker.of(false, Map.of("key", ""), 200, "empty-confirm-key"),
            Checker.of(false, Map.of("key", "123"), 200, "confirm-key-not-exist"),
            Checker.of(true, Map.of("key", existedKey), 200)
    );
  }

  public static final List<Checker> checkers_for_changeConsumerPass = List.of(
          Checker.of(false, Map.of(), 200, "missing-oldpass"),
          Checker.of(false, Map.of("oldpass", ""), 200, "empty-oldpass"),
          Checker.of(false, Map.of("oldpass", PASS_NEW_Consumer), 200, "missing-newpass"),
          Checker.of(false, Map.of("oldpass", PASS_NEW_Consumer, "newpass", ""), 200, "empty-newpass"),
          Checker.of(false, Map.of("oldpass", PASS_NEW_Consumer, "newpass", "12345"), 200, "exceeded-min6-or-max55-newpass-length"),
          Checker.of(false, Map.of("oldpass", PASS_NEW_Consumer + "1", "newpass", "1234567"), 200, "wrong-oldpass"),
          Checker.of(true, Map.of("oldpass", PASS_NEW_Consumer, "newpass", NEWPASS_NEW_Consumer), 200)
  );

  public static final List<Checker> checkers_for_changeConsumerPass_3 = List.of(
          Checker.of(false, Map.of(), 200, "missing-oldpass"),
          Checker.of(false, Map.of("oldpass", ""), 200, "empty-oldpass"),
          Checker.of(false, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded), 200, "missing-newpass"),
          Checker.of(false, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded, "newpass", ""), 200, "empty-newpass"),
          Checker.of(false, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded + "1", "newpass", "1234567"), 200, "wrong-oldpass"),
          Checker.of(true, Map.of("oldpass", PASS_FOR_EMAIL_3_Consumer_Decoded, "newpass", NEWPASS_FOR_EMAIL_3_Consumer_Encoded), 200)
  );

  public static List<Checker> checkers_for_reqRestoreConsumerPass(String email)
  {
    return List.of(
            Checker.of(false, Map.of(), 200, "missing-email"),
            Checker.of(false, Map.of("email", ""), 200, "empty-email"),
            Checker.of(false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "exceeded-max50-email-length"),
            Checker.of(false, Map.of("email", "admin"), 200, "invalid-email"),
            Checker.of(false, Map.of("email", "@uu@mail.ru"), 200, "invalid-email"),
            Checker.of(false, Map.of("email", "!uu@mail.ru"), 200, "email-not-exist"),
            Checker.of(true, Map.of("email", email), 200)
    );
  }

  public static List<Checker> checkers_for_confirmRestoreConsumerPass(String email, String existedKey)
  {
    return List.of(
            Checker.of(false, Map.of(), 200, "missing-confirm-key"),
            Checker.of(false, Map.of("key", ""), 200, "empty-confirm-key"),
            Checker.of(false, Map.of("key", "657"), 200, "confirm-key-not-exist"),
            Checker.of(true, Map.of("key", existedKey), 200, Map.of("email", email, "key", existedKey))
    );
  }

  public static List<Checker> checkers_for_restoreConsumerPass(String email, String newpass, String existedKey)
  {
    List<Checker> list = new ArrayList<>(List.of(
            Checker.of(false, Map.of(), 200, "missing-newpass"),
            Checker.of(false, Map.of("newpass", ""), 200, "empty-newpass")
    ));

    if (!Consumer.isPasswordBcrypted(newpass))
    {
      list.addAll(List.of(
              Checker.of(false, Map.of("newpass", "54321"), 200, "exceeded-min6-or-max55-newpass-length"),
              Checker.of(false, Map.of("newpass", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "exceeded-min6-or-max55-newpass-length")
      ));
    }
    list.addAll(List.of(
            Checker.of(false, Map.of("newpass", newpass), 200, "missing-confirm-key"),
            Checker.of(false, Map.of("newpass", newpass, "key", ""), 200, "empty-confirm-key")
    ));
    list.addAll(List.of(
            Checker.of(false, Map.of("newpass", newpass, "key", "123"), 200, "missing-email"),
            Checker.of(false, Map.of("newpass", newpass, "key", "123", "email", ""), 200, "empty-email"),
            Checker.of(false, Map.of("newpass", newpass, "key", "123", "email", "lllgyg_bnmkksk12345llogygyg_bnmkkskslwlwll@gmail.com"), 200, "exceeded-max50-email-length"),
            Checker.of(false, Map.of("newpass", newpass, "key", "123", "email", "shop"), 200, "invalid-email"),
            Checker.of(false, Map.of("newpass", newpass, "key", "123", "email", "..puqu@mail.ru"), 200, "invalid-email"),
            Checker.of(false, Map.of("newpass", newpass, "key", "123", "email", "ehlo@mail.ru"), 200, "confirm-key-not-exist"),
            Checker.of(false, Map.of("newpass", newpass, "key", existedKey, "email", "ehlo@mail.ru"), 200, "confirm-key-for-another-email"),
            Checker.of(true, Map.of("newpass", newpass, "key", existedKey, "email", email), 200)
    ));
    return list;
  }
}
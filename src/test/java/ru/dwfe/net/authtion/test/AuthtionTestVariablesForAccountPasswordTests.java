package ru.dwfe.net.authtion.test;

import org.springframework.boot.json.JsonParserFactory;
import ru.dwfe.net.authtion.dao.AuthtionConsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthtionTestVariablesForAccountPasswordTests
{


  public static final String Account1_Email = "test1@dwfe.ru";
  public static final String Account2_Email = "test2@dwfe.ru";

  public static final String Account3_Email = "test3@dwfe.ru";
  public static final String Account3_Pass = "123456";
  public static final String Account3_NewPass = "1234567890";

  public static final String Account4_Email = "test4@dwfe.ru";

  public static final String Account5_Email = "test5@dwfe.ru";
  public static final String Account5_Pass_Decoded = "hello123world";
  public static final String Account5_Pass_Encoded = "$2a$10$AvHrvvqQNOyUZxg7XMfDleDLjR3AV5C1KEwsa29EC4Eo7CYIe0eoy"; //hello123world
  public static final String Account5_NewPass_Decoded = "56789900aloha";
  public static final String Account5_NewPass_Encoded = "$2a$10$EGQlh6wWYUFrVbnZJzMvwOnGSxlS65Oap.6l92nA3PLskkipat7Di"; //56789900aloha



  /* BODIES */

  public static final List<AuthtionTestChecker> checkers_for_checkEmail = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-email"),
          AuthtionTestChecker.of(false, Map.of("email", ""), 200, "empty-email"),
          AuthtionTestChecker.of(false, Map.of("email", "123456789012345678901234567890kkklkklklklkklklklklklklklk"), 200, "exceeded-max50-email-length"),
          AuthtionTestChecker.of(false, Map.of("email", "user"), 200, "invalid-email"),
          AuthtionTestChecker.of(false, Map.of("email", ".uuqu@mail.ru"), 200, "invalid-email"),
          AuthtionTestChecker.of(false, Map.of("email", Account2_Email), 200, "email-present-in-database"),
          AuthtionTestChecker.of(true, Map.of("email", Account3_Email), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_checkPass = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-password"),
          AuthtionTestChecker.of(false, Map.of("password", ""), 200, "empty-password"),
          AuthtionTestChecker.of(false, Map.of("password", "12345"), 200, "exceeded-min6-or-max55-password-length"),
          AuthtionTestChecker.of(false, Map.of("password", "12345678901234567890123456789012345678901234567890123456"), 200, "exceeded-min6-or-max55-password-length"),
          AuthtionTestChecker.of(true, Map.of("password", "123456"), 200),
          AuthtionTestChecker.of(true, Map.of("password", Account5_Pass_Encoded), 200)
  );

  public static List<AuthtionTestChecker> checkers_for_createAccount()
  {
    List<AuthtionTestChecker> list = new ArrayList<>(List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-email"),
            AuthtionTestChecker.of(false, Map.of("email", ""), 200, "empty-email"),
            AuthtionTestChecker.of(false, Map.of("email", "ogygyg_bnmkkskslwlwllogygyg_bnmkkskslwlwll@gmail.com"), 200, "exceeded-max50-email-length"),
            AuthtionTestChecker.of(false, Map.of("email", "user"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("email", "@puqu@mail.ru"), 200, "invalid-email"),
            AuthtionTestChecker.of(false, Map.of("email", Account1_Email), 200, "email-present-in-database")
    ));
    list.addAll(List.of(
            AuthtionTestChecker.of(true, Map.of("email", Account4_Email, "firstName", "ozon", "dateOfBirth", "1980-11-27"), 200),

            AuthtionTestChecker.of(false, Map.of("email", Account4_Email), 200, "email-present-in-database"),
            AuthtionTestChecker.of(false, Map.of("email", Account3_Email, "password", ""), 200, "empty-password"),
            AuthtionTestChecker.of(false, Map.of("email", Account3_Email, "password", "54321"), 200, "exceeded-min6-or-max55-password-length"),
            AuthtionTestChecker.of(false, Map.of("email", Account3_Email, "password", "ex24g23grvtbm56m567nc445xv34ecq3z34vwxtn6n364nb345b4554b"), 200, "exceeded-min6-or-max55-password-length"),

            AuthtionTestChecker.of(true, Map.of("email", Account3_Email, "password", Account3_Pass, "nickName", "nobody", "lastName", "sunshine"), 200),
            AuthtionTestChecker.of(true, Map.of("email", Account5_Email, "password", Account5_Pass_Encoded, "nickName", "hello world", "middleName", "john", "gender", 2), 200)
    ));
    return list;
  }

  public static final List<AuthtionTestChecker> checkers_for_updateAccount1 = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateAccount2 = List.of(
          AuthtionTestChecker.of(true, Map.of("nickName", "user", "firstName", "", "lastName", ""), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateAccount3 = List.of(
          AuthtionTestChecker.of(true, Map.of("nickName", "hello", "firstName", "1"), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateAccount4 = List.of(
          AuthtionTestChecker.of(true, Map.of("lastName", "2"), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_updateAccount5 = List.of(
          AuthtionTestChecker.of(true, Map.of("nickName", "good", "firstName", "alto", "lastName", "smith"), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_getAccount = List.of(
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

  public static final List<AuthtionTestChecker> checkers_for_publicAccount_9 = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "id-not-exist")
  );
  public static final List<AuthtionTestChecker> checkers_for_publicAccount_1000 = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200, Map.of("id", 1000, "nickName", "test1"))
  );

  public static final List<AuthtionTestChecker> checkers_for_reqConfirmEmail = List.of(
          AuthtionTestChecker.of(true, Map.of(), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_reqConfirmEmail_isConfirmed = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "email-is-already-confirmed")
  );

  public static final List<AuthtionTestChecker> checkers_for_reqConfirmEmail_duplicateDelay = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "delay-between-duplicate-requests")
  );

  public static List<AuthtionTestChecker> checkers_for_confirmEmail(String existedKey)
  {
    return List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", ""), 200, "empty-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", "123"), 200, "confirm-key-not-exist"),
            AuthtionTestChecker.of(true, Map.of("key", existedKey), 200)
    );
  }

  public static final List<AuthtionTestChecker> checkers_for_changePass = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", ""), 200, "empty-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account3_Pass), 200, "missing-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account3_Pass, "newpass", ""), 200, "empty-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account3_Pass, "newpass", "12345"), 200, "exceeded-min6-or-max55-newpass-length"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account3_Pass, "newpass", "12345678901234567890123456789012345678901234567890123456"), 200, "exceeded-min6-or-max55-newpass-length"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account3_Pass + "1", "newpass", "1234567"), 200, "wrong-oldpass"),
          AuthtionTestChecker.of(true, Map.of("oldpass", Account3_Pass, "newpass", Account3_NewPass), 200)
  );

  public static final List<AuthtionTestChecker> checkers_for_changePass_2 = List.of(
          AuthtionTestChecker.of(false, Map.of(), 200, "missing-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", ""), 200, "empty-oldpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account5_Pass_Decoded), 200, "missing-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account5_Pass_Decoded, "newpass", ""), 200, "empty-newpass"),
          AuthtionTestChecker.of(false, Map.of("oldpass", Account5_Pass_Decoded + "1", "newpass", "1234567"), 200, "wrong-oldpass"),
          AuthtionTestChecker.of(true, Map.of("oldpass", Account5_Pass_Decoded, "newpass", Account5_NewPass_Encoded), 200)
  );

  public static List<AuthtionTestChecker> checkers_for_reqRestorePass(String email)
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

  public static List<AuthtionTestChecker> checkers_for_reqRestorePass_duplicateDelay(String email)
  {
    return List.of(
            AuthtionTestChecker.of(false, Map.of("email", email), 200, "delay-between-duplicate-requests")
    );
  }

  public static List<AuthtionTestChecker> checkers_for_confirmRestorePass(String email, String existedKey)
  {
    return List.of(
            AuthtionTestChecker.of(false, Map.of(), 200, "missing-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", ""), 200, "empty-confirm-key"),
            AuthtionTestChecker.of(false, Map.of("key", "657"), 200, "confirm-key-not-exist"),
            AuthtionTestChecker.of(true, Map.of("key", existedKey), 200, Map.of("email", email, "key", existedKey))
    );
  }

  public static List<AuthtionTestChecker> checkers_for_restorePass(String email, String newpass, String existedKey)
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
            AuthtionTestChecker.of(false, Map.of("newpass", newpass, "key", existedKey, "email", "ehlo@mail.ru"), 200, "confirm-key-not-exist"),
            AuthtionTestChecker.of(true, Map.of("newpass", newpass, "key", existedKey, "email", email), 200)
    ));
    return list;
  }
}
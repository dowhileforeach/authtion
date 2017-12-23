| Token Store          | Branch        |
|----------------------|:-------------:|
| JSON Web Tokens      | `JWT`         |
| In Memory            | `master`      |
<br>
Java 9, Spring Boot 2, OAuth 2, RESTful, MySQL<br>

## API versioning
In this project, the version of the API = "v1", the controller class is called "ControllerAuthtionV1".<br>
When developing a new API should create a new controller class, e.g. "ControllerAuthtionV2", "ControllerAuthtionV3" etc.<br><br>
Unfortunately for resource authentication "/oauth/token" spring-security-oauth2 does not provide for versioning.<br>
Therefore, for all API versions is one version "/oauth/token".

# Automated TESTs
See `test\java\ru.dwfe.authtion` classes.

## AuthorityTest
1. Login as: `user@ya.ru`, `admin@ya.ru`, `shop@ya.ru`
2. Try to access as `user@ya.ru`, `admin@ya.ru`, `shop@ya.ru`, `not logged user` to resources:
   * `/public`
   * `/cities`
   * `/list-of-consumers`
   * `/create-consumer`
   * etc.

![AuthorityTest_pic](./img/AuthorityTest_pic.png)<br>
<br>
![AuthorityTest_RespReq](./img/AuthorityTest_RespReq.png)

#### For Manual tests
`user@ya.ru` login:
```
curl Trusted:trPass@localhost:8080/oauth/token -d grant_type=password -d username=user@ya.ru -d password=passUser
```

`admin@ya.ru` login:
```
curl Untrusted:untrPass@localhost:8080/oauth/token -d grant_type=password -d username=admin@ya.ru -d password=passAdmin
```

`shop@ya.ru` login:
```
curl Trusted:trPass@localhost:8080/oauth/token -d grant_type=password -d username=shop@ya.ru -d password=passFrontend
```

Templates for resources access:
```
curl http://localhost:8080/v1/public
curl http://localhost:8080/v1/cities -H "Authorization: Bearer ACCESS_TOKEN"
curl http://localhost:8080/v1/users -H "Authorization: Bearer ACCESS_TOKEN"
curl http://localhost:8080/v1/create-consumer -H "Authorization: Bearer ACCESS_TOKEN" -H "Content-Type: application/json; charset=utf-8" -X POST -d '{"id": "user"}'
...
```

## ConsumerPassword_CRU_Test

### Consumer: Create, Read, Update
![ConsumerPassword_CRU_Test_Consumer](./img/ConsumerPassword_CRU_Test_Consumer.png)<br>

### Password management
![ConsumerPassword_CRU_Test_Password](./img/ConsumerPassword_CRU_Test_Password.png)<br>
<br>
1. Passwords in database are stored as bcrypt hashes in the following format:<br>
![Authtion_brypt_regexp](./img/Authtion_brypt_regexp.png)
2. For resources: `/create-consumer`, `/change-consumer-pass`, `/restore-consumer-pass` - password and newpass can be transmitted in the raw form or in the form of already encoded bcrypt hash
3. For resource: `/create-consumer` - you may not transmit the password at all. In this case the password will be generated and sent to the e-mail



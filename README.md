| Token Store          | Branch        |
|----------------------|:-------------:|
| JSON Web Tokens      | `JWT`         |
| DataBase             | `master`      |
<br>
Java10, Spring-Boot2, OAuth2, RESTful, MySQL<br>

# API versioning
In this project, the version of the API = "v1", the controller class is called "ControllerAuthtionV1".<br>
When developing a new API should create a new controller class, e.g. "ControllerAuthtionV2", "ControllerAuthtionV3" etc.<br><br>
Unfortunately for resource authentication `/oauth/token` "out of box" spring-security-oauth2 does not provide for versioning.<br>
Therefore, for all API versions is one version `/oauth/token`, which mapped to `/{current API version}/sign-in`.

# Error management
When you interact with the Authtion server, you may encounter three types of errors:
1. Real Http error
2. OAuth2 server error, which look like this:<br>
![OAuth2 server error example](./assets/img/oauth2-server-error-example.png)<br>
The list of OAuth2 server errors and their mapping is [here](./assets/oauth2-server-errors-mapping.js) (may not include unknown errors to me).
3. Authtion server error, which look like this:<br>
![Authtion server error example](./assets/img/authtion-server-error-example.png)<br>
The list of all Authtion server errors and their mapping is [here](./assets/authtion-server-errors-mapping.js).

# Automated TESTs
See `test\java\ru.dwfe.authtion` classes.

## AuthTest
1. Sign In as: `user@ya.ru`, `admin@ya.ru`, and `some more`
2. Try to access as `user@ya.ru`, `admin@ya.ru`, `not logged user` and `some more` to resources:
   * `/public/consumer/{id}`
   * `/get-consumer-data`
   * `/list-of-consumers`
   * `/create-consumer`
   * etc.

![AuthTest_pic](./assets/img/AuthTest_pic.png)<br>
<br>
![AuthTest_RespReq](./assets/img/AuthTest_RespReq.png)
<br>

#### For Manual tests
Sign In as `user@ya.ru`:
```
curl Trusted:trPass@localhost:8080/v1/sign-in -d grant_type=password -d username=user@ya.ru -d password=passUser
```

Sign In as `admin@ya.ru`:
```
curl Untrusted:untrPass@localhost:8080/v1/sign-in -d grant_type=password -d username=admin@ya.ru -d password=passAdmin
```

Templates for accessing to resources:
```
curl http://localhost:8080/v1/public/consumer/1
curl http://localhost:8080/v1/get-consumer-data -H "Authorization: Bearer ACCESS_TOKEN"
curl http://localhost:8080/v1/list-of-consumers -H "Authorization: Bearer ACCESS_TOKEN"
curl http://localhost:8080/v1/check-consumer-email -H "Content-Type: application/json; charset=utf-8" -X POST -d '{"email": "admin@ya.ru"}'
curl http://localhost:8080/v1/create-consumer -H "Content-Type: application/json; charset=utf-8" -X POST -d '{"id": "user"}'
...
```

## Email alerts service
![ServiceAlerts](./assets/img/ServiceAlerts.png)<br>

## ConsumerPassword_CRU_Test

### Consumer: Create, Read, Update
![ConsumerPassword_CRU_Test_Consumer](./assets/img/ConsumerPassword_CRU_Test_Consumer.png)<br><br><br>
![ConsumerPassword_CRU_Test_Consumer_error-codes](./assets/img/ConsumerPassword_CRU_Test_Consumer_error-codes.png)<br>

### Password management
![ConsumerPassword_CRU_Test_Password](./assets/img/ConsumerPassword_CRU_Test_Password.png)<br>
<br>
1. Passwords in database are stored as bcrypt hashes in the following format:<br>
![Authtion_brypt_regexp](./assets/img/Authtion_brypt_regexp.png)
2. For resources: `/create-consumer`, `/change-consumer-pass`, `/restore-consumer-pass` - password and newpass can be passed in the raw form or in the form of already encoded bcrypt hash
3. For resource: `/create-consumer` - you may not transmit the password at all. In this case the password will be generated and sent to the consumer e-mail
<br><br>

![ConsumerPassword_CRU_Test_Password_error-codes](./assets/img/ConsumerPassword_CRU_Test_Password_error-codes.png)

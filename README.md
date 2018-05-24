| Token Store          | Branch        |
|----------------------|:-------------:|
| DataBase             | `master`      |
| JSON Web Tokens      | `JWT`         |
<br>
Java10, Spring-Boot2, OAuth2, RESTful, MySQL
<br><br>

  * [Errors](#errors)
    * [Spring server error](#spring-server-error)
    * [OAuth2 server error](#oauth2-server-error)
    * [Authtion server error](#authtion-server-error)
  * [Date and Time](#date-and-time)
  * [Email alerts](#email-alerts)
  * [API](#api)
    * [Auth](#auth)
    * [Account](#account)
      * [Create, Read, Update](#create-read-update)
      * [Password management](#password-management)

# Errors
When you interact with the Authtion server, you may encounter the following types of errors:<br>

### Spring server error
which look like this:
<br>
![Spring server error example](./assets/img/spring-server-error-example.png)<br>
### OAuth2 server error
which look like this:
<br>
![OAuth2 server error example](./assets/img/oauth2-server-error-example.png)<br>
The list of OAuth2 server error-codes and their mapping see [here](./assets/oauth2-server-errors-mapping.js) (may not include unknown errors to me).
<br>
Also you should know that under one error-code, there may be several different error_description.
<br>
For example `invalid_grant` error-code can be returned with the following error_descriptions:<br>
   * *Bad credentials* - if login or/and password is incorrect
   * *User is disabled* - if enabled field set to false
   * *User account is locked* - if account_non_locked field set to false
   * *User credentials have expired* - if credentials_non_expired field set to false
   * *User account has expired* - if account_non_expired field set to false

In this regard, it may be worthwhile instead of mapping error-codes to output error_description.

### Authtion server error
which look like this:
<br>
![Authtion server error example](./assets/img/authtion-server-error-example.png)<br>
The list of all Authtion server error-codes and their mapping see [here](./assets/authtion-server-errors-mapping.js).

# Date and Time
The Authtion server is set to time zone UTC.<br>
![Authtion is set to UTC](./assets/img/date-time.png)<br>
Also, some Authtion database table fields are automatically assigned directly by the database. Therefore, **to work correctly with date and time, you must explicitly set the time zone for mysqld to UTC** (this can be done in [several ways](https://stackoverflow.com/questions/930900/how-do-i-set-the-time-zone-of-mysql#19069310)) or override Authtion server preferences.

# Email alerts
![ServiceAlerts](./assets/img/ServiceAlerts.png)
<br><br>
Preparation of mail templates was made in a separate project [authtion-backend-mailing](https://github.com/dowhileforeach/authtion-backend-mailing).
<br>
To fill the mail templates used [Thymeleaf](http://www.thymeleaf.org) template engine.

# API
In this project, the version of the API = "v1", the controller class is called "AuthtionControllerV1".<br>
When developing a new API should create a new controller class, e.g. "ControllerV2", "ControllerV3" etc.<br><br>
Unfortunately for resource authentication `/oauth/token` "out of box" spring-security-oauth2 does not provide for versioning. Therefore, for all API versions is one version `/oauth/token`, which mapped to `/{current API version}/sign-in`.
<br><br>
See `test\java\ru.dwfe.authtion` classes.<br>
The tests cover all API, except for the cases, when the test write is simply impossible. For example, automatic test of `/google-captcha-validate` is not possible without human intervention.

## Auth
1. Sign In as: `test1@dwfe.ru`, `test2@dwfe.ru`, and `some more`
2. Try to access as `test1@dwfe.ru`, `test2@dwfe.ru`, `not logged user` and `some more` to resources:
   * `/create-account`
   * `/get-account`
   * `/public/account/{id}`
   * etc.

![AuthTest_pic](./assets/img/AuthTest_pic.png)<br>
<br>
![AuthTest_RespReq](./assets/img/AuthTest_RespReq.png)
<br>

#### For Manual tests
Sign In as `test1@dwfe.ru`:
```
curl Untrusted:4rZi5yEhcv5Jb3jSzGPfFFDK@localhost:8080/v1/sign-in -d grant_type=password -d username=test1@dwfe.ru -d password=test11
```

Sign In as `test2@dwfe.ru`:
```
curl Trusted:YWPVYGiGLW4Whnr3Q5vuzd8i@localhost:8080/v1/sign-in -d grant_type=password -d username=test2@dwfe.ru -d password=test22
```

Templates for manual accessing to resources:
```
curl http://localhost:8080/v1/check-email -H "Content-Type: application/json; charset=utf-8" -X POST -d '{"email": "some@email.com"}'
curl http://localhost:8080/v1/create-account -H "Content-Type: application/json; charset=utf-8" -X POST -d '{"email": "some@email.com"}'
curl http://localhost:8080/v1/get-account -H "Authorization: Bearer ACCESS_TOKEN"
curl http://localhost:8080/v1/public/account/1001
curl http://localhost:8080/v1/update-account -H "Authorization: Bearer ACCESS_TOKEN" -H "Content-Type: application/json; charset=utf-8" -X POST -d '{"emailNonPublic": false, "gender": 1}'
...
```

## Account

### Create, Read, Update
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

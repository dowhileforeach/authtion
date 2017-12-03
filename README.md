| Token Store          | Branch        |
|----------------------|:-------------:|
| JSON Web Tokens      | `JWT`         |
| In Memory            | `master`      |
<br>
Java 9, Spring Boot 2, OAuth 2, RESTful, MySQL<br>
<br>
## API versioning
In this project, the version of the API = "v1",<br>
the controller class is called "AppControllerV1", e.g. "AppControllerV2", "AppControllerV3" etc.<br>
When developing a new API should create a new controller class.<br><br>
Unfortunately for resource authentication "/oauth/token" spring-security-oauth2 does not provide for versioning.<br>
Therefore, for all API versions is one version "/oauth/token".

# TESTs
## Automated tests
See `test\java\ru.dwfe.authtion` classes.
### BasicRun
See screenrecord: https://youtu.be/y1W9WLX88J4
1. Login: `user`, `admin`
2. Try to access with `user`, `admin`, `not loged user` for resources: `/public`, `/cities`, `/users`

![Basic Run](./Authtion_BasicRun.png)

#### For Manual tests
User Login:
```
curl Standard:Login@localhost:8080/oauth/token -d grant_type=password -d username=user -d password=passUser
```

Admin Login:
```
curl ThirdParty:Computer@localhost:8080/oauth/token -d grant_type=password -d username=admin -d password=passAdmin
```

Templates for resources access tests:
```
curl http://localhost:8080/v1/public
curl http://localhost:8080/v1/cities -H "Authorization: Bearer ACCESS_TOKEN"
curl http://localhost:8080/v1/users -H "Authorization: Bearer ACCESS_TOKEN"
```

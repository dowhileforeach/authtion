### Authentification / Authorization, RESTful
---
Java 9, Spring Boot 2, OAuth 2
<br>

| Token Store Strategy | Branch        |
|----------------------|---------------|
| In Memory            | master        |
<br>
# TESTs
#### Automated tests
See `test\java\ru.dwfe.authtion` classes
#### For Manual tests
Admin Login:
```
curl ThirdParty:Computer@localhost:8080/oauth/token -d grant_type=password -d username=admin -d password=passAdmin
```

User Login:
```
curl Standard:Login@localhost:8080/oauth/token -d grant_type=password -d username=user -d password=passUser
```

Templates for Controller tests:
```
curl http://localhost:8080/cities -H "Authorization: Bearer "
curl http://localhost:8080/users -H "Authorization: Bearer "
curl http://localhost:8080/bcrypt
curl http://localhost:8080/lo
curl http://localhost:8080/lo -H "Authorization: Bearer "
```

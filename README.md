Приложение для заведения тикетов.

Для запуска:
```plain
docker compose up -d
./gradlew bootRun
```

Приложение будет доступно по `localhost:8080`. Swagger по `localhost:8080/swagger-ui/index.html`.

Для заполнения бд тестовыми данными отправь `curl http://localhost:8080/api/init-db` (или просто перейди по ссылке в браузере).

Для схемы в бд использую hibernate.ddl-auto: update, скриптов для миграции схемы нет.

Общий план:
0. Create a basic crud that Kate will be able to build on top of. I feel like providing a good enough frontend would make a difference.
1. Implement a basic username/password auth.
2. RBAC. Request, Support Agent, Team Lead, Admin roles with different permissions.
3. Password storage side quest: encryption, hashing, salting.
4. Session approach.
5. Stateless Bearer token approach with JWTs.
6. Secret storage side quest: store something like a JWT signing key or a pepper
7. OAuth2/OIDC as a client integrating with an existing provider.
8. Kubernetes. Get the app running manually with kubectl. Handle both incoming (api calls) and outgoing requests (oidc id provider, maybe an external integration like openrouter or something.)
9. CI/CD. Chose Jenkins, so let's stick to it.
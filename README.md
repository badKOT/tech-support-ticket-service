# Tech Support Ticket Service

Приложение для заведения и обработки тикетов.

## Запуск через Docker Compose

Для запуска всего стенда одной командой:

```bash
docker compose up -d --build
```

## Проверка состояния:
docker compose ps -a

Ожидаемое состояние:
backend — Up
frontend — Up
keycloak — Up (healthy)
postgres — Up (healthy)
init-db — Exited (0)

## Остановка:
docker compose down
Без -v, если не нужно удалять данные PostgreSQL и Keycloak.

## Стенд
Приложение:
https://88-218-67-241.sslip.io

Keycloak:
https://auth.88-218-67-241.sslip.io

OIDC issuer:
https://auth.88-218-67-241.sslip.io/realms/tech-support

HTTPS завершается на nginx.
Сертификат выдан Let's Encrypt для:
88-218-67-241.sslip.io
auth.88-218-67-241.sslip.io

Порт 80 используется для HTTP-01 challenge и редиректа на HTTPS.

## Переменные окружения

Для запуска:
`docker compose up -d`
- Запуск: `.\dev.ps1` - скрипт запускает backend и frontend и открывает приложение в браузере.

- Остановка: `.\devStop.ps1`

- Перезапуск: `.\devRestart.ps1`

Запуск вручную:
Backend: `.\gradlew.bat bootRun`
Frontend: `cd frontend`, `npm install`, `npm run dev`

Приложение будет доступно по `localhost:8080`. Swagger по `localhost:8080/swagger-ui/index.html`.

Для заполнения бд тестовыми данными отправь `curl http://localhost:8080/api/init-db` (или просто перейди по ссылке в браузере).

Для схемы в бд использую hibernate.ddl-auto: update, скриптов для миграции схемы нет.

Общий план:
0. Create a basic crud.
1. Implement a basic username/password auth.
2. RBAC. Request, Support Agent, Team Lead, Admin roles with different permissions.
3. Password storage side quest: encryption, hashing, salting.
4. Session approach.
5. Stateless Bearer token approach with JWTs.
6. Secret storage side quest: store something like a JWT signing key or a pepper
7. OAuth2/OIDC as a client integrating with an existing provider.
8. Kubernetes. Get the app running manually with kubectl. Handle both incoming (api calls) and outgoing requests (oidc id provider, maybe an external integration like openrouter or something.)
9. CI/CD. Chose Jenkins, so let's stick to it.
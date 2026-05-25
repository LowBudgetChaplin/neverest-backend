# Neverest Backend - Phase 4 (Finalized)

Backend-ul acopera acum:
- users cu QR personal;
- events + check-in QR;
- leaderboard general + pe activitate;
- challenges weekly/monthly (online/offline);
- rewards + redeem pe baza de puncte;
- autentificare externa Firebase (optional, activata din config);
- integrare announcements prin webhook (WhatsApp + Strava);
- audit log pentru actiuni admin/user;
- persistenta reala cu JPA + Flyway (PostgreSQL-ready);
- retry job automat pentru webhook announcements.

## Run

```bash
./mvnw spring-boot:run
```

Pe Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

## Database (MySQL 8)

Aplicatia foloseste persistenta SQL prin `Spring Data JPA` + migratii `Flyway`,
iar baza de date oficiala este **MySQL 8** (`neverest`).

### Configurare implicita

In `application.properties`, default-urile sunt:

```properties
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/neverest?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&useUnicode=true
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
spring.jpa.properties.hibernate.type.preferred_uuid_jdbc_type=CHAR
```

Toate valorile pot fi suprascrise prin variabile de mediu:

```powershell
$env:NEV_DB_URL="jdbc:mysql://127.0.0.1:3306/neverest?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true"
$env:NEV_DB_USER="neverest_app"
$env:NEV_DB_PASSWORD="parola_ta"
$env:NEV_DB_DRIVER="com.mysql.cj.jdbc.Driver"
```

### Pornire rapida local (MySQL prin Docker)

```powershell
docker compose up -d
```

Containerul ridicat de `docker-compose.yml` expune MySQL pe `127.0.0.1:3306` cu:
- `MYSQL_DATABASE=neverest`
- `MYSQL_USER=neverest`, `MYSQL_PASSWORD=neverest`
- `MYSQL_ROOT_PASSWORD=root`

### Pornire cu MySQL local instalat (ex. MySQL Workbench)

1. Conecteaza-te in Workbench si executa:
   ```sql
   CREATE DATABASE IF NOT EXISTS neverest
       CHARACTER SET utf8mb4
       COLLATE utf8mb4_unicode_ci;
   ```
2. Optional, creeaza un user dedicat:
   ```sql
   CREATE USER 'neverest_app'@'localhost' IDENTIFIED BY 'parola_ta';
   GRANT ALL PRIVILEGES ON neverest.* TO 'neverest_app'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Seteaza `NEV_DB_USER` / `NEV_DB_PASSWORD` si porneste backend-ul.

La startup, Flyway aplica automat migratia `V1__init_schema.sql` si creeaza
toate tabelele `nev_*` necesare. Daca baza contine deja tabele care nu provin
de la Flyway, este activat `spring.flyway.baseline-on-migrate=true`, deci
migratia se va aplica peste schema existenta fara a sterge ceva manual.

> Nota: profilul `postgres` (`application-postgres.properties`) si imaginea
> Docker Postgres au fost inlocuite cu MySQL. Daca ai nevoie de Postgres,
> reactiveaza profilul cu propriile setari.

## Retry job pentru announcements

Configurare in `application.properties`:

```properties
neverest.integrations.retry.enabled=true
neverest.integrations.retry.fixed-delay-ms=60000
neverest.integrations.retry.batch-size=25
neverest.integrations.retry.max-attempts=5
neverest.integrations.retry.backoff-seconds=120
```

## API Endpoints (base path `/api/v1`)

## Users

### Create user

`POST /users`

Body:
```json
{
  "displayName": "Andrei"
}
```

Endpoint folosit pentru creare user de catre admin sau in development.

### Register current authenticated user

`POST /users/me`

```json
{
  "displayName": "Andrei"
}
```

### List users

`GET /users`

### Current authenticated user

`GET /users/me`

Observatie: fiecare user are:
- `totalPoints` = puncte castigate total (folosit la leaderboard);
- `availablePoints` = puncte disponibile pentru rewards.

## Events

### Create event

`POST /events`

Body:
```json
{
  "title": "Padel de seara",
  "activityType": "PADEL",
  "location": "Complex Sportiv X",
  "startsAt": "2026-04-05T18:00:00",
  "pointsReward": 20
}
```

Raspunsul include:
- obiectul `event`;
- lista `announcements` cu status pentru webhook WhatsApp/Strava.

### List events

`GET /events`

### Check-in user la event (scan QR)

`POST /events/{eventId}/check-ins`

Body:
```json
{
  "userQrCode": "NEV-ABC12345"
}
```

### Retry announcements pentru event (admin)

`POST /events/{eventId}/announcements/retry`

## Leaderboard

### General

`GET /leaderboard/general?limit=20`

### Per activity

`GET /leaderboard/activity/{activityType}?limit=20`

`activityType`: `PADEL`, `MOUNTAIN`, `RUNNING`.

## Challenges

### Create challenge (admin)

`POST /challenges`

Body exemplu challenge online:
```json
{
  "title": "Walking challenge",
  "description": "Mergi de la Centrul Vechi la Arcul de Triumf",
  "activityType": "RUNNING",
  "mode": "ONLINE",
  "frequency": "WEEKLY",
  "startsAt": "2026-04-01T00:00:00",
  "endsAt": "2026-04-07T23:59:59",
  "pointsReward": 40,
  "targetValue": 7.5,
  "targetUnit": "km"
}
```

Body exemplu challenge offline:
```json
{
  "title": "Antrenament de grup montan",
  "description": "Participa la sesiunea de sambata",
  "activityType": "MOUNTAIN",
  "mode": "OFFLINE",
  "frequency": "MONTHLY",
  "startsAt": "2026-04-01T00:00:00",
  "endsAt": "2026-04-30T23:59:59",
  "pointsReward": 30
}
```

### List challenges

`GET /challenges`

### Submit challenge

`POST /challenges/{challengeId}/submissions`

Body pentru ONLINE:
```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "metricValue": 8.2
}
```

Body pentru OFFLINE:
```json
{
  "userId": "00000000-0000-0000-0000-000000000000",
  "proofText": "Am participat la sesiune, atasat dovada."
}
```

### Submit challenge as current authenticated user

`POST /challenges/{challengeId}/submissions/me`

```json
{
  "proofText": "Am participat la sesiune, atasat dovada.",
  "metricValue": 8.2
}
```

### List submissions (admin)

`GET /challenges/{challengeId}/submissions?userId=<optional-uuid>`

### List submissions for current authenticated user

`GET /challenges/{challengeId}/submissions/me`

### Review submission (admin)

`POST /challenges/{challengeId}/submissions/{submissionId}/review`

```json
{
  "approved": true,
  "reviewerNote": "Validat de organizator"
}
```

## Rewards

### Create reward (admin)

`POST /rewards`

```json
{
  "title": "Reducere 30% la antrenor",
  "partnerName": "Coach Local",
  "description": "Voucher valabil 30 de zile",
  "pointsCost": 120,
  "stock": 50
}
```

`stock` este optional; daca lipseste, reward-ul este nelimitat.

### List rewards

`GET /rewards?includeInactive=false`

### Redeem reward

`POST /rewards/{rewardId}/redeem`

```json
{
  "userId": "00000000-0000-0000-0000-000000000000"
}
```

### Redeem reward as current authenticated user

`POST /rewards/{rewardId}/redeem/me`

### List redemptions

`GET /rewards/redemptions?userId=<optional-uuid>`

### List redemptions for current authenticated user

`GET /rewards/redemptions/me`

## Admin / Audit

### List audit logs (admin)

`GET /admin/audit-logs?limit=100&action=EVENT_CREATED&actor=<optional>&success=<optional>`

Audit log-ul retine actiuni importante, de exemplu:
- `USER_CREATED`
- `EVENT_CREATED`
- `EVENT_ANNOUNCEMENT_DISPATCH`
- `EVENT_ANNOUNCEMENT_RETRY`
- `EVENT_CHECK_IN`
- `CHALLENGE_CREATED`
- `CHALLENGE_SUBMITTED`
- `CHALLENGE_REVIEWED`
- `REWARD_CREATED`
- `REWARD_REDEEMED`

## Auth / login extern

### Cine sunt eu (debug auth)

`GET /auth/me`

Intoarce user-ul autentificat si rolurile extrase din token.

## Firebase Auth

Da, poti folosi un serviciu extern pentru logare, iar in proiectul actual este pregatit Firebase.

### Configurare

In `application.properties`:

```properties
neverest.auth.provider=firebase
neverest.firebase.project-id=your-firebase-project-id
neverest.integrations.whatsapp.enabled=true
neverest.integrations.whatsapp.webhook-url=https://your-whatsapp-webhook
neverest.integrations.strava.enabled=true
neverest.integrations.strava.webhook-url=https://your-strava-webhook
neverest.integrations.timeout-seconds=5
```

Modul implicit este:

```properties
neverest.auth.provider=none
```

Astfel poti dezvolta local fara token-uri.

### Flux

- Flutter face login prin Firebase Auth (email/google/apple etc.).
- Flutter trimite `ID token` in header `Authorization: Bearer <firebase-id-token>`.
- Backend valideaza semnatura token-ului, issuer-ul si audience-ul.
- Rolurile se citesc din claims:
  - `roles` (array sau string CSV), sau
  - `role` (string)
- Daca nu exista role claim, user-ul primeste implicit rolul `USER`.
- Pentru a seta rapid claim-uri admin pe useri Firebase, vezi `docs/FIREBASE_ADMIN_SETUP.md`.

## Integrari announcements

- La fiecare `POST /events`, backend-ul trimite automat payload de announcement catre webhook-ul WhatsApp si webhook-ul Strava (daca sunt active).
- Daca un webhook esueaza, event-ul ramane creat, iar eroarea este vizibila in raspunsul API + `audit-logs`.
- Se poate relansa trimiterea pentru un event existent cu `POST /events/{eventId}/announcements/retry`.
- In plus, exista retry job automat care reincearca dispatch-ul esuat pe baza task-urilor persistate in DB.

### Reguli acces in modul `firebase`

- `GET /api/v1/admin/**` -> `ADMIN`
- `POST /api/v1/users` si `GET /api/v1/users` -> `ADMIN`
- `POST /api/v1/events` -> `ADMIN`
- `POST /api/v1/events/{eventId}/check-ins` -> `ADMIN`
- `POST /api/v1/events/{eventId}/announcements/retry` -> `ADMIN`
- `POST /api/v1/challenges` -> `ADMIN`
- `GET /api/v1/challenges/{challengeId}/submissions` -> `ADMIN`
- `POST /api/v1/challenges/{challengeId}/submissions/{submissionId}/review` -> `ADMIN`
- `POST /api/v1/rewards` -> `ADMIN`
- `GET /api/v1/rewards/redemptions` -> `ADMIN`
- restul endpoint-urilor din `/api/v1/**` -> user autentificat
- `/hello` ramane public

## Observatii

- Datele sunt persistate in baza de date (nu se pierd la restart).
- Exista validari de baza si raspunsuri de eroare standardizate.
- Exista teste automate de autorizare pe roluri (`USER` vs `ADMIN`) in `src/test/java/com/app/neverest/api/SecurityAuthorizationTests.java`.
- Urmatorul pas recomandat: hardening productie (rate limits, idempotency keys, tracing, backup strategy).

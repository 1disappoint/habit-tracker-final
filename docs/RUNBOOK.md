# Как запустить

Три сценария: **Docker Compose** (рекомендуется), **локально без Docker**, **развёртывание на сервере**.

## Что и где слушает

| Компонент | Порт | Доступ |
|-----------|------|--------|
| api-gateway | 8080 | публикуется наружу — это единственная точка входа |
| user-service | 8081 | только внутри сети `habit-network` |
| habit-service | 8082 | только внутри сети |
| tracking-service | 8083 | только внутри сети |
| postgres | 5432 | только внутри сети |

Все запросы идут на `http://<host>:8080`.

---

## Требования

| Что | Зачем | Проверка |
|-----|-------|----------|
| Docker Desktop / Docker Engine | вариант 1 и 3 | `docker version` |
| Docker Compose v2 | вариант 1 и 3 | `docker compose version` |
| JDK 21+ | вариант 2, сборка | `java -version` |
| Maven 3.9+ | вариант 2, сборка | `mvn -v` |

---

## Вариант 1. Docker Compose (рекомендуется)

### 1. Подготовить секреты

Пароль и логин не лежат в репозитории — они в отдельном файле.

```bash
cp config/secrets/db-credentials.properties.example config/secrets/db-credentials.properties
cp config/secrets/postgres-password.txt.example      config/secrets/postgres-password.txt
```

Отредактируйте оба файла — пароль должен совпадать:

`config/secrets/db-credentials.properties`
```properties
DB_USER=?
DB_PASS=?
```


`config/secrets/postgres-password.txt`
```
?
```

> Логин БД (`POSTGRES_USER`) задан в `docker-compose.yml` и должен совпадать с `DB_USER`.
> Пароль в `postgres-password.txt` должен совпадать с `DB_PASS` в `db-credentials.properties`.
> Если пароль менялся, а том `db_data` уже создан — нужен `docker compose down -v`.

### 2. Собрать и запустить

```bash
docker compose up -d --build
```

Первый запуск занимает несколько минут: внутри контейнеров скачиваются Maven-зависимости.

### 3. Проверить

```bash
docker compose ps
curl http://localhost:8080/actuator/health
```

Ожидается 5 контейнеров в статусе `(healthy)` и `{"status":"UP"}`.

### 4. Логи

```bash
docker compose logs -f tracking-service   # конкретный сервис
docker compose logs -f                    # всё сразу
```

---

## Вариант 2. Локально без Docker

Нужен только PostgreSQL: либо установленный в системе, либо в контейнере с проброшенным портом.

### 1. База данных

**Вариант A** — PostgreSQL установлен локально: создайте БД `app` и примените схему.

```bash
createdb app
psql -d app -f db/init.sql
```

Windows (psql нет в PATH):
```powershell
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -U postgres -c 'CREATE DATABASE app'
& 'C:\Program Files\PostgreSQL\17\bin\psql.exe' -U postgres -d app -f db\init.sql
```

Дальше положите пароль **этого** PostgreSQL в `config/secrets/db-credentials.properties` —
его используют и запуск из IDE, и Docker-стек:

```properties
DB_USER=postgres
DB_PASS=<пароль локального PostgreSQL>
```

`DB_URL` при этом не нужен — по умолчанию берётся `jdbc:postgresql://localhost:5432/app`,
а логин/пароль подтягиваются из файла. Если для локальной базы нужен отдельный пароль,
а в Docker — свой, задайте `CREDENTIALS_FILE` на другой файл:

```powershell
$env:CREDENTIALS_FILE="$PWD\config\secrets\db-credentials-local.properties"
```

**Вариант B** — Postgres в Docker (порт `15432`, чтобы не конфликтовать с локальным):

```bash
docker run -d --name habit-pg-local \
  -e POSTGRES_PASSWORD=? -e POSTGRES_DB=app \
  -p 127.0.0.1:15432:5432 \
  -v "$(pwd)/db/init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro" \
  postgres:16
```

PowerShell:
```powershell
docker run -d --name habit-pg-local -e POSTGRES_PASSWORD=1234 -e POSTGRES_DB=app -p 127.0.0.1:15432:5432 -v "${PWD}\db\init.sql:/docker-entrypoint-initdb.d/01-init.sql:ro" postgres:16
```

### 2. Собрать jar'ы

Из корня репозитория:

```bash
mvn clean package -DskipTests
```

### 3. Запустить сервисы

Каждый — в своём терминале, **из корня репозитория** (тогда `config/secrets/db-credentials.properties`
находится по пути по умолчанию).

Терминал 1 — user-service:
```bash
export DB_URL=jdbc:postgresql://localhost:15432/app
java -jar user-service/target/user-service-1.0.0.jar
```

PowerShell: `$env:DB_URL='jdbc:postgresql://localhost:15432/app'; java -jar user-service\target\user-service-1.0.0.jar`

Терминал 2 — habit-service:
```bash
export DB_URL=jdbc:postgresql://localhost:15432/app
export USER_SERVICE_URL=http://localhost:8081
java -jar habit-service/target/habit-service-1.0.0.jar
```

Терминал 3 — tracking-service:
```bash
export DB_URL=jdbc:postgresql://localhost:15432/app
export HABIT_SERVICE_URL=http://localhost:8082
java -jar tracking-service/target/tracking-service-1.0.0.jar
```

Терминал 4 — api-gateway (база не нужна):
```bash
export USER_SERVICE_URL=http://localhost:8081
export HABIT_SERVICE_URL=http://localhost:8082
export TRACKING_SERVICE_URL=http://localhost:8083
java -jar api-gateway/target/api-gateway-1.0.0.jar
```

Порядок важен только для вызовов «по цепочке»: gateway можно поднять в любой момент.

### 4. Проверить

```bash
curl http://localhost:8081/actuator/health   # user-service
curl http://localhost:8080/users             # через шлюз
```

### Про `mvn spring-boot:run`

У плагина рабочий каталог — каталог модуля, поэтому путь по умолчанию
`./config/secrets/db-credentials.properties` не найдётся. Задайте абсолютный путь:

```bash
CREDENTIALS_FILE="$(pwd)/config/secrets/db-credentials.properties" \
  mvn -pl user-service spring-boot:run
```

Проще запускать собранный jar из корня — как в шагах выше.

---

## Вариант 3. Развернуть на сервере (Linux)

### 1. Установить Docker

```bash
sudo apt update
sudo apt install -y docker.io docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"    # затем перелогиниться
```

### 2. Положить проект

```bash
sudo mkdir -p /opt/habit-tracker && sudo chown "$USER" /opt/habit-tracker
git clone <repo-url> /opt/habit-tracker
cd /opt/habit-tracker
```

Без git — скопировать каталог: `scp -r ./ user@server:/opt/habit-tracker`.

### 3. Секреты — только для владельца

```bash
cp config/secrets/db-credentials.properties.example config/secrets/db-credentials.properties
cp config/secrets/postgres-password.txt.example      config/secrets/postgres-password.txt
# отредактировать оба файла
chmod 600 config/secrets/db-credentials.properties config/secrets/postgres-password.txt
```

Файлы уже в `.gitignore` и не попадут в репозиторий.

### 4. Запуск

```bash
docker compose up -d --build
docker compose ps
curl -s localhost:8080/actuator/health
```

### 5. Файрвол

Публикуется только `8080`:

```bash
sudo ufw allow 8080/tcp
sudo ufw enable
sudo ufw status
```

Порты `8081–8083` и `5432` наружу не выставляются — не открывайте их без нужды.

### 6. Автозапуск после перезагрузки

Ничего дополнительно настраивать не нужно:

- у всех сервисов в `docker-compose.yml` стоит `restart: unless-stopped`;
- `docker.service` включён в автозапуск (`systemctl enable --now docker`).

Проверка: `sudo reboot`, затем `docker compose ps`.

Отдельный systemd-юнит нужен, только если Docker-демон выключен намеренно.

### 7. HTTPS и домен (опционально)

Gateway сам терминирует HTTP, но не TLS. Поставьте перед ним nginx:

```nginx
server {
    listen 80;
    server_name habits.example.com;

    location / {
        proxy_pass         http://127.0.0.1:8080;
        proxy_set_header   Host              $host;
        proxy_set_header   X-Real-IP         $remote_addr;
        proxy_set_header   X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header   X-Forwarded-Proto $scheme;
    }
}
```

Сертификат: `sudo certbot --nginx -d habits.example.com`.

Тогда `8080` достаточно слушать на loopback — замените в `docker-compose.yml`:
```yaml
ports:
  - "127.0.0.1:8080:8080"
```

---

## Проверка работоспособности (smoke test)

```bash
# 1. Пользователь
curl -X POST http://localhost:8080/users/create \
  -H "Content-Type: application/json" \
  -d '{"name":"Ivan","email":"ivan@test.com"}'
# => 201 {"id":1,"name":"Ivan","email":"ivan@test.com"}

# 2. Привычка (проверяет пользователя через Feign)
curl -X POST http://localhost:8080/habits/create \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"title":"Drink water"}'
# => 201 {"id":1,"userId":1,"title":"Drink water"}

# 3. Отметка выполнения (проверяет привычку через Feign)
curl -X POST http://localhost:8080/trackings/create \
  -H "Content-Type: application/json" \
  -d '{"habitId":1,"completedOn":"2026-11-20"}'
# => 201 {"id":1,"habitId":1,"completedOn":"2026-11-20"}

# 4. Чтение
curl http://localhost:8080/trackings?habitId=1
```

Ожидаемые ошибки (это норма):

| Запрос | Ответ |
|--------|-------|
| повторная отметка за тот же день | `409` |
| `habitId`, которого нет | `422` |
| `completedOn` в будущем | `400` |

> **Windows/PowerShell:** кавычки в JSON ломаются при передаче в `curl.exe`.
> Пишите тело в файл и используйте `--data-binary "@body.json"`.

---

## Обновление, остановка, сброс

```bash
# обновить код и пересобрать образы
git pull
docker compose up -d --build

# остановить (данные в томе db_data сохраняются)
docker compose down

# остановить и удалить данные БД — схема из db/init.sql применится заново
docker compose down -v
```

Схема БД создаётся только при **первом** старте пустого тома. Если правили `db/init.sql`,
нужен `docker compose down -v`.

### Бэкап и восстановление

```bash
# бэкап
docker compose exec -T db pg_dump -U postgres app > backup-$(date +%F).sql

# восстановление
cat backup-2026-11-20.sql | docker compose exec -T db psql -U postgres app
```

---

## Диагностика

| Симптом | Причина | Что делать |
|---------|---------|------------|
| `Cannot connect to the Docker daemon` | Docker Desktop не запущен (Windows) | запустить Docker Desktop, дождаться `docker version` |
| `Bind for 0.0.0.0:8080 failed: port is already allocated` | порт занят | освободить порт или изменить в `docker-compose.yml` |
| `ports are not available ... access permissions` (Windows) | порт в исключённом диапазоне Hyper-V (часто `5432`) | публиковать на другом порту: `-p 127.0.0.1:15432:5432` |
| контейнер `unhealthy` | сервис не поднялся | `docker compose logs <service>` |
| `password authentication failed` | пароли в двух файлах секретов не совпадают | привести `db-credentials.properties` и `postgres-password.txt` к одному значению |
| health `UP`, но таблиц нет | том БД старый, `init.sql` не применялся | `docker compose down -v && docker compose up -d` |
| `{"error":"Malformed JSON request body"}` при curl | PowerShell/PowerShell ISE испортил кавычки | писать тело в файл: `--data-binary "@body.json"` |
| `422 Habit with id N does not exist` | привычка не создана или создана в другой БД | создать привычку, проверить `GET /habits` |
| IDE подчёркивает `HABITS`, `UsersRecord` | jOOQ-исходники не сгенерированы/не подключены | `mvn clean package`, затем Maven → Reload |
| `Could not resolve placeholder 'DB_PASS'` | файл кред не найден | проверить путь `./config/secrets/...` или `CREDENTIALS_FILE` |

---

## Полезные команды

```bash
docker compose ps -a                        # состояние всех контейнеров
docker compose logs -f --tail=100 api-gateway
docker compose exec db psql -U postgres -d app -c '\dt'   # список таблиц
docker compose exec db psql -U postgres -d app -c 'select * from trackings;'
docker compose restart user-service
docker stats                                # потребление ресурсов
```

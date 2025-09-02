# Spring AI Chat 🤖

Локальный чат с AI, который живет у вас на компьютере и не отправляет ваши секреты в облако.

## Что нужно для счастья

- Docker (чтобы контейнеры контейнерились)
- Java 17+ (потому что Spring Boot)
- Maven (собирает всё в кучу)

## Запуск за 3 шага

### 1. Поднимаем зоопарк сервисов

```bash
docker-compose up -d
```

Ждем пока Ollama скачает модели (это как качать торренты, только легально).

### 2. Запускаем Spring Boot

```bash
cd service
mvn spring-boot:run
```

### 3. Открываем браузер

```
http://localhost:8080
```

Готово! Теперь можете спрашивать у AI про смысл жизни или как исправить баг в коде.

## Что где крутится

- **Чат**: http://localhost:8080
- **PgAdmin**: http://localhost:5050 (логин: `pgadmin4@pgadmin.org` / пароль: `admin`)
- **Ollama API**: http://localhost:11431

## Выключение

```bash
# Мягко
docker-compose down

# С форматированием (удалит все данные)
docker-compose down -v
```

## Если что-то пошло не так

- Порт занят? Поменяйте в `docker-compose.yml`
- Ollama не отвечает? `docker-compose logs ollama`
- Spring Boot ругается? Проверьте, что Docker контейнеры запущены

---
*P.S. Модель Gemma3 весит меньше, чем ваша папка node_modules* 📦
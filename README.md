# Task Manager — Веб-приложение

Веб-приложение для управления личными задачами с аутентификацией пользователей.
Проект написан на Java/Spring Boot и демонстрирует многослойную архитектуру: Controller → Service → Repository.

---

## Описание

Проект реализует функционал управления задачами через веб-интерфейс: 
добавление, просмотр, удаление и отметка задач как выполненных.
Поддерживает регистрацию и аутентификацию пользователей, разделение задач по пользователям.
Данные сохраняются в базе данных с использованием JPA/Hibernate.

---

## Функционал

### Для пользователей:
- Регистрация и аутентификация
- Создание, просмотр, удаление задач
- Отметка задач как выполненных
- Просмотр активных и выполненных задач отдельно

### Для администраторов:
- Все возможности пользователей
- Просмотр метрик системы через `/admin/metrics`

---

## Архитектура

* **entity** — JPA-сущности (`Task`, `User`, `Role`)
* **repository** — интерфейсы JPA (`TaskRepository`, `UserRepository`)
* **service** — бизнес-логика (`TaskService`, `UserService`, `CustomUserDetailsService`)
* **controller** — веб-контроллеры (`TaskController`, `AuthController`, `AdminController`)
* **config** — конфигурация безопасности (`SecurityConfig`)
* **templates** — Thymeleaf шаблоны

---

## Используемые технологии

* **Backend**: Spring Boot, Spring MVC, Spring Security, Spring Data JPA
* **Database**: PostgreSQL (prod), H2 (tests)
* **Frontend**: Thymeleaf, Bootstrap
* **Testing**: JUnit 5, Mockito, Spring Test
* **Build**: Maven
* **Containerization**: Docker, Docker Compose

---

## Быстрый старт (локальный запуск)

### Требования

* Java 21
* Maven
* PostgreSQL

### Шаги

1. Склонируйте репозиторий:

   ```bash
   git clone <repository-url>
   cd taskmanager
   ```

2. Создайте базу данных PostgreSQL:

   ```sql
   CREATE DATABASE "rest-taskmanager-db";
   ```

3. Настройте переменные окружения или `application.properties`:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/rest-taskmanager-db
   spring.datasource.username=postgres
   spring.datasource.password=postgres
   ```

4. Соберите и запустите приложение:

   ```bash
   mvn clean install
   mvn spring-boot:run
   ```

5. Откройте браузер и перейдите на http://localhost:8080

6. Войдите как админ: username=`admin`, password=`admin123`, или зарегистрируйтесь как новый пользователь.
После входа вас сразу перенаправит на страницу управления задачами.

---

## Запуск с Docker

### Требования

* Docker и Docker Compose

### Шаги

1. Склонируйте репозиторий:

   ```bash
   git clone <repository-url>
   cd taskmanager
   ```

2. Создайте файл `.env` в корне проекта:

   ```env
   JWT_SECRET=your-secret-key-here
   ```

3. Соберите приложение:

   ```bash
   mvn clean package -DskipTests
   ```

4. Запустите с Docker Compose:

   ```bash
   docker-compose up --build
   ```

5. Откройте браузер: http://localhost:8080

6. Войдите как админ: username=`admin`, password=`admin123`, или зарегистрируйтесь как новый пользователь.
   После входа вас сразу перенаправит на страницу управления задачами.

### Остановка

```bash
docker-compose down
```

---

## API Документация

Swagger UI: http://localhost:8080/swagger-ui/index.html

### Основные эндпоинты API

#### Аутентификация
- `POST /api/auth/register` - Регистрация нового пользователя
- `POST /api/auth/login` - Вход в систему
- `GET /api/auth/me` - Получить информацию о текущем пользователе
- `POST /api/auth/change-password` - Изменить пароль
- `POST /api/auth/logout` - Выход из системы

#### Задачи
- `GET /api/tasks` - Получить активные задачи
- `GET /api/tasks/completed` - Получить выполненные задачи
- `POST /api/tasks` - Создать новую задачу
- `PUT /api/tasks/{id}` - Обновить задачу
- `PUT /api/tasks/{id}/done` - Отметить как выполненную
- `PUT /api/tasks/{id}/undone` - Вернуть в активные
- `DELETE /api/tasks/{id}` - Удалить задачу

Все API эндпоинты требуют аутентификации (кроме регистрации и входа), используйте Basic Auth в Swagger.

### Веб-интерфейс Endpoints

| Метод | Путь | Описание |
|-------|------|----------|
| GET | /login | Страница входа |
| GET | /register | Страница регистрации |
| POST | /register | Регистрация пользователя |
| GET | /tasks | Список активных задач |
| POST | /tasks | Создание задачи |
| POST | /tasks/{id}/delete | Удаление задачи |
| POST | /tasks/{id}/done | Отметка задачи как выполненной |
| GET | /tasks/completed | Список выполненных задач |
| GET | /admin/metrics | Метрики системы (только для админов) |

---

## Тестирование

Запуск тестов:

```bash
mvn test
```

Тесты включают:
- Unit-тесты репозиториев (@DataJpaTest)
- Интеграционные тесты сервисов (@SpringBootTest)
- Тесты контроллеров (@WebMvcTest)

---

## Безопасность

- Аутентификация через Spring Security с JWT
- Шифрование паролей BCrypt
- Ролевая модель (USER, ADMIN)
- Защита от CSRF
- Ограничение доступа к задачам других пользователей

---

## Разработка

Проект следует принципам SOLID и использует паттерны:
- Service Layer
- Repository Pattern
- MVC

Для запуска в режиме разработки:
```bash
mvn spring-boot:run
```

Приложение будет доступно на http://localhost:8080

---


## Дополнительные реализованные функции

### Docker контейнер
Приложение упаковано в Docker контейнер с использованием Docker Compose для запуска PostgreSQL и приложения. 
JWT секретный ключ берется из файла `.env`.

### Расширенное логирование (SLF4J + Logback)
Используется SLF4J + Logback для логирования операций в сервисе задач, включая предупреждения и 
информационные сообщения. В контроллерах добавлено логирование ошибок с выводом исключений.

---

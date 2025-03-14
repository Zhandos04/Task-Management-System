﻿# Task Management System

**Task Management System** – это REST API-сервис для управления задачами, разработанный на `Spring Boot`.  
Проект поддерживает **JWT-аутентификацию**, **ролевую систему (USER, ADMIN)**, **управление задачами и комментариями**, а также **Docker** для удобного развертывания.

---

## Функционал

✔ **Аутентификация и авторизация** (JWT, верификация email)  
✔ **Роли пользователей: `USER` и `ADMIN`**  
✔ **Пользователи могут:**
- Создавать, редактировать и удалять свои задачи
- Добавлять комментарии к своим задачам
- Менять статус своей задачи

✔ **Администраторы могут:**
- Управлять всеми задачами (редактировать, удалять, менять статус/приоритет)
- Добавлять комментарии к любым задачам  

✔ **Фильтрация и пагинация задач**  
✔ **Обработка ошибок с `GlobalExceptionHandler`**  
✔ **Docker Compose для быстрого развертывания**  
✔ **Полное покрытие тестами (`JUnit 5`, `Mockito`)**  
✔ **Документация API через `Swagger`**

---

## Технологии

- **Java 17**
- **Spring Boot 3** (Spring Security, Spring Data JPA, Spring Validation, JavaMailSender)
- **PostgreSQL**  (База данных)
- **JWT** (JSON Web Token)
- **Docker, Docker Compose** (Контейнеризация)
- **Gradle** ⚙ (Система сборки)
- **JUnit 5, Mockito** (Тестирование)
- **Swagger / OpenAPI** (Документация API)
- **ModelMapper** (Маппинг DTO)

---

## Как запустить проект?

### Установи зависимости
Перед запуском убедись, что у тебя установлены:  
- **Java 17**
- **Docker + Docker Compose**
- **Gradle** 

### Клонируй репозиторий
```sh
git clone https://github.com/Zhandos04/Task-Management-System.git
cd task-management-system
```
### Запусти через Docker Compose 
```sh
docker-compose up --build
```

## Аутентификация
После регистрации пользователь получает JWT-токен, который нужен для всех API-запросов.

### Как получить токен?
Отправь POST-запрос на /auth/login:
```sh
{
"email": "user@example.com",
"password": "YourPassword123!"
}
```
Ответ:
```sh
{
"accessToken": "JWT_ACCESS_TOKEN",
"refreshToken": "JWT_REFRESH_TOKEN",
"role": "USER"
}
```
После этого добавляй токен в заголовок Authorization для всех запросов:
```sh
Authorization: Bearer JWT_ACCESS_TOKEN
```
### Обновление токена
Если accessToken истёк, отправь refreshToken на /auth/refresh-token:
```sh
POST /auth/refresh-token
Authorization: Bearer JWT_REFRESH_TOKEN
```
Ответ:
```sh
{
  "accessToken": "NEW_ACCESS_TOKEN",
  "refreshToken": "NEW_REFRESH_TOKEN"
}
```
## API-документация
**Swagger UI: http://localhost:8080/swagger-ui.html**
## Основные эндпоинты API
### Аутентификация (/auth)

- **POST /api/auth/signup – Регистрация**
- **POST /api/auth/verify-email – Верификация email**
- **POST /api/auth/login – Логин**
- **POST /api/auth/logout – Выход**
- **POST /api/auth/refresh-token – Обновление токена**
- **POST /api/auth/forgot-password – Восстановление пароля**
- **POST /api/auth/update-password – Смена пароля**
### Работа с задачами (/tasks)

- **POST /api/tasks/create – Создать задачу**
- **GET /api/tasks/my – Получить свои задачи**
- **GET /api/tasks/all – Получить все задачи**
- **PUT /api/tasks/edit/{id} – Обновить задачу (только свою)**
- **DELETE /api/tasks/delete/{id} – Удалить задачу (только свою)**
- **PATCH /api/tasks/{taskId}/change-status – Изменить статус**
- **POST /api/tasks/{taskId}/add-comment – Добавить комментарий**
### Администратор (/admin/tasks)

- **PUT /api/admin/tasks/edit/{id} – Обновить любую задачу**
- **DELETE /api/admin/tasks/delete/{id} – Удалить любую задачу**
- **PATCH /api/admin/tasks/{taskId}/change-status – Изменить статус**
- **PATCH /api/admin/tasks/{taskId}/change-priority – Изменить приоритет**
- **POST /api/admin/tasks/{taskId}/add-comment – Добавить комментарий**
## Тестирование
### Запуск всех тестов:
```sh
./gradlew test
```

# MVC Hibernate CRUD

Portfolio web application for user management built with Spring MVC, Hibernate, Thymeleaf, Flyway and PostgreSQL.

## Features

- layered architecture: controller, service, DAO
- server-side validation for user forms
- unique email validation before persistence
- audit fields: `createdAt`, `updatedAt`
- Flyway database migrations
- centralized MVC error handling
- Thymeleaf-based UI for listing, creating, editing and deleting users

## Stack

- Java 17+
- Spring MVC 5
- Hibernate ORM 5
- Thymeleaf
- PostgreSQL
- Flyway
- Maven WAR packaging

## Project structure

```text
src/main/java/web
  config/        Spring MVC and database configuration
  controller/    MVC controllers and global exception handling
  dao/           Persistence layer
  model/         JPA entities
  service/       Business logic

src/main/resources
  db.properties
  db/migration

src/webapp
  WEB-INF/pages
  resources/css
```

## Database configuration

Local configuration is stored in `src/main/resources/db.properties`.

Current local settings:

- database: `mvc-hibernate-crud`
- username: `app_user`
- password: `12345`
- url: `jdbc:postgresql://localhost:5432/mvc-hibernate-crud`

For sharing or future deployment use `src/main/resources/db.properties.example` as a template.

## Run locally

1. Make sure PostgreSQL is running and the database `mvc-hibernate-crud` exists.
2. Ensure the user `app_user` with password `12345` has access to that database.
3. Build the project with Maven:

```bash
mvn clean package
```

4. Deploy the generated WAR to your servlet container.
5. Open:

```text
http://localhost:8080/mvc-hibernate-crud/users
```

## Flyway migrations

Schema migrations are stored in:

```text
src/main/resources/db/migration
```

The first migration creates the `users` table and required indexes.

## Current status

The project has been upgraded from a basic educational CRUD toward a more production-like portfolio application:

- PostgreSQL configured instead of MySQL
- cleaner Spring configuration
- better validation and user feedback
- audit fields and unique email support
- improved UI and error handling
- explicit database migration instead of implicit schema mutation

## Next improvements

- add Maven Wrapper (`mvnw`, `mvnw.cmd`)
- add unit and integration tests
- add Docker Compose for PostgreSQL and app startup
- add GitHub Actions CI
- add search, sorting and pagination

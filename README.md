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
- Maven Wrapper
- Docker Compose
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

scripts
  reset-db.sql
```

## Database configuration

Local fallback configuration is stored in `src/main/resources/db.properties`.
The application also supports environment variable overrides for Docker and CI:

- `DB_DRIVER`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `HIBERNATE_SHOW_SQL`
- `HIBERNATE_HBM2DDL_AUTO`
- `HIBERNATE_FORMAT_SQL`
- `HIBERNATE_DIALECT`

Current local settings:

- database: `mvc-hibernate-crud`
- username: `app_user`
- password: `12345`
- url: `jdbc:postgresql://localhost:5432/mvc-hibernate-crud`

For sharing or future deployment use `src/main/resources/db.properties.example` as a template.

## Run locally

1. Make sure PostgreSQL is running and the database `mvc-hibernate-crud` exists.
2. Ensure the user `app_user` with password `12345` has access to that database.
3. Build the project with Maven Wrapper:

```bash
./mvnw clean package
```

For Windows PowerShell:

```powershell
.\mvnw.cmd clean package
```

4. Deploy the generated WAR from `target/mvc-hibernate-crud.war` to your servlet container.
5. Open:

```text
http://localhost:8080/mvc-hibernate-crud/users
```

## Run with Docker

1. Copy `.env.example` to `.env` if you want to customize local values.
2. Start the stack:

```bash
docker compose up --build
```

3. Open:

```text
http://localhost:8080/users
```

The Docker setup runs:

- PostgreSQL 17
- Flyway migrations on application startup
- Tomcat with the built WAR deployed as the root app

## Flyway migrations

Schema migrations are stored in:

```text
src/main/resources/db/migration
```

Current migrations:

- `V1__create_users_table.sql` - creates the `users` table and indexes
- `V2__seed_users.sql` - inserts demo users for local development

## Data lifecycle

By default the application keeps data between restarts.
This is the recommended behavior for the portfolio version of the project.

For local development and demos you have two reset options.

Reset only table contents while keeping schema:

```bash
psql -U app_user -d mvc-hibernate-crud -f scripts/reset-db.sql
```

After that the application starts with an empty `users` table.

Reset the entire Docker database volume:

```bash
docker compose down -v
docker compose up --build
```

That recreates PostgreSQL from scratch and reapplies all migrations, including demo seed data.

## Current status

The project has been upgraded from a basic educational CRUD toward a more production-like portfolio application:

- PostgreSQL configured instead of MySQL
- cleaner Spring configuration
- better validation and user feedback
- audit fields and unique email support
- improved UI and error handling
- explicit database migration instead of implicit schema mutation
- managed seed data and explicit database reset flow
- reproducible build via Maven Wrapper
- containerized local environment via Docker Compose

## Next improvements

- add unit and integration tests
- add GitHub Actions CI
- add search, sorting and pagination

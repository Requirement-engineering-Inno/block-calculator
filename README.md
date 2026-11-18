# Block Calculator

A web application for calculating material requirements (cement, crushed stone, and sand) and production costs for concrete blocks.

Java 21, Spring Boot 3.5, Thymeleaf + htmx, MongoDB, PDF generation via OpenPDF. A single repository, a single JAR.

System description and user stories: [docs/USER_STORIES.md](docs/USER_STORIES.md).

## Getting Started

### Using Docker

Start the application and MongoDB:

```bash
docker compose up --build
```

Open [http://localhost:8080](http://localhost:8080).

Docker Compose starts the application in demo mode (`APP_DEMO_ENABLED=true`). On the first startup, it creates demo users, two projects, and several calculations.

Demo accounts can be selected with a single click on the login page:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin` | Admin |
| `elena`, `marcus` | `demo1234` | Manager |
| `ivan`, `maria`, `john` | `demo1234` | Worker |

For a production deployment, disable `APP_DEMO_ENABLED` and set your own `ADMIN_PASSWORD`. Without demo mode, only an admin account (password from `ADMIN_PASSWORD`, default: `admin`) and the "Other calculations" project are created.

Change the password after the first login.

## Tests

```bash
./gradlew test
```

Cucumber scenarios are located in `app/src/test/resources/features`, with one directory per user story.

Run a single user story tests:

```bash
./gradlew test -Dcucumber.filter.tags="@US-06"
```

Test report: `app/build/reports/cucumber.html`.
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

## Application Settings

Managers and admins open **Settings** in the sidebar to manage values shared by all projects:

- the list of currencies (projects choose their currency from it; a currency used by a project cannot be removed);
- the default wheelbarrow weight (WS), used when a calculation has no value of its own.

Sand and stone densities are fixed (1600 and 1450 kg/m³), a cement bag is 50 kg and a tonne is 1000 kg. Prices can be entered per kg, per m³, per tonne, or per bag (cement).

## Welcome E-mail

When a manager creates an employee, the e-mail address is required and the employee receives an e-mail with the login, password, and role.

- With Docker Compose, e-mails are caught by [Mailpit](https://mailpit.axllent.org/): open [http://localhost:8025](http://localhost:8025).
- For a real SMTP server, set `APP_MAIL_ENABLED=true`, `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT` (and `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `MAIL_FROM`, `PUBLIC_URL` as needed).
- Without mail settings, the account is still created and the manager is told to pass the credentials on personally.

## Input Limits

Prices up to 1,000,000, blocks up to 10,000,000, WS up to 10,000 (`common/Limits.java`).

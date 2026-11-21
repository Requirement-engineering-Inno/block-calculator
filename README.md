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

Managers and admins open **Settings** in the sidebar. It has three tabs: **General** (values shared by all production areas), **Users** and **My account**. Workers only have **My account**.

General settings:

- the list of currencies (projects choose their currency from it; a currency used by a project cannot be removed);
- the default wheelbarrow weight (WS), used when a calculation has no value of its own.

Sand and stone densities are fixed (1600 and 1450 kg/m³), a cement bag is 50 kg and a tonne is 1000 kg. Prices can be entered per kg, per m³, per tonne, or per bag (cement).

## Navigation and terms

The interface uses two levels: a **production area** (prices, currency, team) contains **projects** (block estimates).
There is no separate list of estimates in the menu: open a production area to see, create and share its projects.
Inside the code the old names are kept: a production area is `Project` / `/projects`, a project is `Calculation` / `/calculations`.
`/calculations` still opens the flat list of all projects, but it is not linked from the menu.

- **Workers see no money.** They get the amounts of materials and the mix proportions only; prices, costs, totals and currencies are removed on the server (`CalcResult.withoutMoney()`), also in the live preview and in the PDF.
- **Access levels** of a shared project are changed by clicking the level in the sharing list (Read only / Read and write / Remove access).
- **Welcome tour** opens by itself on the first visit (stored per user in `UserAccount.onboardingDone`) and can be repeated with the *Take a tour* button on the home page. Question-mark icons and hover hints explain the components.

## Welcome E-mail

When a manager creates an employee, the e-mail address is required and the employee receives an e-mail with the login, password, and role.

- With Docker Compose, e-mails are caught by [Mailpit](https://mailpit.axllent.org/): open [http://localhost:8025](http://localhost:8025).
- For a real SMTP server, set `APP_MAIL_ENABLED=true`, `SPRING_MAIL_HOST`, `SPRING_MAIL_PORT` (and `SPRING_MAIL_USERNAME`, `SPRING_MAIL_PASSWORD`, `MAIL_FROM`, `PUBLIC_URL` as needed).
- Without mail settings, the account is still created and the manager is told to pass the credentials on personally.

## Input Limits

Prices up to 1,000,000, blocks up to 10,000,000, WS up to 10,000 (`common/Limits.java`).

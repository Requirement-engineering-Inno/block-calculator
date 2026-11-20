# User Stories and Checklist

Each user story has its own directory in `app/src/test/resources/features/`, containing `.feature` files (Cucumber/Gherkin).

Each scenario is a checklist item. Mark it `[x]` when the scenario passes in `./gradlew test`.

Run: all tests `./gradlew test`, a single user story `./gradlew test -Dcucumber.filter.tags="@US-06"`.

Report: `app/build/reports/cucumber.html`.

## Roles

| Role | Description |
|---|---|
| Admin | One by default; has all manager permissions and can create managers |
| Manager | Can view all projects and calculations, set prices, create workers, freeze and delete calculations |
| Worker | Can view their own and shared calculations, create their own calculations, and work in assigned projects and "Other calculations" |

## Input Limits

| Field | Maximum |
|---|---|
| Material price | 1,000,000 |
| Blocks (whole and half blocks separately) | 10,000,000 |
| WS (wheelbarrow weight) | 10,000 |
| Exchange rate when changing currency | 1,000,000, and prices after conversion must not exceed 1,000,000 |

## Summary

| User Story | Title                                      | Files | Scenarios |
|------------|--------------------------------------------|---|---|
| US-01      | Login and logout                           | 1 | 7 |
| US-02      | Creating, blocking, and searching accounts | 4 | 23 |
| US-03      | Changing your own password                 | 1 | 4 |
| US-04      | Projects and their settings                | 1 | 9 |
| US-05      | Assigning workers to projects              | 1 | 9 |
| US-06      | Material and cost calculation              | 1 | 12 |
| US-07      | Prices in kg/m³/tonnes/bags and project currency | 2 | 12 |
| US-08      | Saving and listing calculations            | 1 | 8 |
| US-09      | Calculation access                         | 1 | 11 |
| US-10      | Editing calculations                       | 1 | 8 |
| US-11      | Archiving                                  | 1 | 6 |
| US-12      | Deleting calculations                      | 1 | 4 |
| US-13      | Freezing calculation prices                | 1 | 8 |
| US-14      | PDF export and printing                    | 1 | 10 |
| US-15      | Switching languages                        | 1 | 5 |
| US-16      | Role-based access                          | 1 | 5 |
| ~~US-17~~  | ~~Demo data~~ | 1 | 4 |
| US-18      | Employee profiles and "My Account"         | 1 | 11 |
| US-19      | Application settings (wheelbarrow, currencies) | 1 | 12 |
| US-20      | Welcome e-mail for new employees           | 1 | 6 |
|            | **Total**                                  | | **174** |

## US-01. Login and Logout

As a user, I want to log in with a username and password so that only company employees can use the application.

**`features/us01-login/login.feature`**

- [ ] A user signs in with correct credentials
- [ ] Wrong password is rejected
- [ ] A blocked user cannot sign in
- [ ] The default administrator exists
- [ ] An anonymous user is sent to the login page
- [ ] A user logs out
- [ ] Logo, styles and scripts load before signing in

## US-02. Creating, Blocking, and Searching Accounts

As an admin, I create manager accounts, and as a manager, I create worker accounts with an e-mail address; the list can be searched, filtered and paged because there can be very many workers; employees who left can be blocked.

**`features/us02-accounts/block-accounts.feature`**

- [ ] A manager blocks a worker
- [ ] A manager unblocks a worker
- [ ] A manager cannot block another manager
- [ ] The admin can block a manager

**`features/us02-accounts/create-accounts.feature`**

- [ ] The admin creates a manager
- [ ] The admin creates a worker
- [ ] A manager creates a worker
- [ ] A manager cannot create another manager
- [ ] A worker cannot manage accounts
- [ ] A duplicate login is rejected
- [ ] A short password is rejected
- [ ] The manager sees the workers in the user list
- [ ] The e-mail is required
- [ ] Two employees cannot share an e-mail

**`features/us02-accounts/find-accounts.feature`**

- [ ] Search by a part of the login
- [ ] Filter blocked accounts
- [ ] The admin filters managers
- [ ] A search without results shows nothing
- [ ] A manager does not find managers or the admin

**`features/us02-accounts/user-list-paging.feature`**

- [ ] The first page has 15 users
- [ ] The second page has the rest
- [ ] A page number out of range shows the last page
- [ ] The search is applied before paging

## US-03. Changing Your Own Password

As a user, I want to change my own password so that nobody else knows it.

**`features/us03-password/change-password.feature`**

- [ ] A user changes his password
- [ ] The old password stops working
- [ ] The current password must be correct
- [ ] A short new password is rejected

## US-04. Projects and Their Settings

As a manager, I want to create projects with their own prices and a currency chosen from the application list, so that every project is calculated under its own conditions. Prices are limited to 1,000,000. The "Other calculations" project always exists.

**`features/us04-projects/manage-projects.feature`**

- [ ] The built-in project "Other calculations" exists
- [ ] A manager creates a project with prices
- [ ] A project can use any currency from the list
- [ ] A currency that is not in the list is rejected
- [ ] Every manager sees all projects
- [ ] A negative price is rejected
- [ ] A worker cannot open the project settings
- [ ] A price of exactly one million is accepted
- [ ] A price above one million is rejected

## US-05. Assigning Workers to Projects

As a manager, I want to find workers using search and assign them to projects (a worker can belong to multiple projects), so that they only work with the projects they are assigned to.

**`features/us05-assign-workers/assign-workers.feature`**

- [ ] A manager assigns workers to a project
- [ ] A manager removes a worker from a project
- [ ] One worker can belong to several projects
- [ ] A worker does not see projects he is not assigned to
- [ ] A worker cannot create a calculation in a project he is not assigned to
- [ ] A manager searches workers to assign
- [ ] Search matches the login case-insensitively
- [ ] Workers can search colleagues to share a calculation
- [ ] Search is closed for anonymous users

## US-06. Material and Cost Calculation

As a worker, I want to enter the number of full and half blocks (up to 10,000,000) and choose a mix (mix 1–4), so that I get the cement, stone and sand quantities and the cost without pen and paper. The wheelbarrow weight (WS) comes from the application settings unless entered by hand.

**`features/us06-calculate/calculate-materials.feature`**

- [ ] The customer's example, batches rounded up
- [ ] Without rounding the batches stay fractional
- [ ] A half block counts as 0.5
- [ ] Batch 1 uses no stone
- [ ] WS comes from the application settings when it is not entered
- [ ] WS entered by hand overrides the project default
- [ ] The form recalculates while the user types
- [ ] A calculation without blocks is rejected
- [ ] An unknown batch is rejected
- [ ] A huge number of blocks is rejected
- [ ] More blocks than the limit are rejected
- [ ] A huge WS is rejected

## US-07. Prices in kg, m³, Tonnes or Bags and Project Currency

As a manager, I want to enter prices in the unit the supplier uses (kg, m³, tonne, or a 50 kg bag for cement) and change the currency of a project with a rate, so that I do not convert anything by hand. Sand and stone densities are fixed (1600 and 1450 kg/m³).

**`features/us07-prices-currency/currency-conversion.feature`**

- [ ] Changing the currency converts all prices with the rate
- [ ] Results are shown in the new currency
- [ ] The currency can also be set directly in the settings
- [ ] A rate of zero is rejected
- [ ] A worker cannot change the currency
- [ ] A conversion that pushes a price above one million is rejected
- [ ] A currency outside the application list is rejected

**`features/us07-prices-currency/price-units.feature`**

- [ ] Prices per m³ and per bag are converted to kg
- [ ] Prices per tonne are converted to kg
- [ ] The fixed densities are used to convert m³ to kg
- [ ] Every project has its own prices
- [ ] A cement price per m³ is not allowed

## US-08. Saving and Listing Calculations

As a worker, I want calculations to be saved together with their author and timestamps (time is stored in GMT, and the browser displays it in the user's time zone), while managers can see all calculations.

**`features/us08-save-and-list/save-and-list.feature`**

- [ ] A saved calculation keeps its author and the project
- [ ] A calculation without a project goes to "Other calculations"
- [ ] A worker sees only his own calculations
- [ ] A manager sees all calculations of all projects
- [ ] A manager sees who created a calculation
- [ ] The list can be filtered by project
- [ ] A worker cannot open a calculation of a colleague
- [ ] Times are shown in GMT and can be converted by the browser

## US-09. Calculation Access

As a calculation author, I want to share my calculations with colleagues (read-only or read-write access). As a manager, I want to share a calculation with all workers in a project with a single click.

**`features/us09-sharing/share-calculation.feature`**

- [ ] A worker shares his calculation read-only
- [ ] A worker shares his calculation read-write
- [ ] Without sharing a colleague cannot open the calculation
- [ ] A shared calculation appears in the list of the colleague
- [ ] Changing the access level of a colleague
- [ ] The author takes the access back
- [ ] The sharing is listed on the calculation page
- [ ] A manager shares a calculation with all workers of the project
- [ ] Only a manager can share with all workers of a project
- [ ] The "share with all" button is shown to managers
- [ ] A colleague with read-write access cannot share further

## US-10. Editing Calculations

As a worker with write access, I want to edit a calculation. The system must remember who edited it last and when.

**`features/us10-edit/edit-calculation.feature`**

- [ ] The author edits his calculation
- [ ] The result is recalculated after the edit
- [ ] A colleague with read-write access edits, the author stays the same
- [ ] A colleague with read-only access cannot edit
- [ ] A read-only colleague does not get the edit form
- [ ] A worker without access cannot edit
- [ ] A manager edits any calculation
- [ ] An invalid edit is rejected and nothing changes

## US-11. Archiving

As a worker with write access, I want to move an unnecessary calculation to the archive (where it is displayed in grey) instead of deleting it.

**`features/us11-archive/archive-calculation.feature`**

- [ ] The author archives his calculation
- [ ] An archived calculation can be restored
- [ ] A colleague with read-write access archives
- [ ] A colleague with read-only access cannot archive
- [ ] An archived calculation is still readable and marked
- [ ] A manager archives any calculation

## US-12. Deleting Calculations

As a manager, I want to delete calculations. Workers must not be allowed to delete calculations.

**`features/us12-delete/delete-calculation.feature`**

- [ ] A manager deletes a calculation
- [ ] The admin deletes a calculation
- [ ] A worker cannot delete even his own calculation
- [ ] The delete button is shown to managers only

## US-13. Freezing Calculation Prices

As a manager, I want to freeze a calculation so that its prices do not change when project prices are updated.

**`features/us13-freeze-prices/freeze-prices.feature`**

- [ ] A manager freezes a calculation
- [ ] A frozen calculation keeps its prices, a normal one follows the project
- [ ] Unfreezing recalculates with the current prices
- [ ] The frozen mark is shown in the list
- [ ] A frozen calculation cannot be edited
- [ ] The edit button is hidden on a frozen calculation
- [ ] A worker cannot freeze
- [ ] Only managers see the freeze button

## US-14. PDF Export and Printing

As a user, I want to download a calculation as a PDF or print it directly (using my time zone, with Cyrillic characters supported).

**`features/us14-pdf-export/export-pdf.feature`**

- [ ] A worker exports his calculation
- [ ] A manager exports any calculation
- [ ] The PDF works with the Russian interface (Cyrillic font)
- [ ] A worker without access cannot export
- [ ] The PDF button is on the calculation page
- [ ] The PDF uses the time zone of the browser
- [ ] An unknown time zone falls back to GMT
- [ ] The calculation page offers printing
- [ ] The PDF can be opened inline for printing
- [ ] Printing is not possible without access

## US-15. Switching Languages

As a user, I want to switch the interface language (a new language should be added using a single translation file).

**`features/us15-language/switch-language.feature`**

- [ ] The interface is in English by default
- [ ] A user switches to Russian
- [ ] The chosen language is remembered
- [ ] A user switches back to English
- [ ] The login page can be switched before signing in

## US-16. Role-Based Access

As a company, we want each role to have access only to its designated pages. The admin has the same permissions as a manager.

**`features/us16-role-access/role-access.feature`**

- [ ] Management pages by role
- [ ] Everybody who is signed in can open the calculation list
- [ ] Management pages are closed for anonymous users
- [ ] The admin has the same rights on calculations as a manager
- [ ] Workers do not see the management links

## US-18. Employee Profiles and "My Account"

As a manager, I want to open an employee's profile (projects, calculations, and status). As any user, I want to view my own information alongside the password change form.

**`features/us18-profiles/employee-profiles.feature`**

- [ ] A manager opens the profile of a worker
- [ ] The profile lists the projects of the worker
- [ ] The profile lists the calculations of the worker
- [ ] A blocked worker is shown as blocked
- [ ] The profile is reachable from the user list
- [ ] A manager cannot open the profile of another manager
- [ ] The admin opens the profile of a manager
- [ ] A worker cannot open the profile of a colleague
- [ ] An unknown profile gives 404
- [ ] My account shows the information about me
- [ ] A manager has an account page too

## US-19. Application Settings

As a manager, I want to keep the wheelbarrow weight and the list of currencies in one place, so that every project uses the same values and projects only choose a currency from the list.

**`features/us19-global-settings/global-settings.feature`**

- [ ] The settings page is available to managers
- [ ] A worker cannot open the settings
- [ ] A manager changes the wheelbarrow weight
- [ ] The new weight is used by calculations without their own value
- [ ] A wrong wheelbarrow weight is rejected
- [ ] A manager adds a currency
- [ ] A new currency can be chosen in a project
- [ ] A wrong currency code is rejected
- [ ] The same currency cannot be added twice
- [ ] A currency that nobody uses can be removed
- [ ] A currency used by a project cannot be removed
- [ ] A worker cannot change the settings

## US-20. Welcome E-mail

As a manager, I want a new employee to receive an e-mail with the login, password and role, so that I do not have to pass the credentials on myself.

**`features/us20-welcome-email/welcome-email.feature`**

- [ ] The e-mail contains the login, the password and the role
- [ ] The e-mail uses the language of the manager
- [ ] The admin creates a manager and the e-mail names the role
- [ ] An invalid e-mail address creates nothing and sends nothing
- [ ] A broken mail server does not stop the registration
- [ ] The e-mail address is shown in the profile of the employee

## Out of Scope for the First Release

- [ ] Automatic retrieval of material prices
- [ ] Automatic currency exchange rates
- [ ] Importing house CAD plans with automatic calculations
- [ ] Calculation version history (only the latest changes are stored)

## Assumptions to Confirm with the Customer

- Workers can create calculations in "Other calculations" and only see their own calculations and those shared with them.
- Densities are fixed: sand 1600 kg/m³, crushed stone 1450 kg/m³; a cement bag weighs 50 kg; a tonne is 1000 kg.
- The wheelbarrow weight (WS) is global and is the same for sand and crushed stone; a calculation can override it.
- Currencies are global and managed by managers and admins; a currency that a project uses cannot be removed.
- Only a manager or admin can freeze and unfreeze a calculation. A frozen calculation cannot be edited.
- The welcome e-mail contains the password in plain text, as requested. Employees should change it after the first sign-in.

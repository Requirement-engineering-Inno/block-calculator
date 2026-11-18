package org.example.blockcalc.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import org.example.blockcalc.calc.AccessLevel;
import org.example.blockcalc.calc.Calculation;
import org.example.blockcalc.calc.Grant;
import org.example.blockcalc.project.PriceEntry;
import org.example.blockcalc.project.PriceUnit;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Map;

/** "Given" steps: arrange the database and choose who is acting. */
public class SetupSteps {

    @Autowired
    private TestData data;
    @Autowired
    private World world;

    @Given("the following users exist:")
    public void usersExist(DataTable table) {
        for (Map<String, String> row : table.asMaps()) {
            data.user(row.get("username"), Role.valueOf(row.get("role")));
        }
    }

    @Given("project {string} exists")
    public void projectExists(String name) {
        data.project(name);
    }

    @Given("project {string} has these prices:")
    public void projectPrices(String name, DataTable table) {
        Project p = data.projectByName(name);
        for (Map<String, String> row : table.asMaps()) {
            PriceEntry price = new PriceEntry(new BigDecimal(row.get("amount")), PriceUnit.valueOf(row.get("unit")));
            switch (row.get("material")) {
                case "sand" -> p.sandPrice = price;
                case "stone" -> p.stonePrice = price;
                case "cement" -> p.cementPrice = price;
                default -> throw new AssertionError("Unknown material " + row.get("material"));
            }
        }
        data.save(p);
    }

    @Given("project {string} has default WS {int}")
    public void projectWs(String name, int ws) {
        Project p = data.projectByName(name);
        p.wsDefault = BigDecimal.valueOf(ws);
        data.save(p);
    }

    @Given("worker {string} is assigned to project {string}")
    public void workerAssigned(String username, String projectName) {
        Project p = data.projectByName(projectName);
        p.workerIds.add(data.userByName(username).id);
        data.save(p);
    }

    /** The customer's example: 1800 full and 200 half blocks, batch 4, WS 150, batches rounded up. */
    @Given("calculation {string} exists in project {string} created by {string}")
    public void calculationExists(String title, String project, String author) {
        data.calc(title, project, author, 1800, 200, 4, true);
    }

    @Given("calculation {string} exists in project {string} created by {string} with {int} full and {int} half blocks using batch {int}")
    public void calculationExistsWith(String title, String project, String author, int full, int half, int batch) {
        data.calc(title, project, author, full, half, batch, false);
    }

    @Given("calculation {string} is shared with {string} as {string}")
    public void calculationShared(String title, String username, String level) {
        Calculation c = data.calcByTitle(title);
        c.grants.add(new Grant(data.userByName(username).id, level(level)));
        data.save(c);
    }

    @Given("calculation {string} is archived")
    public void calculationArchived(String title) {
        Calculation c = data.calcByTitle(title);
        c.archived = true;
        data.save(c);
    }

    @Given("calculation {string} is frozen")
    public void calculationFrozen(String title) {
        data.freeze(data.calcByTitle(title));
    }

    @Given("user {string} is blocked")
    public void userBlocked(String username) {
        UserAccount u = data.userByName(username);
        u.active = false;
        data.saveUser(u);
    }

    @Given("I am logged in as {string}")
    public void loggedInAs(String username) {
        world.loginAs(username);
    }

    @Given("I am not logged in")
    public void notLoggedIn() {
        world.anonymous();
    }

    static AccessLevel level(String text) {
        return switch (text) {
            case "read-only" -> AccessLevel.READ_ONLY;
            case "read-write" -> AccessLevel.READ_WRITE;
            default -> throw new AssertionError("Unknown access level: " + text);
        };
    }
}

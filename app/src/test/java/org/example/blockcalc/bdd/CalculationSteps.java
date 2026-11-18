package org.example.blockcalc.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.blockcalc.calc.AccessLevel;
import org.example.blockcalc.calc.Calculation;
import org.example.blockcalc.calc.Grant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Creating, changing and sharing calculations. */
public class CalculationSteps {

    @Autowired
    private TestData data;
    @Autowired
    private World world;

    // ---- create / preview / edit ---------------------------------------------------------

    /** Columns: title, project, full, half, batch, ws, roundUp. A blank project means "not chosen". */
    @When("I create a calculation:")
    public void create(DataTable table) throws Exception {
        world.post("/calculations", createParams(table.asMaps().get(0)));
    }

    @When("I preview a calculation:")
    public void preview(DataTable table) throws Exception {
        world.post("/calculations/preview", createParams(table.asMaps().get(0)));
    }

    /** Starts from the stored values and changes only the columns of the table. */
    @When("I edit calculation {string} changing:")
    public void edit(String title, DataTable table) throws Exception {
        Calculation c = data.calcByTitle(title);
        Map<String, String> row = table.asMaps().get(0);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("projectId", c.projectId);
        params.add("title", row.getOrDefault("title", c.title));
        params.add("fullBlocks", row.getOrDefault("full", String.valueOf(c.fullBlocks)));
        params.add("halfBlocks", row.getOrDefault("half", String.valueOf(c.halfBlocks)));
        params.add("batch", row.getOrDefault("batch", String.valueOf(c.batch)));
        params.add("ws", row.getOrDefault("ws", c.ws.toPlainString()));
        if (Boolean.parseBoolean(row.getOrDefault("roundUp", String.valueOf(c.roundUp)))) {
            params.add("roundUp", "on");
        }
        world.post("/calculations/" + c.id, params);
    }

    private MultiValueMap<String, String> createParams(Map<String, String> row) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        String project = row.get("project");
        if (project != null && !project.isBlank()) {
            params.add("projectId", data.projectByName(project).id);
        }
        addIfPresent(params, "title", row.get("title"));
        addIfPresent(params, "fullBlocks", row.get("full"));
        addIfPresent(params, "halfBlocks", row.get("half"));
        addIfPresent(params, "batch", row.get("batch"));
        addIfPresent(params, "ws", row.get("ws"));
        if (Boolean.parseBoolean(row.get("roundUp"))) {
            params.add("roundUp", "on");
        }
        return params;
    }

    private static void addIfPresent(MultiValueMap<String, String> params, String name, String value) {
        if (value != null && !value.isBlank()) {
            params.add(name, value);
        }
    }

    // ---- opening pages -------------------------------------------------------------------

    @When("I open {string}")
    public void open(String path) throws Exception {
        world.get(path);
    }

    @When("I open the calculation list")
    public void openList() throws Exception {
        world.get("/calculations");
    }

    @When("I open the calculation list filtered by project {string}")
    public void openListFiltered(String project) throws Exception {
        world.get("/calculations?projectId=" + data.projectByName(project).id);
    }

    @When("I open the new calculation form")
    public void openNewForm() throws Exception {
        world.get("/calculations/new");
    }

    @When("I open calculation {string}")
    public void openCalculation(String title) throws Exception {
        world.get("/calculations/" + data.calcByTitle(title).id);
    }

    @When("I open the edit form of calculation {string}")
    public void openEditForm(String title) throws Exception {
        world.get("/calculations/" + data.calcByTitle(title).id + "/edit");
    }

    @When("I switch the language to {string}")
    public void switchLanguage(String lang) throws Exception {
        world.get("/calculations?lang=" + lang);
    }

    @When("I export calculation {string} to PDF")
    public void exportPdf(String title) throws Exception {
        world.get("/calculations/" + data.calcByTitle(title).id + "/pdf");
    }

    @When("I open the PDF of calculation {string} for printing")
    public void openPdfForPrinting(String title) throws Exception {
        world.get("/calculations/" + data.calcByTitle(title).id + "/pdf?inline=true");
    }

    @When("I export calculation {string} to PDF in time zone {string}")
    public void exportPdfInZone(String title, String zone) throws Exception {
        world.get("/calculations/" + data.calcByTitle(title).id + "/pdf?tz=" + zone);
    }

    // ---- state changes -------------------------------------------------------------------

    @When("I archive calculation {string}")
    public void archive(String title) throws Exception {
        action(title, "archive");
    }

    @When("I restore calculation {string}")
    public void restore(String title) throws Exception {
        action(title, "restore");
    }

    @When("I delete calculation {string}")
    public void delete(String title) throws Exception {
        action(title, "delete");
    }

    @When("I freeze calculation {string}")
    public void freeze(String title) throws Exception {
        action(title, "freeze");
    }

    @When("I unfreeze calculation {string}")
    public void unfreeze(String title) throws Exception {
        action(title, "unfreeze");
    }

    private void action(String title, String action) throws Exception {
        world.post("/calculations/" + data.calcByTitle(title).id + "/" + action);
    }

    // ---- sharing -------------------------------------------------------------------------

    @When("I share calculation {string} with {string} as {string}")
    public void share(String title, String username, String level) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", data.userByName(username).id);
        params.add("level", SetupSteps.level(level).name());
        world.post("/calculations/" + data.calcByTitle(title).id + "/share", params);
    }

    @When("I share calculation {string} with all workers of its project as {string}")
    public void shareAll(String title, String level) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("level", SetupSteps.level(level).name());
        world.post("/calculations/" + data.calcByTitle(title).id + "/share-all", params);
    }

    @When("I stop sharing calculation {string} with {string}")
    public void unshare(String title, String username) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("userId", data.userByName(username).id);
        world.post("/calculations/" + data.calcByTitle(title).id + "/unshare", params);
    }

    // ---- stored state --------------------------------------------------------------------

    @Then("calculation {string} is stored with:")
    public void stored(String title, DataTable table) {
        Calculation c = data.calcByTitle(title);
        for (Map.Entry<String, String> e : table.asMap().entrySet()) {
            String expected = e.getValue();
            switch (e.getKey()) {
                case "full" -> assertEquals(Integer.parseInt(expected), c.fullBlocks, "full");
                case "half" -> assertEquals(Integer.parseInt(expected), c.halfBlocks, "half");
                case "batch" -> assertEquals(Integer.parseInt(expected), c.batch, "batch");
                case "ws" -> assertEquals(0, new BigDecimal(expected).compareTo(c.ws), "ws was " + c.ws);
                case "roundUp" -> assertEquals(Boolean.parseBoolean(expected), c.roundUp, "roundUp");
                case "frozen" -> assertEquals(Boolean.parseBoolean(expected), c.frozen, "frozen");
                case "archived" -> assertEquals(Boolean.parseBoolean(expected), c.archived, "archived");
                case "project" -> assertEquals(expected, data.projectNameById(c.projectId), "project");
                case "author" -> assertEquals(expected, data.usernameById(c.createdBy), "author");
                case "updated by" -> assertEquals(expected, data.usernameById(c.updatedBy), "updated by");
                default -> throw new AssertionError("Unknown calculation field " + e.getKey());
            }
        }
    }

    @Then("calculation {string} still exists")
    public void stillExists(String title) {
        assertTrue(data.calcExists(title), "Calculation " + title + " should exist");
    }

    @Then("calculation {string} no longer exists")
    public void noLongerExists(String title) {
        assertFalse(data.calcExists(title), "Calculation " + title + " should be deleted");
    }

    @Then("calculation {string} grants {string} {string} access")
    public void grants(String title, String username, String level) {
        Calculation c = data.calcByTitle(title);
        String userId = data.userByName(username).id;
        AccessLevel expected = SetupSteps.level(level);
        Grant grant = c.grants.stream().filter(g -> userId.equals(g.userId)).findFirst().orElse(null);
        assertNotNull(grant, username + " has no access to " + title);
        assertEquals(expected, grant.level);
    }

    @Then("calculation {string} grants {string} no access")
    public void grantsNone(String title, String username) {
        Calculation c = data.calcByTitle(title);
        String userId = data.userByName(username).id;
        assertNull(c.grants.stream().filter(g -> userId.equals(g.userId)).findFirst().orElse(null),
                username + " should have no grant on " + title);
    }
}

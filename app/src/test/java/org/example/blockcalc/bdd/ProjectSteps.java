package org.example.blockcalc.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.blockcalc.project.Project;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Project settings, prices, currency and worker assignment. */
public class ProjectSteps {

    @Autowired
    private TestData data;
    @Autowired
    private World world;

    /** Same defaults as the "new project" form. Table columns override them. */
    @When("I create a project:")
    public void createProject(DataTable table) throws Exception {
        Map<String, String> row = table.asMaps().get(0);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        put(params, row, "name", "");
        put(params, row, "currency", "RUB");
        put(params, row, "sandAmount", "0");
        put(params, row, "sandUnit", "PER_M3");
        put(params, row, "stoneAmount", "0");
        put(params, row, "stoneUnit", "PER_M3");
        put(params, row, "cementAmount", "0");
        put(params, row, "cementUnit", "PER_BAG");
        addWorkers(params, row.get("workers"));
        world.post("/projects", params);
    }

    /** Starts from the stored values and changes only the columns of the table. */
    @When("I update project {string} with:")
    public void updateProject(String name, DataTable table) throws Exception {
        Project p = data.projectByName(name);
        Map<String, String> row = table.asMaps().get(0);
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        put(params, row, "name", p.name);
        put(params, row, "currency", p.currency);
        put(params, row, "sandAmount", p.sandPrice.amount.toPlainString());
        put(params, row, "sandUnit", p.sandPrice.unit.name());
        put(params, row, "stoneAmount", p.stonePrice.amount.toPlainString());
        put(params, row, "stoneUnit", p.stonePrice.unit.name());
        put(params, row, "cementAmount", p.cementPrice.amount.toPlainString());
        put(params, row, "cementUnit", p.cementPrice.unit.name());
        if (row.containsKey("workers")) {
            addWorkers(params, row.get("workers"));
        } else {
            p.workerIds.forEach(id -> params.add("workerIds", id));
        }
        world.post("/projects/" + p.id, params);
    }

    @When("I change the currency of project {string} to {string} with rate {string}")
    public void changeCurrency(String name, String currency, String rate) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("currency", currency);
        params.add("rate", rate);
        world.post("/projects/" + data.projectByName(name).id + "/currency", params);
    }

    @Then("project {string} is stored with:")
    public void projectStored(String name, DataTable table) {
        Project p = data.projectByName(name);
        for (Map.Entry<String, String> e : table.asMap().entrySet()) {
            String key = e.getKey();
            String expected = e.getValue();
            switch (key) {
                case "currency" -> assertEquals(expected, p.currency);
                case "sandAmount" -> assertNumber(key, expected, p.sandPrice.amount);
                case "stoneAmount" -> assertNumber(key, expected, p.stonePrice.amount);
                case "cementAmount" -> assertNumber(key, expected, p.cementPrice.amount);
                case "sandUnit" -> assertEquals(expected, p.sandPrice.unit.name());
                case "stoneUnit" -> assertEquals(expected, p.stonePrice.unit.name());
                case "cementUnit" -> assertEquals(expected, p.cementPrice.unit.name());
                case "workers" -> assertEquals(names(expected), usernames(p.workerIds), "workers");
                default -> throw new AssertionError("Unknown project field " + key);
            }
        }
    }

    @Then("project {string} has been created")
    public void projectExistsCheck(String name) {
        data.projectByName(name);
    }

    @Then("project {string} has not been created")
    public void projectNotCreated(String name) {
        try {
            data.projectByName(name);
        } catch (AssertionError expected) {
            return;
        }
        throw new AssertionError("Project " + name + " exists but should not");
    }

    private static void put(MultiValueMap<String, String> params, Map<String, String> row, String key, String fallback) {
        String value = row.get(key);
        params.add(key, value == null || value.isBlank() ? fallback : value);
    }

    private void addWorkers(MultiValueMap<String, String> params, String workers) {
        for (String name : names(workers)) {
            params.add("workerIds", data.userByName(name).id);
        }
    }

    private static Set<String> names(String commaSeparated) {
        if (commaSeparated == null || commaSeparated.isBlank()) {
            return new HashSet<>();
        }
        return Arrays.stream(commaSeparated.split(",")).map(String::trim).collect(Collectors.toSet());
    }

    private Set<String> usernames(Set<String> ids) {
        return ids.stream().map(data::usernameById).collect(Collectors.toSet());
    }

    private static void assertNumber(String field, String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual), field + ": expected " + expected + " but was " + actual);
    }
}

package org.example.blockcalc.bdd;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.blockcalc.user.UserAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Login, logout, accounts and passwords. */
public class AccountSteps {

    @Autowired
    private TestData data;
    @Autowired
    private World world;

    @When("I log in with username {string} and password {string}")
    public void logIn(String username, String password) throws Exception {
        world.formLogin(username, password);
    }

    @When("I log out")
    public void logOut() throws Exception {
        world.post("/logout");
    }

    @When("I create user {string} with role {string}")
    public void createUser(String username, String role) throws Exception {
        createUserWithPassword(username, role, World.PASSWORD);
    }

    @When("I create user {string} with role {string} and password {string}")
    public void createUserWithPassword(String username, String role, String password) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("username", username);
        params.add("displayName", username);
        params.add("password", password);
        params.add("role", role);
        world.post("/users", params);
    }

    @When("I block user {string}")
    public void blockUser(String username) throws Exception {
        world.post("/users/" + data.userByName(username).id + "/block");
    }

    @When("I unblock user {string}")
    public void unblockUser(String username) throws Exception {
        world.post("/users/" + data.userByName(username).id + "/unblock");
    }

    @When("I change my password from {string} to {string}")
    public void changePassword(String current, String next) throws Exception {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("current", current);
        params.add("new", next);
        world.post("/account/password", params);
    }

    @When("{int} workers named {string} exist")
    public void manyWorkers(int count, String prefix) {
        for (int i = 1; i <= count; i++) {
            data.user(String.format("%s%02d", prefix, i), org.example.blockcalc.user.Role.WORKER);
        }
    }

    @When("I open the profile of {string}")
    public void openProfile(String username) throws Exception {
        world.get("/users/" + data.userByName(username).id);
    }

    @When("I open my account")
    public void openAccount() throws Exception {
        world.get("/account");
    }

    @When("I search users for {string}")
    public void searchUsers(String text) throws Exception {
        world.get("/users?q=" + text);
    }

    @When("I filter users by role {string} and status {string}")
    public void filterUsers(String role, String active) throws Exception {
        world.get("/users?role=" + role + "&active=" + active);
    }

    @When("I search workers for {string}")
    public void searchWorkers(String text) throws Exception {
        world.get("/directory/workers?q=" + text);
    }

    @Then("the worker search results contain {string}")
    public void workerResultsContain(String username) throws Exception {
        org.junit.jupiter.api.Assertions.assertFalse(
                world.document().select("button[data-pick][data-username=\"" + username + "\"]").isEmpty(), "Missing " + username);
    }

    @Then("the worker search results do not contain {string}")
    public void workerResultsDoNotContain(String username) throws Exception {
        assertTrue(world.document().select("button[data-pick][data-username=\"" + username + "\"]").isEmpty(), "Unexpected " + username);
    }

    @Then("the profile shows:")
    public void profileShows(io.cucumber.datatable.DataTable table) throws Exception {
        java.util.Map<String, String> ids = java.util.Map.of(
                "name", "#profile-name", "username", "#profile-username", "role", "#profile-role",
                "status", "#profile-status", "calculations", "#profile-authored");
        for (java.util.Map.Entry<String, String> e : table.asMap().entrySet()) {
            String selector = ids.get(e.getKey());
            org.junit.jupiter.api.Assertions.assertNotNull(selector, "Unknown profile field " + e.getKey());
            org.jsoup.nodes.Element el = world.document().selectFirst(selector);
            org.junit.jupiter.api.Assertions.assertNotNull(el, "Not on the page: " + e.getKey());
            assertEquals(e.getValue(), el.text().trim(), e.getKey());
        }
    }

    @Then("the profile lists project {string}")
    public void profileListsProject(String name) throws Exception {
        org.junit.jupiter.api.Assertions.assertFalse(
                world.document().select("[data-profile-project=\"" + name + "\"]").isEmpty(), "Project not listed: " + name);
    }

    @Then("the profile does not list project {string}")
    public void profileDoesNotListProject(String name) throws Exception {
        assertTrue(world.document().select("[data-profile-project=\"" + name + "\"]").isEmpty(), "Project listed: " + name);
    }

    @Then("the profile lists calculation {string}")
    public void profileListsCalculation(String title) throws Exception {
        org.junit.jupiter.api.Assertions.assertFalse(
                world.document().select("[data-profile-calc=\"" + title + "\"]").isEmpty(), "Calculation not listed: " + title);
    }

    @Then("the account {string} exists with role {string}")
    public void accountExists(String username, String role) {
        assertEquals(role, data.userByName(username).role.name());
    }

    @Then("the account {string} does not exist")
    public void accountMissing(String username) {
        try {
            data.userByName(username);
        } catch (AssertionError expected) {
            return;
        }
        throw new AssertionError("Account " + username + " exists but should not");
    }

    @Then("the account {string} is blocked")
    public void accountBlocked(String username) {
        UserAccount u = data.userByName(username);
        assertFalse(u.active, username + " should be blocked");
    }

    @Then("the account {string} is active")
    public void accountActive(String username) {
        UserAccount u = data.userByName(username);
        assertTrue(u.active, username + " should be active");
    }
}

package org.example.blockcalc.bdd;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Then;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** "Then" steps that look at the HTTP response and the HTML page. */
public class PageSteps {

    private static final Map<String, String> RESULT = Map.ofEntries(
            Map.entry("sand kg", "#res-sand-kg"),
            Map.entry("stone kg", "#res-stone-kg"),
            Map.entry("cement kg", "#res-cement-kg"),
            Map.entry("sand cost", "#res-sand-cost"),
            Map.entry("stone cost", "#res-stone-cost"),
            Map.entry("cement cost", "#res-cement-cost"),
            Map.entry("total", "#res-total"),
            Map.entry("currency", "#res-currency"),
            Map.entry("batches", "#res-batches"),
            Map.entry("blocks", "#res-blocks"),
            Map.entry("ws", "#res-ws"));

    private static final Map<String, String> DETAILS = Map.of(
            "title", "#calc-title",
            "project", "#meta-project",
            "author", "#meta-author",
            "created", "#meta-created",
            "updated by", "#meta-updated-by",
            "updated", "#meta-updated");

    private static final Map<String, String> BUTTONS = Map.of(
            "edit", "#btn-edit",
            "archive", "#btn-archive",
            "restore", "#btn-restore",
            "delete", "#btn-delete",
            "freeze", "#btn-freeze",
            "unfreeze", "#btn-unfreeze",
            "pdf", "#btn-pdf",
            "share", "#btn-share",
            "share all", "#btn-share-all",
            "print", "#btn-print");

    private static final Map<String, String> BADGES = Map.of(
            "frozen", "#badge-frozen",
            "archived", "#badge-archived");

    @Autowired
    private World world;

    // ---- HTTP ----------------------------------------------------------------------------

    @Then("the response status is {int}")
    public void status(int expected) {
        assertEquals(expected, world.status());
    }

    @Then("I am redirected to {string}")
    public void redirectedTo(String expected) {
        assertNotNull(world.redirect(), "Expected a redirect but status was " + world.status());
        assertTrue(world.redirect().equals(expected) || world.redirect().endsWith(expected),
                "Expected redirect to " + expected + " but was " + world.redirect());
    }

    @Then("I am redirected to the login page")
    public void redirectedToLogin() {
        redirectedTo("/login");
    }

    @Then("I am redirected to the calculation page")
    public void redirectedToCalculation() {
        assertNotNull(world.redirect(), "Expected a redirect but status was " + world.status());
        assertTrue(world.redirect().matches("/calculations/[0-9a-f]{24}"), "Unexpected redirect " + world.redirect());
    }

    @Then("I see an error message")
    public void errorMessage() throws Exception {
        assertEquals(400, world.status());
        assertFalse(world.document().select("#error-message").isEmpty(), "No #error-message on the page");
    }

    @Then("the PDF is shown inline")
    public void pdfInline() {
        assertEquals(200, world.status());
        assertTrue(world.header("Content-Disposition").startsWith("inline"), "Content-Disposition was " + world.header("Content-Disposition"));
    }

    @Then("the PDF is returned")
    public void pdf() {
        assertEquals(200, world.status());
        assertTrue(world.contentType().startsWith("application/pdf"), "Content type was " + world.contentType());
        byte[] bytes = world.bytes();
        assertTrue(bytes.length > 1000, "PDF is too small: " + bytes.length);
        assertEquals("%PDF", new String(bytes, 0, 4, StandardCharsets.US_ASCII));
    }

    // ---- page content --------------------------------------------------------------------

    @Then("the page contains {string}")
    public void pageContains(String text) throws Exception {
        assertTrue(world.document().text().contains(text), "Page does not contain: " + text);
    }

    @Then("the page does not contain {string}")
    public void pageDoesNotContain(String text) throws Exception {
        assertFalse(world.document().text().contains(text), "Page contains: " + text);
    }

    @Then("the result shows:")
    public void resultShows(DataTable table) throws Exception {
        Document doc = world.document();
        for (Map.Entry<String, String> e : table.asMap().entrySet()) {
            String selector = RESULT.get(e.getKey());
            assertNotNull(selector, "Unknown result field " + e.getKey());
            Element element = doc.selectFirst(selector);
            assertNotNull(element, "Result field not on the page: " + e.getKey());
            assertEquals(e.getValue(), element.text().trim(), e.getKey());
        }
    }

    /** Times are sent as UTC instants and shown as GMT; the browser converts them to the visitor's time zone. */
    @Then("the creation time is shown in GMT with a machine readable instant")
    public void creationTimeInGmt() throws Exception {
        Element time = world.document().selectFirst("time#meta-created");
        assertNotNull(time, "No <time id=meta-created> on the page");
        assertTrue(time.text().matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2} GMT"), "Unexpected text: " + time.text());
        assertTrue(time.attr("datetime").endsWith("Z"), "datetime is not a UTC instant: " + time.attr("datetime"));
    }

    @Then("the calculation page shows:")
    public void detailsShow(DataTable table) throws Exception {
        Document doc = world.document();
        for (Map.Entry<String, String> e : table.asMap().entrySet()) {
            String selector = DETAILS.get(e.getKey());
            assertNotNull(selector, "Unknown detail " + e.getKey());
            Element element = doc.selectFirst(selector);
            assertNotNull(element, "Detail not on the page: " + e.getKey());
            assertEquals(e.getValue(), element.text().trim(), e.getKey());
        }
    }

    @Then("I see the {string} button")
    public void seeButton(String name) throws Exception {
        assertFalse(world.document().select(selector(BUTTONS, name)).isEmpty(), "Missing button: " + name);
    }

    @Then("I do not see the {string} button")
    public void doNotSeeButton(String name) throws Exception {
        assertTrue(world.document().select(selector(BUTTONS, name)).isEmpty(), "Unexpected button: " + name);
    }

    @Then("I see the {string} badge")
    public void seeBadge(String name) throws Exception {
        assertFalse(world.document().select(selector(BADGES, name)).isEmpty(), "Missing badge: " + name);
    }

    @Then("I do not see the {string} badge")
    public void doNotSeeBadge(String name) throws Exception {
        assertTrue(world.document().select(selector(BADGES, name)).isEmpty(), "Unexpected badge: " + name);
    }

    // ---- lists ---------------------------------------------------------------------------

    @Then("the calculation list contains {string}")
    public void listContains(String title) throws Exception {
        assertNotNull(row(title), "Calculation list does not contain " + title);
    }

    @Then("the calculation list does not contain {string}")
    public void listDoesNotContain(String title) throws Exception {
        assertNull(row(title), "Calculation list contains " + title);
    }

    @Then("the calculation list shows {string} as archived")
    public void listShowsArchived(String title) throws Exception {
        Element row = row(title);
        assertNotNull(row, "Calculation list does not contain " + title);
        assertEquals("true", row.attr("data-archived"));
        assertTrue(row.hasClass("archived"));
    }

    @Then("the calculation list shows {string} as frozen")
    public void listShowsFrozen(String title) throws Exception {
        Element row = row(title);
        assertNotNull(row, "Calculation list does not contain " + title);
        assertEquals("true", row.attr("data-frozen"));
    }

    @Then("the project list contains {string}")
    public void projectListContains(String name) throws Exception {
        assertFalse(world.document().select("tr[data-project=\"" + name + "\"]").isEmpty(), "Missing project " + name);
    }

    @Then("the project list does not contain {string}")
    public void projectListDoesNotContain(String name) throws Exception {
        assertTrue(world.document().select("tr[data-project=\"" + name + "\"]").isEmpty(), "Unexpected project " + name);
    }

    @Then("the user list does not contain {string}")
    public void userListDoesNotContain(String username) throws Exception {
        assertTrue(world.document().select("tr[data-user=\"" + username + "\"]").isEmpty(), "Unexpected user " + username);
    }

    @Then("the user list shows {int} users")
    public void userListCount(int expected) throws Exception {
        assertEquals(expected, world.document().select("tr[data-user]").size());
    }

    @Then("the user list contains {string}")
    public void userListContains(String username) throws Exception {
        assertFalse(world.document().select("tr[data-user=\"" + username + "\"]").isEmpty(), "Missing user " + username);
    }

    @Then("the new calculation form offers project {string}")
    public void formOffers(String name) throws Exception {
        assertTrue(offeredProjects().contains(name), "Offered projects: " + offeredProjects());
    }

    @Then("the new calculation form compares {int} mixes")
    public void formComparesMixes(int expected) throws Exception {
        assertEquals(expected, world.document().select("#mix-compare .mix-row").size());
    }

    @Then("mix {int} is highlighted in the comparison")
    public void mixHighlighted(int number) throws Exception {
        assertEquals(1, world.document().select("#mix-compare .mix-row.active").size());
        assertFalse(world.document().select("#mix-compare .mix-row.active[data-mix-pick=\"" + number + "\"]").isEmpty(),
                "Mix " + number + " is not highlighted");
    }

    @Then("the new calculation form does not offer project {string}")
    public void formDoesNotOffer(String name) throws Exception {
        assertFalse(offeredProjects().contains(name), "Offered projects: " + offeredProjects());
    }

    @Then("calculation {string} is shared with {string} as {string} on the page")
    public void pageShowsGrant(String title, String username, String level) throws Exception {
        Element item = world.document().selectFirst("#grants li[data-grant-user=\"" + username + "\"]");
        assertNotNull(item, "No grant for " + username + " on the page");
        assertEquals(SetupSteps.level(level).name(), item.attr("data-level"));
    }

    private Element row(String title) throws Exception {
        Elements rows = world.document().select("tr[data-calc-title]");
        for (Element r : rows) {
            if (title.equals(r.attr("data-calc-title"))) {
                return r;
            }
        }
        return null;
    }

    private List<String> offeredProjects() throws Exception {
        return world.document().select("select[name=projectId] option").eachText();
    }

    private static String selector(Map<String, String> map, String name) {
        String selector = map.get(name);
        assertNotNull(selector, "Unknown name: " + name);
        return selector;
    }
}

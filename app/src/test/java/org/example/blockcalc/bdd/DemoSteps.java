package org.example.blockcalc.bdd;

import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.example.blockcalc.calc.CalculationRepository;
import org.example.blockcalc.common.Bootstrap;
import org.example.blockcalc.demo.DemoData;
import org.example.blockcalc.project.ProjectRepository;
import org.example.blockcalc.project.ProjectService;
import org.example.blockcalc.user.UserRepository;
import org.example.blockcalc.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The demo loader is only a bean when app.demo.enabled=true, so the tests create it by hand. */
public class DemoSteps {

    @Autowired
    private Bootstrap bootstrap;
    @Autowired
    private UserRepository users;
    @Autowired
    private UserService userService;
    @Autowired
    private ProjectRepository projects;
    @Autowired
    private ProjectService projectService;
    @Autowired
    private CalculationRepository calcs;
    @Autowired
    private TestData data;

    @When("the demo data is loaded")
    public void load() {
        new DemoData(bootstrap, users, userService, projects, projectService, calcs).run(null);
    }

    @Then("there is exactly one account {string}")
    public void exactlyOne(String username) {
        assertEquals(1, data.countUsers(username));
    }
}

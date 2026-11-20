package org.example.blockcalc.bdd;

import org.example.blockcalc.calc.Calculation;
import org.example.blockcalc.calc.CalculationRepository;
import org.example.blockcalc.calc.PriceSnapshot;
import org.example.blockcalc.common.Bootstrap;
import org.example.blockcalc.project.PriceEntry;
import org.example.blockcalc.project.PriceUnit;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.project.ProjectRepository;
import org.example.blockcalc.settings.SettingsRepository;
import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserAccount;
import org.example.blockcalc.user.UserRepository;
import org.example.blockcalc.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;

/** Arranges database state for the "Given" steps. */
@Component
public class TestData {

    @Autowired
    private UserRepository users;
    @Autowired
    private ProjectRepository projects;
    @Autowired
    private CalculationRepository calcs;
    @Autowired
    private UserService userService;
    @Autowired
    private Bootstrap bootstrap;
    @Autowired
    private SettingsRepository settingsRepo;

    /** Empty database plus the default admin and the "Other calculations" project. */
    public void reset() {
        calcs.deleteAll();
        projects.deleteAll();
        users.deleteAll();
        settingsRepo.deleteAll();
        bootstrap.init();
    }

    public UserAccount user(String username, Role role) {
        return userService.createUnchecked(username, World.PASSWORD, username, role, null);
    }

    public UserAccount saveUser(UserAccount u) {
        return users.save(u);
    }

    public UserAccount userByName(String username) {
        return users.findByUsername(username).orElseThrow(() -> new AssertionError("No user " + username));
    }

    public long countUsers(String username) {
        return users.findAll().stream().filter(u -> username.equals(u.username)).count();
    }

    public String usernameById(String id) {
        return users.findById(id).map(u -> u.username).orElse("?");
    }

    /** Defaults: RUB, WS 150, sand 1/kg, stone 2/kg, cement 10/kg. Easy numbers for the expected results. */
    public Project project(String name) {
        Project p = new Project();
        p.name = name;
        p.sandPrice = new PriceEntry(new BigDecimal("1"), PriceUnit.PER_KG);
        p.stonePrice = new PriceEntry(new BigDecimal("2"), PriceUnit.PER_KG);
        p.cementPrice = new PriceEntry(new BigDecimal("10"), PriceUnit.PER_KG);
        p.createdAt = Instant.now();
        return projects.save(p);
    }

    public Project projectByName(String name) {
        return projects.findByName(name).orElseThrow(() -> new AssertionError("No project " + name));
    }

    public String projectNameById(String id) {
        return projects.findById(id).map(p -> p.name).orElse("?");
    }

    public Project save(Project p) {
        return projects.save(p);
    }

    public Calculation calc(String title, String projectName, String authorUsername,
                            int full, int half, int batch, boolean roundUp) {
        Calculation c = new Calculation();
        c.projectId = projectByName(projectName).id;
        c.title = title;
        c.fullBlocks = full;
        c.halfBlocks = half;
        c.batch = batch;
        c.ws = new BigDecimal("150");
        c.roundUp = roundUp;
        c.createdBy = userByName(authorUsername).id;
        c.updatedBy = c.createdBy;
        c.createdAt = Instant.now();
        c.updatedAt = c.createdAt;
        return calcs.save(c);
    }

    public Calculation calcByTitle(String title) {
        return calcs.findAll().stream()
                .filter(c -> title.equals(c.title))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No calculation " + title));
    }

    public boolean calcExists(String title) {
        return calcs.findAll().stream().anyMatch(c -> title.equals(c.title));
    }

    public Calculation save(Calculation c) {
        return calcs.save(c);
    }

    public void freeze(Calculation c) {
        c.frozen = true;
        c.snapshot = new PriceSnapshot(projects.findById(c.projectId).orElseThrow().perKg());
        calcs.save(c);
    }
}

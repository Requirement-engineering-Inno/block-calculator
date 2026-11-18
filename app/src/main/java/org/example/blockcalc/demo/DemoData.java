package org.example.blockcalc.demo;

import org.example.blockcalc.calc.AccessLevel;
import org.example.blockcalc.calc.Calculation;
import org.example.blockcalc.calc.CalculationRepository;
import org.example.blockcalc.calc.Grant;
import org.example.blockcalc.calc.PriceSnapshot;
import org.example.blockcalc.common.Bootstrap;
import org.example.blockcalc.project.PriceEntry;
import org.example.blockcalc.project.PriceUnit;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.project.ProjectRepository;
import org.example.blockcalc.project.ProjectService;
import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserAccount;
import org.example.blockcalc.user.UserRepository;
import org.example.blockcalc.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Demo users, projects and calculations. Enabled with app.demo.enabled=true (docker-compose does it).
 * Safe to run on every start: nothing happens when the demo users already exist.
 */
@Component
@Order(2)
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DemoData implements ApplicationRunner {

    public static final String PASSWORD = "demo1234";

    public record DemoAccount(String username, String role, String name) {
    }

    /** Shown on the login page in demo mode. */
    public static final List<DemoAccount> ACCOUNTS = List.of(
            new DemoAccount("elena", "MANAGER", "Elena Petrova"),
            new DemoAccount("marcus", "MANAGER", "Marcus Weber"),
            new DemoAccount("ivan", "WORKER", "Ivan Sokolov"),
            new DemoAccount("maria", "WORKER", "Maria Rossi"),
            new DemoAccount("john", "WORKER", "John Carter"));

    private static final Logger log = LoggerFactory.getLogger(DemoData.class);

    private final Bootstrap bootstrap;
    private final UserRepository users;
    private final UserService userService;
    private final ProjectRepository projects;
    private final ProjectService projectService;
    private final CalculationRepository calcs;

    public DemoData(Bootstrap bootstrap, UserRepository users, UserService userService, ProjectRepository projects,
                    ProjectService projectService, CalculationRepository calcs) {
        this.bootstrap = bootstrap;
        this.users = users;
        this.userService = userService;
        this.projects = projects;
        this.projectService = projectService;
        this.calcs = calcs;
    }

    @Override
    public void run(ApplicationArguments args) {
        bootstrap.init();
        if (users.existsByUsername("elena")) {
            return;
        }
        UserAccount elena = account("elena", "Elena Petrova", Role.MANAGER);
        account("marcus", "Marcus Weber", Role.MANAGER);
        UserAccount ivan = account("ivan", "Ivan Sokolov", Role.WORKER);
        UserAccount maria = account("maria", "Maria Rossi", Role.WORKER);
        UserAccount john = account("john", "John Carter", Role.WORKER);

        // Prices of the customer's reference (Cape Town): sand and stone per m3, cement per 50 kg bag.
        Project cape = project("Cape Town housing", "ZAR", "262.88", "348", "85", ivan, maria);
        Project warehouse = project("Warehouse extension", "RUB", "1800", "2900", "520", maria, john);
        Project other = projectService.systemProject();

        Calculation wall = calc("North wall, section A", cape, ivan, 1800, 200, 4, true, 6);
        wall.grants.add(new Grant(maria.id, AccessLevel.READ_WRITE));
        calcs.save(wall);
        calc("Garage foundation", cape, maria, 650, 0, 3, true, 4);
        Calculation fence = calc("Fence posts", warehouse, john, 400, 120, 2, false, 12);
        fence.archived = true;
        calcs.save(fence);
        Calculation east = calc("Warehouse east wall", warehouse, elena, 5200, 800, 4, true, 2);
        east.frozen = true;
        east.snapshot = new PriceSnapshot(warehouse.perKg());
        calcs.save(east);
        calc("Sample batch check", other, ivan, 120, 0, 1, false, 1);
        log.info("Demo data created. Demo users have the password '{}'.", PASSWORD);
    }

    private UserAccount account(String username, String name, Role role) {
        return userService.createUnchecked(username, PASSWORD, name, role, null);
    }

    private Project project(String name, String currency, String sand, String stone, String cement, UserAccount... workers) {
        Project p = new Project();
        p.name = name;
        p.currency = currency;
        p.sandPrice = new PriceEntry(new BigDecimal(sand), PriceUnit.PER_M3);
        p.stonePrice = new PriceEntry(new BigDecimal(stone), PriceUnit.PER_M3);
        p.cementPrice = new PriceEntry(new BigDecimal(cement), PriceUnit.PER_BAG);
        for (UserAccount w : workers) {
            p.workerIds.add(w.id);
        }
        p.createdAt = Instant.now().minus(30, ChronoUnit.DAYS);
        return projects.save(p);
    }

    private Calculation calc(String title, Project project, UserAccount author, int full, int half, int batch,
                             boolean roundUp, int daysAgo) {
        Calculation c = new Calculation();
        c.projectId = project.id;
        c.title = title;
        c.fullBlocks = full;
        c.halfBlocks = half;
        c.batch = batch;
        c.ws = new BigDecimal("150");
        c.roundUp = roundUp;
        c.createdBy = author.id;
        c.updatedBy = author.id;
        c.createdAt = Instant.now().minus(daysAgo, ChronoUnit.DAYS);
        c.updatedAt = c.createdAt.plus(3, ChronoUnit.HOURS);
        return calcs.save(c);
    }
}

package org.example.blockcalc.common;

import org.example.blockcalc.project.ProjectService;
import org.example.blockcalc.settings.SettingsService;
import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserRepository;
import org.example.blockcalc.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Makes sure the default admin and the "Other calculations" project exist. */
@Component
@Order(1)
public class Bootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);

    private final UserRepository users;
    private final UserService userService;
    private final ProjectService projects;
    private final SettingsService settings;
    private final String adminUsername;
    private final String adminEmail;
    private final String adminPassword;

    public Bootstrap(UserRepository users, UserService userService, ProjectService projects, SettingsService settings,
                     @Value("${app.admin.username:admin}") String adminUsername,
                     @Value("${app.admin.email:admin@blockcalc.local}") String adminEmail,
                     @Value("${app.admin.password:admin}") String adminPassword) {
        this.users = users;
        this.userService = userService;
        this.projects = projects;
        this.settings = settings;
        this.adminUsername = adminUsername;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        init();
    }

    public void init() {
        if (!users.existsByRole(Role.ADMIN)) {
            userService.createUnchecked(adminUsername, adminPassword, "Administrator", Role.ADMIN, null, adminEmail);
            log.warn("Default admin '{}' created. Change its password after the first login.", adminUsername);
        }
        settings.get();
        projects.ensureSystemProject();
    }
}

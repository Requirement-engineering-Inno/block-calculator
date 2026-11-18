package org.example.blockcalc.user;

import org.example.blockcalc.calc.AccessLevel;
import org.example.blockcalc.calc.Calculation;
import org.example.blockcalc.calc.CalculationRepository;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.project.ProjectRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Builds the profile page of an employee (for managers) and "my account" (for everybody). */
@Service
public class ProfileService {

    private static final int RECENT = 8;

    public record ProjectRef(String id, String name, boolean system) {
    }

    public record CalcRef(String id, String title, String project, boolean projectSystem, Instant updatedAt,
                          boolean archived, boolean frozen) {
    }

    public record ProfileView(String id, String username, String displayName, Role role, boolean active,
                              Instant createdAt, String createdBy, boolean self,
                              List<ProjectRef> projects, long authored, long archived, long frozen, long sharedWithMe,
                              List<CalcRef> recent) {
    }

    private final UserRepository users;
    private final ProjectRepository projects;
    private final CalculationRepository calcs;

    public ProfileService(UserRepository users, ProjectRepository projects, CalculationRepository calcs) {
        this.users = users;
        this.projects = projects;
        this.calcs = calcs;
    }

    /** Admin sees everybody, a manager sees workers (and himself), a worker sees only himself. */
    public ProfileView of(CurrentUser viewer, String userId) {
        UserAccount u = users.findById(userId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        boolean self = u.id.equals(viewer.getId());
        boolean allowed = self || viewer.isAdmin() || (viewer.isStaff() && u.role == Role.WORKER);
        if (!allowed) {
            throw new AccessDeniedException("No access to this profile");
        }

        Map<String, Project> projectMap = new HashMap<>();
        projects.findAll().forEach(p -> projectMap.put(p.id, p));
        List<ProjectRef> refs = projectMap.values().stream()
                .filter(p -> u.role == Role.WORKER ? (p.system || p.workerIds.contains(u.id)) : (!p.system && u.id.equals(p.createdBy)))
                .sorted(Comparator.comparing((Project p) -> !p.system).thenComparing(p -> p.name.toLowerCase()))
                .map(p -> new ProjectRef(p.id, p.name, p.system))
                .toList();

        List<Calculation> authored = calcs.findAll().stream().filter(c -> u.id.equals(c.createdBy)).toList();
        long shared = calcs.findAll().stream()
                .filter(c -> c.grants.stream().anyMatch(g -> u.id.equals(g.userId) && g.level != null))
                .count();
        List<CalcRef> recent = authored.stream()
                .sorted(Comparator.comparing((Calculation c) -> c.updatedAt).reversed())
                .limit(RECENT)
                .map(c -> {
                    Project p = projectMap.get(c.projectId);
                    return new CalcRef(c.id, c.title, p == null ? "?" : p.name, p != null && p.system, c.updatedAt, c.archived, c.frozen);
                })
                .toList();

        String creator = u.createdBy == null ? null : users.findById(u.createdBy).map(x -> x.displayName).orElse(null);
        return new ProfileView(u.id, u.username, u.displayName, u.role, u.active, u.createdAt, creator, self, refs,
                authored.size(), authored.stream().filter(c -> c.archived).count(),
                authored.stream().filter(c -> c.frozen).count(), shared, recent);
    }
}

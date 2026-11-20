package org.example.blockcalc.project;

import org.example.blockcalc.common.BusinessException;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.common.Limits;
import org.example.blockcalc.settings.SettingsService;
import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectService {

    private final ProjectRepository repo;
    private final UserRepository users;
    private final SettingsService settings;

    public ProjectService(ProjectRepository repo, UserRepository users, SettingsService settings) {
        this.repo = repo;
        this.users = users;
        this.settings = settings;
    }

    public Project ensureSystemProject() {
        return repo.findBySystemTrue().orElseGet(() -> {
            Project p = new Project();
            p.name = Project.OTHER_NAME;
            p.system = true;
            p.createdAt = Instant.now();
            return repo.save(p);
        });
    }

    public Project systemProject() {
        return ensureSystemProject();
    }

    public Project get(String id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public Map<String, Project> byIds(Collection<String> ids) {
        Map<String, Project> map = new HashMap<>();
        repo.findAllById(ids).forEach(p -> map.put(p.id, p));
        return map;
    }

    /** Staff see all projects; a worker sees the projects he is assigned to and the system project. */
    public List<Project> visibleTo(CurrentUser me) {
        return repo.findAll().stream()
                .filter(p -> canUse(me, p))
                .sorted(Comparator.comparing((Project p) -> !p.system).thenComparing(p -> p.name.toLowerCase()))
                .toList();
    }

    public boolean canUse(CurrentUser me, Project p) {
        return me.isStaff() || p.system || p.workerIds.contains(me.getId());
    }

    public Project create(CurrentUser actor, ProjectInput in) {
        requireStaff(actor);
        Project p = new Project();
        p.createdBy = actor.getId();
        p.createdAt = Instant.now();
        apply(p, in);
        return repo.save(p);
    }

    public Project update(CurrentUser actor, String id, ProjectInput in) {
        requireStaff(actor);
        Project p = get(id);
        apply(p, in);
        return repo.save(p);
    }

    /** Switches the project currency and converts every price with a manually entered rate. */
    public Project changeCurrency(CurrentUser actor, String id, String newCurrency, BigDecimal rate) {
        requireStaff(actor);
        if (newCurrency == null || !settings.hasCurrency(newCurrency)) {
            throw new BusinessException("error.project.currency.unknown");
        }
        if (rate == null || rate.signum() <= 0
                || rate.compareTo(BigDecimal.valueOf(Limits.MAX_RATE)) > 0) {
            throw new BusinessException("error.project.currency", Limits.MAX_RATE);
        }
        Project p = get(id);
        BigDecimal max = BigDecimal.valueOf(Limits.MAX_PRICE);
        if (convert(p.sandPrice.amount, rate).compareTo(max) > 0 || convert(p.stonePrice.amount, rate).compareTo(max) > 0
                || convert(p.cementPrice.amount, rate).compareTo(max) > 0) {
            throw new BusinessException("error.project.limit", Limits.MAX_PRICE);
        }
        p.currency = newCurrency.trim().toUpperCase();
        p.sandPrice.amount = convert(p.sandPrice.amount, rate);
        p.stonePrice.amount = convert(p.stonePrice.amount, rate);
        p.cementPrice.amount = convert(p.cementPrice.amount, rate);
        return repo.save(p);
    }

    private static BigDecimal convert(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(4, RoundingMode.HALF_UP);
    }

    private void apply(Project p, ProjectInput in) {
        if (!p.system) {
            if (in.name() == null || in.name().isBlank() || Project.OTHER_NAME.equalsIgnoreCase(in.name().trim())) {
                throw new BusinessException("error.project.invalid");
            }
            p.name = in.name().trim();
        }
        if (in.currency() == null || !settings.hasCurrency(in.currency())) {
            throw new BusinessException("error.project.currency.unknown");
        }
        if (missingOrNegative(in.sandAmount()) || missingOrNegative(in.stoneAmount()) || missingOrNegative(in.cementAmount())
                || !PriceUnit.BULK.contains(in.sandUnit())
                || !PriceUnit.BULK.contains(in.stoneUnit())
                || !PriceUnit.CEMENT.contains(in.cementUnit())) {
            throw new BusinessException("error.project.invalid");
        }
        BigDecimal maxPrice = BigDecimal.valueOf(Limits.MAX_PRICE);
        if (in.sandAmount().compareTo(maxPrice) > 0 || in.stoneAmount().compareTo(maxPrice) > 0
                || in.cementAmount().compareTo(maxPrice) > 0) {
            throw new BusinessException("error.project.limit", Limits.MAX_PRICE);
        }
        p.currency = in.currency().trim().toUpperCase();
        p.sandPrice = new PriceEntry(in.sandAmount(), in.sandUnit());
        p.stonePrice = new PriceEntry(in.stoneAmount(), in.stoneUnit());
        p.cementPrice = new PriceEntry(in.cementAmount(), in.cementUnit());
        if (!p.system) {
            Set<String> ids = in.workerIds() == null ? Set.of() : in.workerIds();
            Set<String> valid = new HashSet<>();
            users.findAllById(ids).forEach(u -> {
                if (u.role == Role.WORKER) {
                    valid.add(u.id);
                }
            });
            p.workerIds = valid;
        }
    }

    /** True when the value is missing or negative. */
    private static boolean missingOrNegative(BigDecimal v) {
        return v == null || v.signum() < 0;
    }

    private static void requireStaff(CurrentUser actor) {
        if (!actor.isStaff()) {
            throw new AccessDeniedException("Staff only");
        }
    }
}

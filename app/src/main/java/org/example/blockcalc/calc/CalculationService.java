package org.example.blockcalc.calc;

import org.example.blockcalc.calc.Views.CalcDetail;
import org.example.blockcalc.calc.Views.CalcForm;
import org.example.blockcalc.calc.Views.CalcRow;
import org.example.blockcalc.calc.Views.GrantRow;
import org.example.blockcalc.common.BusinessException;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.common.Limits;
import org.example.blockcalc.common.Times;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.project.ProjectService;
import org.example.blockcalc.user.Role;
import org.example.blockcalc.user.UserAccount;
import org.example.blockcalc.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class CalculationService {

    private final CalculationRepository repo;
    private final ProjectService projects;
    private final UserService users;

    public CalculationService(CalculationRepository repo, ProjectService projects, UserService users) {
        this.repo = repo;
        this.projects = projects;
        this.users = users;
    }

    // ---- create / update ----------------------------------------------------------------

    public Calculation create(CurrentUser me, CalcInput in) {
        Project p = resolveProject(me, in.projectId());
        Calculation c = new Calculation();
        c.projectId = p.id;
        c.createdBy = me.getId();
        c.createdAt = Instant.now();
        applyInput(c, in, p);
        touch(c, me, c.createdAt);
        return repo.save(c);
    }

    public Calculation update(CurrentUser me, String id, CalcInput in) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canWrite(me, c)) {
            throw new AccessDeniedException("Read-only access");
        }
        if (c.frozen) {
            throw new BusinessException("error.calc.frozen");
        }
        applyInput(c, in, projects.get(c.projectId));
        touch(c, me, Instant.now());
        return repo.save(c);
    }

    /** Calculates without saving (live preview in the form). */
    public CalcResult preview(CurrentUser me, CalcInput in) {
        Project p = resolveProject(me, in.projectId());
        Calculation c = new Calculation();
        applyInput(c, in, p);
        return CalculationEngine.calculate(c.fullBlocks, c.halfBlocks, Mix.of(c.batch), c.ws, c.roundUp, p.perKg());
    }

    private Project resolveProject(CurrentUser me, String projectId) {
        Project p = projectId == null || projectId.isBlank() ? projects.systemProject() : projects.get(projectId);
        if (!projects.canUse(me, p)) {
            throw new AccessDeniedException("Project is not available");
        }
        return p;
    }

    private void applyInput(Calculation c, CalcInput in, Project p) {
        int full = in.fullBlocks() == null ? 0 : in.fullBlocks();
        int half = in.halfBlocks() == null ? 0 : in.halfBlocks();
        if (full < 0 || half < 0 || full + half == 0 || full > Limits.MAX_BLOCKS || half > Limits.MAX_BLOCKS) {
            throw new BusinessException("error.calc.blocks", Limits.MAX_BLOCKS);
        }
        Mix mix = Mix.of(in.batch() == null ? 0 : in.batch());
        BigDecimal ws = in.ws() == null ? p.wsDefault : in.ws();
        if (ws.signum() <= 0 || ws.compareTo(BigDecimal.valueOf(Limits.MAX_WS)) > 0) {
            throw new BusinessException("error.calc.ws", Limits.MAX_WS);
        }
        c.fullBlocks = full;
        c.halfBlocks = half;
        c.batch = mix.number;
        c.ws = ws;
        c.roundUp = Boolean.TRUE.equals(in.roundUp());
        c.title = in.title() == null || in.title().isBlank()
                ? "Calculation " + Times.formatGmt(Instant.now())
                : in.title().trim();
    }

    private static void touch(Calculation c, CurrentUser me, Instant at) {
        c.updatedBy = me.getId();
        c.updatedAt = at;
    }

    // ---- state changes ------------------------------------------------------------------

    public void setArchived(CurrentUser me, String id, boolean archived) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canWrite(me, c)) {
            throw new AccessDeniedException("Read-only access");
        }
        c.archived = archived;
        touch(c, me, Instant.now());
        repo.save(c);
    }

    public void delete(CurrentUser me, String id) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canDelete(me, c)) {
            throw new AccessDeniedException("Only managers delete calculations");
        }
        repo.delete(c);
    }

    /** Freezing stores the current project prices on the calculation; unfreezing drops them. */
    public void setFrozen(CurrentUser me, String id, boolean frozen) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canFreeze(me, c)) {
            throw new AccessDeniedException("Only managers freeze calculations");
        }
        c.frozen = frozen;
        c.snapshot = frozen ? new PriceSnapshot(projects.get(c.projectId).perKg()) : null;
        touch(c, me, Instant.now());
        repo.save(c);
    }

    // ---- sharing ------------------------------------------------------------------------

    public void share(CurrentUser me, String id, String userId, AccessLevel level) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canShare(me, c)) {
            throw new AccessDeniedException("Cannot share this calculation");
        }
        UserAccount target = users.get(userId);
        if (target.role != Role.WORKER || !target.active) {
            throw new BusinessException("error.share.target");
        }
        grant(c, target.id, level);
        repo.save(c);
    }

    /** Manager button: give every worker assigned to the calculation's project the same access. */
    public int shareWithProjectWorkers(CurrentUser me, String id, AccessLevel level) {
        Calculation c = load(me, id);
        if (!me.isStaff()) {
            throw new AccessDeniedException("Managers only");
        }
        Project p = projects.get(c.projectId);
        int count = 0;
        for (String workerId : p.workerIds) {
            if (grant(c, workerId, level)) {
                count++;
            }
        }
        repo.save(c);
        return count;
    }

    public void unshare(CurrentUser me, String id, String userId) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canShare(me, c)) {
            throw new AccessDeniedException("Cannot change sharing");
        }
        c.grants.removeIf(g -> userId.equals(g.userId));
        repo.save(c);
    }

    /** @return false when nothing was granted (the author already has full access) */
    private static boolean grant(Calculation c, String userId, AccessLevel level) {
        if (userId.equals(c.createdBy)) {
            return false;
        }
        for (Grant g : c.grants) {
            if (userId.equals(g.userId)) {
                g.level = level;
                return true;
            }
        }
        c.grants.add(new Grant(userId, level));
        return true;
    }

    // ---- reading ------------------------------------------------------------------------

    private Calculation load(CurrentUser me, String id) {
        Calculation c = repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!AccessPolicy.canView(me, c)) {
            throw new AccessDeniedException("No access to this calculation");
        }
        return c;
    }

    public CalcResult resultOf(Calculation c, Project p) {
        PerKgPrices prices = c.frozen && c.snapshot != null ? c.snapshot.toPrices() : p.perKg();
        return CalculationEngine.calculate(c.fullBlocks, c.halfBlocks, Mix.of(c.batch), c.ws, c.roundUp, prices);
    }

    public List<CalcRow> rows(CurrentUser me, String projectId) {
        List<Calculation> list = (projectId == null || projectId.isBlank() ? repo.findAll() : repo.findByProjectId(projectId))
                .stream()
                .filter(c -> AccessPolicy.canView(me, c))
                .sorted(Comparator.comparing((Calculation c) -> c.updatedAt).reversed())
                .toList();
        Set<String> projectIds = new HashSet<>();
        Set<String> userIds = new HashSet<>();
        for (Calculation c : list) {
            projectIds.add(c.projectId);
            userIds.add(c.createdBy);
        }
        Map<String, Project> projectMap = projects.byIds(projectIds);
        Map<String, UserAccount> userMap = users.byIds(userIds);
        List<CalcRow> rows = new ArrayList<>();
        for (Calculation c : list) {
            Project p = projectMap.get(c.projectId);
            CalcResult r = resultOf(c, p);
            rows.add(new CalcRow(c.id, c.title, p.name, p.system, name(userMap, c.createdBy),
                    c.updatedAt, c.archived, c.frozen, r.total().toPlainString(), r.currency()));
        }
        return rows;
    }

    public CalcDetail detail(CurrentUser me, String id) {
        Calculation c = load(me, id);
        Project p = projects.get(c.projectId);
        Set<String> userIds = new HashSet<>();
        userIds.add(c.createdBy);
        userIds.add(c.updatedBy);
        c.grants.forEach(g -> userIds.add(g.userId));
        Map<String, UserAccount> names = users.byIds(userIds);

        List<GrantRow> grants = c.grants.stream()
                .map(g -> new GrantRow(g.userId, username(names, g.userId), name(names, g.userId), g.level))
                .toList();
        boolean canShare = AccessPolicy.canShare(me, c);

        return new CalcDetail(c.id, c.title, p.id, p.name, p.system, c.frozen, c.archived,
                c.fullBlocks, c.halfBlocks, c.batch, c.ws, c.roundUp,
                name(names, c.createdBy), c.createdAt,
                name(names, c.updatedBy), c.updatedAt,
                resultOf(c, p),
                AccessPolicy.canEdit(me, c), AccessPolicy.canWrite(me, c), canShare,
                AccessPolicy.canDelete(me, c), AccessPolicy.canFreeze(me, c),
                me.isStaff() && !p.system && !p.workerIds.isEmpty(),
                grants);
    }

    public CalcForm formFor(CurrentUser me, String id) {
        Calculation c = load(me, id);
        if (!AccessPolicy.canWrite(me, c)) {
            throw new AccessDeniedException("Read-only access");
        }
        if (c.frozen) {
            throw new BusinessException("error.calc.frozen");
        }
        return new CalcForm(c.id, c.projectId, c.title, c.fullBlocks, c.halfBlocks, c.batch, c.ws, c.roundUp);
    }

    public CalcForm emptyForm(String projectId) {
        return new CalcForm(null, projectId, "", null, null, 4, null, false);
    }

    private static String name(Map<String, UserAccount> map, String id) {
        UserAccount u = map.get(id);
        return u == null ? "?" : u.displayName;
    }

    private static String username(Map<String, UserAccount> map, String id) {
        UserAccount u = map.get(id);
        return u == null ? "?" : u.username;
    }
}

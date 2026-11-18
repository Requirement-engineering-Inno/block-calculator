package org.example.blockcalc.user;

import org.example.blockcalc.common.BusinessException;
import org.example.blockcalc.common.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserService implements UserDetailsService {

    private static final int MIN_PASSWORD = 6;

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return repo.findByUsername(username)
                .map(CurrentUser::new)
                .orElseThrow(() -> new UsernameNotFoundException(username));
    }

    public UserAccount get(String id) {
        return repo.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /** Creates an account without permission checks (bootstrap and tests). */
    public UserAccount createUnchecked(String username, String password, String displayName, Role role, String createdBy) {
        UserAccount u = new UserAccount();
        u.username = username;
        u.passwordHash = encoder.encode(password);
        u.displayName = displayName == null || displayName.isBlank() ? username : displayName.trim();
        u.role = role;
        u.active = true;
        u.createdBy = createdBy;
        u.createdAt = Instant.now();
        return repo.save(u);
    }

    /** Admin creates managers and workers; a manager creates workers. */
    public UserAccount create(CurrentUser actor, String username, String password, String displayName, Role role) {
        boolean allowed = (actor.getRole() == Role.ADMIN && (role == Role.MANAGER || role == Role.WORKER))
                || (actor.getRole() == Role.MANAGER && role == Role.WORKER);
        if (!allowed) {
            throw new AccessDeniedException("Cannot create role " + role);
        }
        String name = username == null ? "" : username.trim();
        if (!name.matches("[A-Za-z0-9._-]{3,40}") || password == null || password.length() < MIN_PASSWORD) {
            throw new BusinessException("error.user.invalid", MIN_PASSWORD);
        }
        if (repo.existsByUsername(name)) {
            throw new BusinessException("error.user.exists");
        }
        return createUnchecked(name, password, displayName, role, actor.getId());
    }

    public void setActive(CurrentUser actor, String userId, boolean active) {
        UserAccount target = get(userId);
        boolean allowed = (actor.getRole() == Role.ADMIN && target.role != Role.ADMIN)
                || (actor.getRole() == Role.MANAGER && target.role == Role.WORKER);
        if (!allowed || target.id.equals(actor.getId())) {
            throw new AccessDeniedException("Cannot change status of this user");
        }
        target.active = active;
        repo.save(target);
    }

    public void changePassword(CurrentUser me, String current, String next) {
        UserAccount u = get(me.getId());
        if (current == null || !encoder.matches(current, u.passwordHash)) {
            throw new BusinessException("error.password.wrong");
        }
        if (next == null || next.length() < MIN_PASSWORD) {
            throw new BusinessException("error.user.invalid", MIN_PASSWORD);
        }
        u.passwordHash = encoder.encode(next);
        repo.save(u);
    }

    /** Admin manages everyone, a manager manages workers. */
    public List<UserAccount> manageable(CurrentUser actor) {
        List<UserAccount> all = actor.isAdmin() ? repo.findAll() : repo.findByRole(Role.WORKER);
        all.sort(Comparator.comparing((UserAccount u) -> u.role).thenComparing(u -> u.username));
        return all;
    }

    public record UserPage(List<UserAccount> items, int page, int pages, long total) {
    }

    /** Search and filter for the user list. Manager: workers only, admin: everybody. */
    public UserPage search(CurrentUser actor, String q, Role role, Boolean active, int page, int size) {
        String needle = q == null ? "" : q.trim().toLowerCase();
        List<UserAccount> all = manageable(actor).stream()
                .filter(u -> role == null || u.role == role)
                .filter(u -> active == null || u.active == active)
                .filter(u -> matches(u, needle))
                .toList();
        int pages = Math.max(1, (all.size() + size - 1) / size);
        int current = Math.min(Math.max(page, 1), pages);
        List<UserAccount> items = all.subList((current - 1) * size, Math.min(all.size(), current * size));
        return new UserPage(items, current, pages, all.size());
    }

    /** Active workers whose name or login contains the text (for the pickers). */
    public List<UserAccount> searchWorkers(String q, int limit) {
        String needle = q == null ? "" : q.trim().toLowerCase();
        return repo.findByRole(Role.WORKER).stream()
                .filter(u -> u.active && matches(u, needle))
                .sorted(Comparator.comparing((UserAccount u) -> u.displayName.toLowerCase()))
                .limit(limit)
                .toList();
    }

    private static boolean matches(UserAccount u, String needle) {
        return needle.isEmpty() || u.username.toLowerCase().contains(needle) || u.displayName.toLowerCase().contains(needle);
    }

    public List<UserAccount> activeWorkers() {
        return repo.findByRole(Role.WORKER).stream()
                .filter(u -> u.active)
                .sorted(Comparator.comparing((UserAccount u) -> u.username))
                .toList();
    }

    public Map<String, UserAccount> byIds(Collection<String> ids) {
        Map<String, UserAccount> map = new HashMap<>();
        repo.findAllById(ids).forEach(u -> map.put(u.id, u));
        return map;
    }
}

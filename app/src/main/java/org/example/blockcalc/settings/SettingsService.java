package org.example.blockcalc.settings;

import org.example.blockcalc.common.BusinessException;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.common.Limits;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.project.ProjectRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class SettingsService {

    private final SettingsRepository repo;
    private final ProjectRepository projects;

    public SettingsService(SettingsRepository repo, ProjectRepository projects) {
        this.repo = repo;
        this.projects = projects;
    }

    /** Loads the settings, creating them with RUB, USD and EUR on first use. */
    public AppSettings get() {
        return repo.findById(AppSettings.ID).orElseGet(() -> {
            AppSettings s = new AppSettings();
            s.currencies = new ArrayList<>(List.of("EUR", "RUB", "USD"));
            return repo.save(s);
        });
    }

    public BigDecimal wheelbarrowKg() {
        return get().wheelbarrowKg;
    }

    public List<String> currencies() {
        return get().currencies.stream().sorted().toList();
    }

    public boolean hasCurrency(String code) {
        return code != null && get().currencies.contains(code.trim().toUpperCase(Locale.ROOT));
    }

    /** Currency code to number of projects that use it. */
    public Map<String, Long> usage() {
        List<Project> all = projects.findAll();
        Map<String, Long> map = new LinkedHashMap<>();
        for (String c : currencies()) {
            map.put(c, all.stream().filter(p -> c.equals(p.currency)).count());
        }
        return map;
    }

    public void setWheelbarrow(CurrentUser actor, BigDecimal kg) {
        requireStaff(actor);
        if (kg == null || kg.signum() <= 0 || kg.compareTo(BigDecimal.valueOf(Limits.MAX_WS)) > 0) {
            throw new BusinessException("error.calc.ws", Limits.MAX_WS);
        }
        AppSettings s = get();
        s.wheelbarrowKg = kg;
        repo.save(s);
    }

    public void addCurrency(CurrentUser actor, String code) {
        requireStaff(actor);
        addCurrencyUnchecked(code);
    }

    /** Also used by the demo data and the tests. */
    public void addCurrencyUnchecked(String code) {
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z]{3}")) {
            throw new BusinessException("error.currency.invalid");
        }
        AppSettings s = get();
        if (s.currencies.contains(normalized)) {
            throw new BusinessException("error.currency.exists");
        }
        s.currencies.add(normalized);
        repo.save(s);
    }

    public void setWheelbarrowUnchecked(BigDecimal kg) {
        AppSettings s = get();
        s.wheelbarrowKg = kg;
        repo.save(s);
    }

    public void removeCurrency(CurrentUser actor, String code) {
        requireStaff(actor);
        String normalized = code == null ? "" : code.trim().toUpperCase(Locale.ROOT);
        AppSettings s = get();
        if (!s.currencies.contains(normalized)) {
            throw new BusinessException("error.currency.unknown");
        }
        if (s.currencies.size() == 1) {
            throw new BusinessException("error.currency.last");
        }
        if (projects.findAll().stream().anyMatch(p -> normalized.equals(p.currency))) {
            throw new BusinessException("error.currency.inUse");
        }
        s.currencies.remove(normalized);
        repo.save(s);
    }

    private static void requireStaff(CurrentUser actor) {
        if (!actor.isStaff()) {
            throw new AccessDeniedException("Staff only");
        }
    }
}

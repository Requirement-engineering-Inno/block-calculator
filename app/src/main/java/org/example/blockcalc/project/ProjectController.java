package org.example.blockcalc.project;

import org.example.blockcalc.calc.CalculationService;
import org.example.blockcalc.calc.Views;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.common.Materials;
import org.example.blockcalc.settings.SettingsService;
import org.example.blockcalc.user.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

/**
 * Production areas. Everybody opens the list and the page of an area they can use; the settings, creation and
 * currency change are limited to admin and manager in SecurityConfig.
 */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projects;
    private final UserService users;
    private final SettingsService settings;

    private final CalculationService calcs;

    public ProjectController(ProjectService projects, UserService users, SettingsService settings, CalculationService calcs) {
        this.projects = projects;
        this.users = users;
        this.settings = settings;
        this.calcs = calcs;
    }

    /** Home page. Opens the welcome tour by itself until the user has finished or skipped it once. */
    @GetMapping
    public String list(@AuthenticationPrincipal CurrentUser me, Model model) {
        List<Project> visible = projects.visibleTo(me);
        model.addAttribute("projects", visible);
        model.addAttribute("currencies", visible.stream().map(p -> p.currency).distinct().sorted().toList());
        model.addAttribute("counts", calcs.countsByArea(me));
        model.addAttribute("startTour", !users.isOnboardingDone(me.getId()));
        return "project/list";
    }

    /** One production area with all its projects (calculations). */
    @GetMapping("/{id}")
    public String view(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, Model model) {
        Project p = projects.get(id);
        if (!projects.canUse(me, p)) {
            throw new AccessDeniedException("Production area is not available");
        }
        List<Views.CalcRow> rows = calcs.rows(me, id);
        model.addAttribute("p", p);
        model.addAttribute("rows", rows);
        model.addAttribute("countActive", rows.stream().filter(r -> !r.archived()).count());
        model.addAttribute("countArchived", rows.stream().filter(Views.CalcRow::archived).count());
        model.addAttribute("countFrozen", rows.stream().filter(Views.CalcRow::frozen).count());
        model.addAttribute("teamSize", p.workerIds.size());
        return "project/view";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        fillForm(model, new Project(), true);
        return "project/form";
    }

    @PostMapping
    public String create(@ModelAttribute ProjectInput in, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        projects.create(me, in);
        ra.addFlashAttribute("flash", "flash.project.saved");
        return "redirect:/projects";
    }

    @GetMapping("/{id}/settings")
    public String edit(@PathVariable String id, Model model) {
        fillForm(model, projects.get(id), false);
        return "project/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id, @ModelAttribute ProjectInput in, @AuthenticationPrincipal CurrentUser me,
                         RedirectAttributes ra) {
        projects.update(me, id, in);
        ra.addFlashAttribute("flash", "flash.project.saved");
        return "redirect:/projects/" + id;
    }

    @PostMapping("/{id}/currency")
    public String changeCurrency(@PathVariable String id, @RequestParam String currency, @RequestParam BigDecimal rate,
                                 @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        projects.changeCurrency(me, id, currency, rate);
        ra.addFlashAttribute("flash", "flash.project.currency");
        return "redirect:/projects/" + id + "/settings";
    }

    private void fillForm(Model model, Project p, boolean isNew) {
        model.addAttribute("p", p);
        model.addAttribute("isNew", isNew);
        model.addAttribute("selectedWorkers", users.byIds(p.workerIds).values().stream()
                .sorted(java.util.Comparator.comparing(u -> u.displayName.toLowerCase()))
                .toList());
        model.addAttribute("bulkUnits", PriceUnit.BULK);
        model.addAttribute("cementUnits", PriceUnit.CEMENT);
        model.addAttribute("currencies", settings.currencies());
        model.addAttribute("sandDensity", Materials.SAND_DENSITY.toPlainString());
        model.addAttribute("stoneDensity", Materials.STONE_DENSITY.toPlainString());
    }
}

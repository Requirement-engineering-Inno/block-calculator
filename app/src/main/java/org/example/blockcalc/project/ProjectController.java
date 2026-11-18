package org.example.blockcalc.project;

import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.user.UserService;
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

/** Project settings. Access is limited to admin and manager in SecurityConfig. */
@Controller
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projects;
    private final UserService users;

    public ProjectController(ProjectService projects, UserService users) {
        this.projects = projects;
        this.users = users;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal CurrentUser me, Model model) {
        List<Project> visible = projects.visibleTo(me);
        model.addAttribute("projects", visible);
        model.addAttribute("currencies", visible.stream().map(p -> p.currency).distinct().sorted().toList());
        return "project/list";
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

    @GetMapping("/{id}")
    public String edit(@PathVariable String id, Model model) {
        fillForm(model, projects.get(id), false);
        return "project/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id, @ModelAttribute ProjectInput in, @AuthenticationPrincipal CurrentUser me,
                         RedirectAttributes ra) {
        projects.update(me, id, in);
        ra.addFlashAttribute("flash", "flash.project.saved");
        return "redirect:/projects";
    }

    @PostMapping("/{id}/currency")
    public String changeCurrency(@PathVariable String id, @RequestParam String currency, @RequestParam BigDecimal rate,
                                 @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        projects.changeCurrency(me, id, currency, rate);
        ra.addFlashAttribute("flash", "flash.project.currency");
        return "redirect:/projects/" + id;
    }

    private void fillForm(Model model, Project p, boolean isNew) {
        model.addAttribute("p", p);
        model.addAttribute("isNew", isNew);
        model.addAttribute("selectedWorkers", users.byIds(p.workerIds).values().stream()
                .sorted(java.util.Comparator.comparing(u -> u.displayName.toLowerCase()))
                .toList());
        model.addAttribute("areaUnits", List.of(PriceUnit.PER_KG, PriceUnit.PER_M3));
        model.addAttribute("cementUnits", List.of(PriceUnit.PER_KG, PriceUnit.PER_BAG));
    }
}

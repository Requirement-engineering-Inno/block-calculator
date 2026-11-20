package org.example.blockcalc.calc;

import org.example.blockcalc.calc.Views.CalcDetail;
import org.example.blockcalc.calc.Views.CalcForm;
import org.example.blockcalc.common.BusinessException;
import org.example.blockcalc.common.CurrentUser;
import org.example.blockcalc.project.Project;
import org.example.blockcalc.project.ProjectService;
import org.example.blockcalc.settings.SettingsService;
import org.springframework.context.MessageSource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import org.example.blockcalc.common.Times;

import java.util.List;
import java.util.Locale;

@Controller
@RequestMapping("/calculations")
public class CalculationController {

    private final CalculationService calcs;
    private final ProjectService projects;
    private final PdfService pdf;
    private final MessageSource messages;
    private final SettingsService settings;

    public CalculationController(CalculationService calcs, ProjectService projects, PdfService pdf, MessageSource messages,
                                 SettingsService settings) {
        this.settings = settings;
        this.calcs = calcs;
        this.projects = projects;
        this.pdf = pdf;
        this.messages = messages;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String projectId,
                       @AuthenticationPrincipal CurrentUser me, Model model) {
        List<Views.CalcRow> rows = calcs.rows(me, projectId);
        model.addAttribute("rows", rows);
        model.addAttribute("countActive", rows.stream().filter(r -> !r.archived()).count());
        model.addAttribute("countArchived", rows.stream().filter(Views.CalcRow::archived).count());
        model.addAttribute("countFrozen", rows.stream().filter(Views.CalcRow::frozen).count());
        model.addAttribute("projects", projects.visibleTo(me));
        model.addAttribute("projectId", projectId == null ? "" : projectId);
        return "calculation/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(required = false) String projectId,
                          @AuthenticationPrincipal CurrentUser me, Model model) {
        Project p = projectId == null || projectId.isBlank() ? projects.systemProject() : projects.get(projectId);
        if (!projects.canUse(me, p)) {
            throw new AccessDeniedException("Project is not available");
        }
        fillForm(model, calcs.emptyForm(p.id), projects.visibleTo(me), p, false);
        return "calculation/form";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, Model model) {
        CalcForm form = calcs.formFor(me, id);
        Project p = projects.get(form.projectId());
        fillForm(model, form, List.of(p), p, true);
        return "calculation/form";
    }

    private void fillForm(Model model, CalcForm form, List<Project> choices, Project current, boolean editing) {
        model.addAttribute("f", form);
        model.addAttribute("projects", choices);
        model.addAttribute("wsHint", settings.wheelbarrowKg().stripTrailingZeros().toPlainString());
        model.addAttribute("projectName", current.name);
        model.addAttribute("projectSystem", current.system);
        model.addAttribute("editing", editing);
        model.addAttribute("mixes", Mix.values());
    }

    @PostMapping
    public String create(@ModelAttribute CalcInput in, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        String id = calcs.create(me, in).id;
        ra.addFlashAttribute("flash", "flash.calc.saved");
        return "redirect:/calculations/" + id;
    }

    /** htmx: recalculates while the user types. */
    @PostMapping("/preview")
    public String preview(@ModelAttribute CalcInput in, @AuthenticationPrincipal CurrentUser me, Model model, Locale locale) {
        try {
            model.addAttribute("res", calcs.preview(me, in));
            return "calculation/result :: result";
        } catch (BusinessException e) {
            if ("error.calc.blocks".equals(e.getMessageKey())) {
                // Nothing entered yet: show no estimate and no message
                return "calculation/result :: previewEmpty";
            }
            model.addAttribute("previewMessage", messages.getMessage(e.getMessageKey(), e.getArgs(), e.getMessageKey(), locale));
            return "calculation/result :: previewError";
        }
    }

    @GetMapping("/{id}")
    public String view(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, Model model) {
        CalcDetail d = calcs.detail(me, id);
        model.addAttribute("d", d);
        model.addAttribute("res", d.result());
        return "calculation/view";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable String id, @ModelAttribute CalcInput in, @AuthenticationPrincipal CurrentUser me,
                         RedirectAttributes ra) {
        calcs.update(me, id, in);
        ra.addFlashAttribute("flash", "flash.calc.updated");
        return redirectTo(id);
    }

    @PostMapping("/{id}/archive")
    public String archive(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.setArchived(me, id, true);
        ra.addFlashAttribute("flash", "flash.calc.archived");
        return redirectTo(id);
    }

    @PostMapping("/{id}/restore")
    public String restore(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.setArchived(me, id, false);
        ra.addFlashAttribute("flash", "flash.calc.restored");
        return redirectTo(id);
    }

    @PostMapping("/{id}/freeze")
    public String freeze(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.setFrozen(me, id, true);
        ra.addFlashAttribute("flash", "flash.calc.frozen");
        return redirectTo(id);
    }

    @PostMapping("/{id}/unfreeze")
    public String unfreeze(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.setFrozen(me, id, false);
        ra.addFlashAttribute("flash", "flash.calc.unfrozen");
        return redirectTo(id);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable String id, @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.delete(me, id);
        ra.addFlashAttribute("flash", "flash.calc.deleted");
        return "redirect:/calculations";
    }

    @PostMapping("/{id}/share")
    public String share(@PathVariable String id, @RequestParam String userId, @RequestParam AccessLevel level,
                        @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.share(me, id, userId, level);
        ra.addFlashAttribute("flash", "flash.calc.shared");
        return redirectTo(id);
    }

    @PostMapping("/{id}/share-all")
    public String shareAll(@PathVariable String id, @RequestParam AccessLevel level,
                           @AuthenticationPrincipal CurrentUser me, RedirectAttributes ra) {
        calcs.shareWithProjectWorkers(me, id, level);
        ra.addFlashAttribute("flash", "flash.calc.shared");
        return redirectTo(id);
    }

    @PostMapping("/{id}/unshare")
    public String unshare(@PathVariable String id, @RequestParam String userId, @AuthenticationPrincipal CurrentUser me,
                          RedirectAttributes ra) {
        calcs.unshare(me, id, userId);
        ra.addFlashAttribute("flash", "flash.calc.unshared");
        return redirectTo(id);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable String id, @RequestParam(required = false) String tz,
                                     @RequestParam(defaultValue = "false") boolean inline,
                                     @AuthenticationPrincipal CurrentUser me, Locale locale) {
        byte[] bytes = pdf.render(calcs.detail(me, id), locale, Times.zoneOf(tz));
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        (inline ? ContentDisposition.inline() : ContentDisposition.attachment())
                                .filename("calculation-" + id + ".pdf").build().toString())
                .body(bytes);
    }

    private static String redirectTo(String id) {
        return "redirect:/calculations/" + id;
    }
}

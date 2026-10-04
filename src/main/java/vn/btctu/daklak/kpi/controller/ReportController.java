package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import java.util.List;

@Controller
@RequestMapping("/reports")
public class ReportController {
    @Autowired
    private EvaluationRepository evalRepo;

    @GetMapping
    public String reportPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        model.addAttribute(user, currentUser);

        List<Evaluation> evals = evalRepo.findByPeriodValue(Tháng 09/2026);
        model.addAttribute(evaluations, evals);

        return reports;
    }

    @GetMapping("/draft-decision")
    public String draftDecision(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        model.addAttribute(user, currentUser);

        List<Evaluation> evals = evalRepo.findByPeriodValue(Tháng 09/2026);
        model.addAttribute(evaluations, evals);

        return decision_preview;
    }
}

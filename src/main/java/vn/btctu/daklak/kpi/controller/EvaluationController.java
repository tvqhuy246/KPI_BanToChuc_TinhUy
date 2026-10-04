package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.KpiCriteria;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.repository.KpiCriteriaRepository;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.KpiCalculationService;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/evaluation")
public class EvaluationController {
    @Autowired
    private KpiCriteriaRepository criteriaRepo;

    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private KpiCalculationService kpiService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String evaluationForm(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        model.addAttribute(user, currentUser);

        List<KpiCriteria> criteriaList = criteriaRepo.findAllByOrderByGroupTypeAscCodeAsc();
        model.addAttribute(criteriaList, criteriaList);

        Optional<Evaluation> myEval = evalRepo.findByUserIdAndPeriodValue(currentUser.getId(), Tháng 10/2026);
        model.addAttribute(myEval, myEval.orElse(new Evaluation()));

        return evaluation;
    }

    @PostMapping("/submit")
    public String submitEvaluation(@RequestParam(scoreA) Double scoreA,
                                   @RequestParam(scoreB) Double scoreB,
                                   @RequestParam(scoreC) Double scoreC,
                                   @RequestParam(value = selfNotes, required = false) String selfNotes,
                                   HttpSession session,
                                   HttpServletRequest request) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        Evaluation eval = evalRepo.findByUserIdAndPeriodValue(currentUser.getId(), Tháng 10/2026)
                .orElse(new Evaluation());

        eval.setUser(currentUser);
        eval.setDepartment(currentUser.getDepartment());
        eval.setPeriodType(MONTH);
        eval.setPeriodValue(Tháng 10/2026);
        eval.setSelfNotes(selfNotes);
        eval.setStatus(SUBMITTED);

        Evaluation saved = kpiService.calculateAndSave(eval, scoreA, scoreB, scoreC, currentUser.getId());
        auditLogService.log(currentUser.getId(), currentUser.getUsername(), SUBMIT, evaluations, saved.getId(), Nộp phiếu tự đánh giá KPI tháng 10/2026, request.getRemoteAddr());

        return redirect:/evaluation;
    }
}

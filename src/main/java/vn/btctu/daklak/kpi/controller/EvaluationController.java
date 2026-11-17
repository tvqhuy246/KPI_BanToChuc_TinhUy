package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.KpiCriteria;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.repository.KpiCriteriaRepository;
import vn.btctu.daklak.kpi.service.ApprovalService;
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
    private ApprovalService approvalService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String evaluationForm(@RequestParam(value = "periodType", defaultValue = "MONTH") String periodType,
                                 @RequestParam(value = "periodValue", required = false) String periodValue,
                                 HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        model.addAttribute("user", currentUser);

        if (periodValue == null || periodValue.trim().isEmpty()) {
            if ("QUARTER".equalsIgnoreCase(periodType)) {
                periodValue = "Quý IV/2026";
            } else if ("YEAR".equalsIgnoreCase(periodType)) {
                periodValue = "Năm 2026";
            } else {
                periodValue = "Tháng 10/2026";
            }
        }

        model.addAttribute("currentPeriodType", periodType);
        model.addAttribute("currentPeriodValue", periodValue);

        // Kiểm tra đối tượng cán bộ: Lãnh đạo quản lý vs Chuyên viên (HD 05-HD/TU & QĐ 39-QĐ/TU)
        boolean isLeader = currentUser.getOrgLevel() != null && currentUser.getOrgLevel() <= 3;
        model.addAttribute("isLeader", isLeader);

        List<KpiCriteria> criteriaList = criteriaRepo.findAllByOrderByGroupTypeAscCodeAsc();
        model.addAttribute("criteriaList", criteriaList);

        Optional<Evaluation> myEval = evalRepo.findByUserIdAndPeriodValue(currentUser.getId(), periodValue);
        model.addAttribute("myEval", myEval.orElse(new Evaluation()));

        return "evaluation";
    }

    @PostMapping("/submit")
    public String submitEvaluation(@RequestParam(value = "scoreA", required = false, defaultValue = "0") Double scoreA,
                                   @RequestParam(value = "scoreB", required = false, defaultValue = "0") Double scoreB,
                                   @RequestParam(value = "scoreC", required = false, defaultValue = "0") Double scoreC,
                                   @RequestParam(value = "periodType", defaultValue = "MONTH") String periodType,
                                   @RequestParam(value = "periodValue", defaultValue = "Tháng 10/2026") String periodValue,
                                   @RequestParam(value = "targetType", defaultValue = "CHUYEN_VIEN") String targetType,
                                   @RequestParam(value = "leadershipScore", required = false, defaultValue = "0") Double leadershipScore,
                                   @RequestParam(value = "teamProgressScore", required = false, defaultValue = "0") Double teamProgressScore,
                                   @RequestParam(value = "exemplaryScore", required = false, defaultValue = "0") Double exemplaryScore,
                                   @RequestParam(value = "selfNotes", required = false) String selfNotes,
                                   HttpSession session,
                                   RedirectAttributes redirectAttrs,
                                   HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Chuẩn hóa periodValue tránh lỗi mã hóa ký tự Unicode
        if (periodValue == null || periodValue.trim().isEmpty() || periodValue.contains("�")) {
            if ("QUARTER".equalsIgnoreCase(periodType)) {
                periodValue = "Quý IV/2026";
            } else if ("YEAR".equalsIgnoreCase(periodType)) {
                periodValue = "Năm 2026";
            } else {
                periodValue = "Tháng 10/2026";
            }
        }

        Evaluation eval = evalRepo.findByUserIdAndPeriodValue(currentUser.getId(), periodValue)
                .orElse(new Evaluation());

        boolean isActualLeader = currentUser.getOrgLevel() != null && currentUser.getOrgLevel() <= 4;
        if (!isActualLeader) {
            targetType = "CHUYEN_VIEN";
            leadershipScore = 0.0;
            teamProgressScore = 0.0;
            exemplaryScore = 0.0;
        }

        eval.setUser(currentUser);
        eval.setDepartment(currentUser.getDepartment());
        eval.setPeriodType(periodType);
        eval.setPeriodValue(periodValue);
        eval.setTargetType(targetType);
        eval.setLeadershipScore(leadershipScore);
        eval.setTeamProgressScore(teamProgressScore);
        eval.setExemplaryScore(exemplaryScore);
        eval.setSelfNotes(selfNotes);

        // Tính điểm tự đánh giá (nếu là Lãnh đạo có cộng thêm điểm năng lực điều hành)
        if ("LANH_DAO_QUAN_LY".equals(targetType)) {
            double totalLeaderScore = leadershipScore + teamProgressScore + exemplaryScore;
            if (totalLeaderScore > 100) totalLeaderScore = 100.0;
            eval.setSelfScore(totalLeaderScore);
            eval.setStatus("SUBMITTED");
            eval.setEvaluatedAt(java.time.LocalDateTime.now());
            evalRepo.save(eval);
        } else {
            kpiService.calculateAndSave(eval, scoreA, scoreB, scoreC, currentUser.getId());
        }

        // Kích hoạt Engine Smart Routing đẩy cho Quản lý trực tiếp (manager_id)
        Evaluation routed = approvalService.submitWithSmartRouting(eval, currentUser);

        String approverName = routed.getCurrentApprover() != null ? routed.getCurrentApprover().getFullName() : "Lãnh đạo Ban";

        auditLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "SUBMIT",
                "evaluations",
                routed.getId(),
                "Nộp phiếu tự đánh giá KPI kỳ " + periodValue + " (" + targetType + ") và điều hướng thông minh tới: " + approverName,
                request.getRemoteAddr()
        );

        redirectAttrs.addFlashAttribute("success", "Đã gửi phiếu tự chấm điểm KPI kỳ " + periodValue + " thành công! Hồ sơ đã được chuyển tiếp tới " + approverName + " thẩm định.");
        redirectAttrs.addAttribute("periodType", periodType);
        redirectAttrs.addAttribute("periodValue", periodValue);
        return "redirect:/evaluation";
    }
}

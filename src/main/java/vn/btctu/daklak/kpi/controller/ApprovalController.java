package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.service.ApprovalService;
import vn.btctu.daklak.kpi.service.AuditLogService;
import java.util.List;

@Controller
@RequestMapping("/approvals")
public class ApprovalController {
    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String approvalList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        model.addAttribute(user, currentUser);

        List<Evaluation> evals;
        if (TRUONG_PHONG.equals(currentUser.getRole())) {
            evals = evalRepo.findByDepartmentIdAndPeriodValue(currentUser.getDepartment().getId(), Tháng 10/2026);
        } else {
            evals = evalRepo.findByPeriodValue(Tháng 10/2026);
        }
        model.addAttribute(evaluations, evals);

        return approvals;
    }

    @PostMapping("/approve")
    public String approve(@RequestParam(evalId) Long evalId,
                          @RequestParam(finalScore) Double finalScore,
                          @RequestParam(ranking) String ranking,
                          @RequestParam(value = notes, required = false) String notes,
                          HttpSession session,
                          HttpServletRequest request) {
        User currentUser = (User) session.getAttribute(CURRENT_USER);
        if (TRUONG_PHONG.equals(currentUser.getRole())) {
            approvalService.managerReview(evalId, finalScore, notes);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), REVIEW, evaluations, evalId, Trưởng phòng thẩm định điểm:  + finalScore, request.getRemoteAddr());
        } else if (LANH_DAO_BAN.equals(currentUser.getRole()) || ADMIN.equals(currentUser.getRole())) {
            approvalService.leaderApprove(evalId, currentUser, finalScore, ranking, notes);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), APPROVE, evaluations, evalId, Lãnh đạo Ban phê duyệt kết quả:  + ranking, request.getRemoteAddr());
        }
        return redirect:/approvals;
    }
}

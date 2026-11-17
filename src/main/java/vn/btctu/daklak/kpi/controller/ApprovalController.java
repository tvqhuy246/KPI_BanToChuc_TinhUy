package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.service.ApprovalService;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.NotificationService;

import java.util.List;

@Controller
@RequestMapping("/approvals")
public class ApprovalController {
    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private ApprovalService approvalService;

    @Autowired
    private NotificationService notifService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String approvalList(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        model.addAttribute("user", currentUser);

        // Lấy danh sách phiếu được điều hướng đích danh đến người dùng này
        List<Evaluation> evals = approvalService.getPendingApprovalsForUser(currentUser);
        model.addAttribute("evaluations", evals);
        model.addAttribute("unreadNotifs", notifService.getUnreadNotifications(currentUser.getId()));

        return "approvals";
    }

    @PostMapping("/approve")
    public String approve(@RequestParam("evalId") Long evalId,
                          @RequestParam("finalScore") Double finalScore,
                          @RequestParam(value = "ranking", required = false) String ranking,
                          @RequestParam(value = "notes", required = false) String notes,
                          HttpSession session,
                          HttpServletRequest request,
                          RedirectAttributes redirectAttrs) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        int userLevel = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
        if (userLevel >= 5 || "CHUYEN_VIEN".equals(currentUser.getRole()) || "VAN_THU".equals(currentUser.getRole())) {
            return "redirect:/?error=no_permission_approval";
        }

        String role = currentUser.getRole();

        if ("PHO_TRUONG_PHONG".equals(role) || userLevel == 4) {
            // Cấp 4: Phó phòng duyệt -> tự động chuyển tiếp lên Trưởng phòng (Cấp 3)
            Evaluation ev = approvalService.deputyHeadReview(evalId, currentUser, finalScore, notes);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "REVIEW_DEPUTY_HEAD", "evaluations", evalId, "Phó phòng thẩm định điểm: " + finalScore, request.getRemoteAddr());
            String nextApprover = (ev != null && ev.getCurrentApprover() != null) ? ev.getCurrentApprover().getFullName() : "Trưởng phòng";
            redirectAttrs.addFlashAttribute("success", "Phó Trưởng phòng đã thẩm định (" + finalScore + " điểm) và chuyển tiếp hồ sơ lên Trưởng phòng (" + nextApprover + ").");
        } else if ("TRUONG_PHONG".equals(role) || userLevel == 3) {
            // Cấp 3: Trưởng phòng duyệt -> chuyển tiếp lên Phó Trưởng ban (Cấp 2)
            Evaluation ev = approvalService.deptHeadReview(evalId, currentUser, finalScore, notes);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "REVIEW_DEPT_HEAD", "evaluations", evalId, "Trưởng phòng thẩm định điểm: " + finalScore, request.getRemoteAddr());
            String nextApprover = (ev != null && ev.getCurrentApprover() != null) ? ev.getCurrentApprover().getFullName() : "Phó Trưởng ban";
            redirectAttrs.addFlashAttribute("success", "Trưởng phòng đã thẩm định (" + finalScore + " điểm) và trình hồ sơ lên Phó Trưởng ban phụ trách (" + nextApprover + ").");
        } else if ("PHO_TRUONG_BAN".equals(role) || userLevel == 2) {
            // Cấp 2: Phó Trưởng ban xem xét -> chuyển tiếp lên Trưởng ban (Cấp 1)
            Evaluation ev = approvalService.deputyLeaderReview(evalId, currentUser, finalScore, notes);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "REVIEW_DEPUTY_LEADER", "evaluations", evalId, "Phó Trưởng ban cho ý kiến: " + finalScore, request.getRemoteAddr());
            String nextApprover = (ev != null && ev.getCurrentApprover() != null) ? ev.getCurrentApprover().getFullName() : "Trưởng ban";
            redirectAttrs.addFlashAttribute("success", "Phó Trưởng ban đã thẩm định (" + finalScore + " điểm) và trình Trưởng ban (" + nextApprover + ") phê duyệt quyết định cuối cùng.");
        } else if ("LANH_DAO_BAN".equals(role) || "ADMIN".equals(role) || userLevel == 1) {
            // Cấp 1: Trưởng ban phê duyệt kết luận cuối cùng
            String finalRanking = (ranking != null && !ranking.trim().isEmpty()) ? ranking : (finalScore >= 90 ? "XUAT_SAC" : (finalScore >= 70 ? "TOT" : (finalScore >= 50 ? "HOAN_THANH" : "KHONG_HOAN_THANH")));
            Evaluation ev = approvalService.topLeaderApprove(evalId, currentUser, finalScore, finalRanking, notes);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "APPROVE_FINAL", "evaluations", evalId, "Trưởng ban phê duyệt chính thức kết quả: " + finalRanking, request.getRemoteAddr());
            redirectAttrs.addFlashAttribute("success", "Trưởng ban đã phê duyệt chính thức hồ sơ KPI (" + finalScore + " điểm - Xếp loại: " + (ev != null ? ev.getRankingDisplayName() : finalRanking) + ").");
        }

        return "redirect:/approvals";
    }
}

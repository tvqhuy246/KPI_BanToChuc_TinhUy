package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.btctu.daklak.kpi.model.AuditLog;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.AuditLogRepository;

import java.util.List;

@Controller
@RequestMapping("/audit-logs")
public class AuditLogController {

    @Autowired
    private AuditLogRepository auditLogRepo;

    @GetMapping
    public String auditLogPage(@RequestParam(value = "action", required = false, defaultValue = "ALL") String action,
                               @RequestParam(value = "username", required = false) String username,
                               HttpSession session,
                               Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra thẩm quyền: Chỉ Lãnh đạo Ban (Cấp 1, 2) hoặc Admin hệ thống mới được xem nhật ký kiểm toán
        boolean isAuthorized = "ADMIN".equals(currentUser.getRole()) ||
                               "LANH_DAO_BAN".equals(currentUser.getRole()) ||
                               (currentUser.getOrgLevel() != null && currentUser.getOrgLevel() <= 2);

        if (!isAuthorized) {
            model.addAttribute("error", "Đồng chí không có thẩm quyền truy cập Nhật ký Kiểm toán hệ thống.");
            return "redirect:/dashboard";
        }

        model.addAttribute("user", currentUser);

        String searchUser = (username != null && !username.trim().isEmpty()) ? username.trim() : null;
        List<AuditLog> logs = auditLogRepo.searchLogs(action, searchUser);

        model.addAttribute("logs", logs);
        model.addAttribute("selectedAction", action);
        model.addAttribute("searchUsername", username);
        model.addAttribute("totalLogs", logs.size());

        return "audit_logs";
    }
}
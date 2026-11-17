package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.model.WorkLog;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.repository.TaskRepository;
import vn.btctu.daklak.kpi.repository.UserRepository;
import vn.btctu.daklak.kpi.repository.WorkLogRepository;
import vn.btctu.daklak.kpi.service.NotificationService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private TaskRepository taskRepo;

    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private WorkLogRepository workLogRepo;

    @Autowired
    private NotificationService notifService;

    public String index(String filter, HttpSession session, Model model) {
        return index(filter, null, session, model);
    }

    @GetMapping({"/", "/dashboard"})
    public String index(@RequestParam(value = "filter", defaultValue = "today") String filter,
                        @RequestParam(value = "error", required = false) String error,
                        HttpSession session,
                        Model model) {
        if (error != null) {
            String errorMsg = "Đồng chí không có thẩm quyền truy cập chức năng này!";
            if ("no_permission_admin".equals(error)) {
                errorMsg = "Từ chối truy cập: Chỉ Quản trị viên hệ thống (Admin) mới có quyền vào Trung tâm Quản trị CSDL!";
            } else if ("no_permission_audit".equals(error)) {
                errorMsg = "Từ chối truy cập: Chỉ Lãnh đạo Ban mới có quyền xem Nhật ký Kiểm toán hệ thống!";
            } else if ("no_permission_approval".equals(error)) {
                errorMsg = "Từ chối truy cập: Cán bộ Cấp 5 (Chuyên viên/Văn thư) không có quyền vào phân hệ Phê duyệt đánh giá KPI!";
            } else if ("no_permission_reports".equals(error)) {
                errorMsg = "Từ chối truy cập: Phân hệ Báo cáo & Quyết định chỉ dành cho cán bộ Lãnh đạo quản lý!";
            } else if ("no_permission_draft".equals(error)) {
                errorMsg = "Từ chối truy cập: Dự thảo Quyết định chỉ dành cho Lãnh đạo Ban và Trưởng phòng chuyên môn!";
            }
            model.addAttribute("permissionError", errorMsg);
        }
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        model.addAttribute("user", currentUser);
        model.addAttribute("currentFilter", filter);

        String periodQuery = "Tháng 10/2026";
        String filterTitle = "Hôm nay (" + LocalDate.now() + ")";
        if ("week".equalsIgnoreCase(filter)) {
            filterTitle = "Tuần này (Tuần 41/2026)";
        } else if ("month".equalsIgnoreCase(filter)) {
            filterTitle = "Toàn bộ Tháng 10/2026";
        } else if ("quarter".equalsIgnoreCase(filter)) {
            periodQuery = "Quý IV/2026";
            filterTitle = "Đánh giá Quý IV/2026 (HD 05-HD/TU)";
        } else if ("year".equalsIgnoreCase(filter)) {
            periodQuery = "Năm 2026";
            filterTitle = "Tổng kết Năm 2026 (QĐ 39-QĐ/TU)";
        }
        model.addAttribute("filterTitle", filterTitle);
        model.addAttribute("periodQuery", periodQuery);

        // =========================================================================
        // DASHBOARD QUẢN LÝ THEO CẤP (HIERARCHICAL DASHBOARD SCOPING)
        // =========================================================================
        String role = currentUser.getRole();
        int orgLevel = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;

        List<User> scopedStaff = new ArrayList<>();
        List<Task> scopedTasks = new ArrayList<>();
        List<Evaluation> scopedEvals = new ArrayList<>();
        String scopeTitle = "";

        if ("LANH_DAO_BAN".equals(role) || "ADMIN".equals(role) || orgLevel == 1) {
            // CẤP 1 (TRƯỞNG BAN): Theo dõi toàn diện 39 cán bộ công chức & toàn bộ phòng ban
            scopeTitle = "Toàn diện Ban Tổ chức Tỉnh ủy (39 cán bộ công chức)";
            scopedStaff = userRepo.findAllActiveUsers();
            scopedTasks = taskRepo.findAll();
            scopedEvals = evalRepo.findByPeriodValue(periodQuery);
            if (scopedEvals.isEmpty()) scopedEvals = evalRepo.findByPeriodValue("Tháng 10/2026");

        } else if ("PHO_TRUONG_BAN".equals(role) || orgLevel == 2) {
            // CẤP 2 (PHÓ TRƯỞNG BAN): Theo dõi khối / các phòng chuyên môn phụ trách
            Long deptId = currentUser.getDepartment() != null ? currentUser.getDepartment().getId() : 1L;
            if (deptId != null && deptId > 1) {
                scopeTitle = "Khối chuyên môn: " + currentUser.getDepartment().getName();
                scopedStaff = userRepo.findByDepartmentId(deptId);
                scopedTasks = taskRepo.findByDepartmentId(deptId);
                scopedEvals = evalRepo.findByDepartmentIdAndPeriodValue(deptId, periodQuery);
            } else {
                scopeTitle = "Khối điều phối & văn phòng";
                scopedStaff = userRepo.findSubordinatesRecursive(currentUser.getId());
                scopedTasks = taskRepo.findAll();
                scopedEvals = evalRepo.findByPeriodValue(periodQuery);
            }

        } else if ("TRUONG_PHONG".equals(role) || orgLevel == 3) {
            // CẤP 3 (TRƯỞNG PHÒNG): Theo dõi toàn bộ cán bộ và nhiệm vụ trong phòng
            Long deptId = currentUser.getDepartment().getId();
            scopeTitle = "Phòng ban: " + currentUser.getDepartment().getName();
            scopedStaff = userRepo.findByDepartmentId(deptId);
            scopedTasks = taskRepo.findByDepartmentId(deptId);
            scopedEvals = evalRepo.findByDepartmentIdAndPeriodValue(deptId, periodQuery);

        } else if ("PHO_TRUONG_PHONG".equals(role) || orgLevel == 4) {
            // CẤP 4 (PHÓ PHÒNG): Theo dõi tổ chuyên viên phụ trách trực tiếp
            scopeTitle = "Tổ chuyên viên phụ trách trực tiếp";
            scopedStaff = userRepo.findByManagerId(currentUser.getId());
            scopedTasks = taskRepo.findByAssignedById(currentUser.getId());
            for (User sub : scopedStaff) {
                evalRepo.findByUserIdAndPeriodValue(sub.getId(), periodQuery).ifPresent(scopedEvals::add);
            }

        } else {
            // CẤP 5 (CHUYÊN VIÊN): Bàn làm việc cá nhân
            scopeTitle = "Bàn làm việc cá nhân chuyên môn";
            scopedStaff.add(currentUser);
            scopedTasks = taskRepo.findByAssignedToId(currentUser.getId());
            evalRepo.findByUserIdAndPeriodValue(currentUser.getId(), periodQuery).ifPresent(scopedEvals::add);
        }

        // BẢO VỆ AN TOÀN BÍ MẬT NHÀ NƯỚC (NĐ 63/2026/NĐ-CP & NĐ 85/2016)
        // Chỉ Lãnh đạo Ban và Trưởng phòng BVCTNB mới xem được nội dung/tài liệu nhiệm vụ MẬT
        boolean canViewConfidential = "LANH_DAO_BAN".equals(role) || "ADMIN".equals(role) || 
                ("TRUONG_PHONG".equals(role) && currentUser.getDepartment() != null && "PHONG_BVCTNB".equals(currentUser.getDepartment().getCode()));

        if (!canViewConfidential) {
            for (Task t : scopedTasks) {
                boolean isMyTask = t.getAssignedTo() != null && t.getAssignedTo().getId().equals(currentUser.getId());
                boolean isMyAssigned = t.getAssignedBy() != null && t.getAssignedBy().getId().equals(currentUser.getId());
                // Cán bộ được giao việc hoặc người trực tiếp giao việc được phép thấy tiêu đề nhiệm vụ của mình
                if (!isMyTask && !isMyAssigned) {
                    if (Boolean.TRUE.equals(t.getIsConfidential()) || "MAT".equals(t.getSecurityLevel()) || "TUYET_MAT".equals(t.getSecurityLevel())) {
                        t.setTitle("[TÀI LIỆU MẬT - ĐÃ ẨN NỘI DUNG THEO NĐ 63/2026/NĐ-CP]");
                        t.setDescription("Bạn không có thẩm quyền truy cập nội dung văn bản mật của đơn vị khác.");
                        t.setEvidenceText("*** BẢO MẬT NHÀ NƯỚC ***");
                    }
                }
            }
        }

        // Tính toán thống kê theo phạm vi
        long totalUsers = scopedStaff.size();
        long totalTasks = scopedTasks.size();
        long completedTasks = scopedTasks.stream().filter(t -> "COMPLETED".equalsIgnoreCase(t.getStatus())).count();

        Double avgScore = evalRepo.getAverageScoreByPeriod(periodQuery);
        if (avgScore == null) avgScore = 94.5;

        model.addAttribute("scopeTitle", scopeTitle);
        model.addAttribute("scopedStaff", scopedStaff);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalTasks", totalTasks);
        model.addAttribute("completedTasks", completedTasks);
        model.addAttribute("avgScore", Math.round(avgScore * 10.0) / 10.0);
        model.addAttribute("canViewConfidential", canViewConfidential);

        model.addAttribute("evaluations", scopedEvals);
        model.addAttribute("recentTasks", scopedTasks);
        model.addAttribute("workLogs", workLogRepo.findAll());
        model.addAttribute("unreadCount", notifService.countUnread(currentUser.getId()));

        return "dashboard";
    }

    @GetMapping("/today")
    public String todayPage(HttpSession session, Model model) {
        return index("today", session, model);
    }

    @GetMapping("/week")
    public String weekPage(HttpSession session, Model model) {
        return index("week", session, model);
    }

    @GetMapping("/month")
    public String monthPage(HttpSession session, Model model) {
        return index("month", session, model);
    }

    @GetMapping("/quarter")
    public String quarterPage(HttpSession session, Model model) {
        return index("quarter", session, model);
    }

    @GetMapping("/year")
    public String yearPage(HttpSession session, Model model) {
        return index("year", session, model);
    }
}

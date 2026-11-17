package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.btctu.daklak.kpi.model.JobCatalog;
import vn.btctu.daklak.kpi.model.Task;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.JobCatalogService;
import vn.btctu.daklak.kpi.service.TaskService;
import vn.btctu.daklak.kpi.service.UserService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;

    @Autowired
    private JobCatalogService jobCatalogService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String listTasks(@RequestParam(value = "periodFilter", required = false, defaultValue = "ALL") String periodFilter,
                            @RequestParam(value = "statusFilter", required = false, defaultValue = "ALL") String statusFilter,
                            HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }
        model.addAttribute("user", currentUser);

        int userLevel = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
        String role = currentUser.getRole();

        // 1. Phân quyền xem danh sách nhiệm vụ
        List<Task> rawTasks;
        if ("CHUYEN_VIEN".equals(role) || "VAN_THU".equals(role) || userLevel == 5) {
            rawTasks = taskService.getTasksByUser(currentUser.getId());
        } else if ("TRUONG_PHONG".equals(role) || "PHO_TRUONG_PHONG".equals(role) || userLevel == 3 || userLevel == 4) {
            rawTasks = new ArrayList<>(taskService.getTasksByDepartment(currentUser.getDepartment().getId()));
            // Đảm bảo Cấp 3 và Cấp 4 ở mọi phòng đều nhìn thấy nhiệm vụ của Trần Võ Quang Huy (ID=231) để theo dõi/đánh giá mẫu
            for (Task t : taskService.getTasksByUser(231L)) {
                if (rawTasks.stream().noneMatch(existing -> existing.getId().equals(t.getId()))) {
                    rawTasks.add(t);
                }
            }
        } else {
            rawTasks = taskService.getAllTasks();
        }

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.minusDays(now.getDayOfWeek().getValue() - 1);
        LocalDate weekEnd = weekStart.plusDays(6);
        LocalDate monthStart = now.withDayOfMonth(1);
        LocalDate monthEnd = now.withDayOfMonth(now.lengthOfMonth());

        List<Task> filtered = new ArrayList<>();
        for (Task t : rawTasks) {
            boolean matchStatus = true;
            if (!"ALL".equalsIgnoreCase(statusFilter)) {
                if ("COMPLETED".equalsIgnoreCase(statusFilter) && !"COMPLETED".equalsIgnoreCase(t.getStatus())) matchStatus = false;
                if ("IN_PROGRESS".equalsIgnoreCase(statusFilter) && !"IN_PROGRESS".equalsIgnoreCase(t.getStatus())) matchStatus = false;
                if ("OVERDUE".equalsIgnoreCase(statusFilter) && !"OVERDUE".equalsIgnoreCase(t.getStatus())) matchStatus = false;
            }

            boolean matchPeriod = true;
            if (!"ALL".equalsIgnoreCase(periodFilter) && t.getDueDate() != null) {
                LocalDate d = t.getDueDate();
                if ("THIS_WEEK".equalsIgnoreCase(periodFilter)) {
                    if (d.isBefore(weekStart) || d.isAfter(weekEnd)) matchPeriod = false;
                } else if ("THIS_MONTH".equalsIgnoreCase(periodFilter)) {
                    if (d.isBefore(monthStart) || d.isAfter(monthEnd)) matchPeriod = false;
                } else if ("THIS_QUARTER".equalsIgnoreCase(periodFilter)) {
                    int currentQuarter = (now.getMonthValue() - 1) / 3 + 1;
                    int taskQuarter = (d.getMonthValue() - 1) / 3 + 1;
                    if (d.getYear() != now.getYear() || currentQuarter != taskQuarter) matchPeriod = false;
                }
            }

            if (matchStatus && matchPeriod) {
                filtered.add(t);
            }
        }

        model.addAttribute("tasks", filtered);
        model.addAttribute("periodFilter", periodFilter);
        model.addAttribute("statusFilter", statusFilter);
        model.addAttribute("totalTasksCount", rawTasks.size());
        model.addAttribute("filteredTasksCount", filtered.size());

        // 2. PHÂN QUYỀN GIAO VIỆC NGHIÊM NGẶT (NGUYÊN TẮC HÀNH CHÍNH ĐẢNG & NHÀ NƯỚC):
        // Cán bộ chỉ được giao việc cho cấp dưới thuộc thẩm quyền quản lý:
        // - Cấp 1 (Trưởng ban), Admin: Giao cho toàn thể cán bộ công chức (Cấp 2, 3, 4, 5).
        // - Cấp 2 (Phó ban): Giao cho Trưởng phòng, Phó phòng, Chuyên viên thuộc khối phụ trách (Cấp 3, 4, 5).
        // - Cấp 3 (Trưởng phòng): Chỉ giao cho Phó phòng và Chuyên viên trong phòng của mình (Cấp 4, 5).
        // - Cấp 4 (Phó phòng): Chỉ giao cho các Chuyên viên trong phòng / tổ phụ trách (Cấp 5).
        // - Cấp 5 (Chuyên viên, Nhân viên, Văn thư): TUYỆT ĐỐI KHÔNG có quyền giao việc cho bất kỳ ai!
        boolean canAssignTask = userLevel < 5;
        model.addAttribute("canAssignTask", canAssignTask);

        List<User> assignableUsers = new ArrayList<>();
        if (canAssignTask) {
            List<User> allActive = userService.getAllActiveUsers();
            for (User u : allActive) {
                int targetLvl = u.getOrgLevel() != null ? u.getOrgLevel() : 5;
                // Bắt buộc cấp của người nhận việc phải THẤP HƠN cấp của người giao việc (Số level lớn hơn)
                if (targetLvl > userLevel) {
                    // Nếu là Trưởng phòng / Phó phòng: Giao cho cán bộ cùng phòng chuyên môn + các tài khoản mẫu Trần Võ Quang Huy
                    if (userLevel == 3 || userLevel == 4) {
                        if (vn.btctu.daklak.kpi.service.KpiCalculationService.isHuyTestAccount(u) ||
                            (currentUser.getDepartment() != null && u.getDepartment() != null &&
                            currentUser.getDepartment().getId().equals(u.getDepartment().getId()))) {
                            assignableUsers.add(u);
                        }
                    } else {
                        // Lãnh đạo Ban (Cấp 1, Cấp 2): Giao được toàn cơ quan
                        assignableUsers.add(u);
                    }
                }
            }
        }
        model.addAttribute("users", assignableUsers);

        model.addAttribute("jobCatalogs", jobCatalogService.getAllActive());
        return "tasks";
    }

    @PostMapping("/create")
    public String createTask(@RequestParam("title") String title,
                             @RequestParam(value = "description", required = false) String description,
                             @RequestParam(value = "jobCatalogId", required = false) Long jobCatalogId,
                             @RequestParam("assignedToId") Long assignedToId,
                             @RequestParam(value = "dueDate", required = false) String dueDateStr,
                             @RequestParam(value = "deliverableProduct", required = false) String deliverableProduct,
                             @RequestParam(value = "actualHours", required = false, defaultValue = "0") Double actualHours,
                             @RequestParam(value = "isConfidential", required = false, defaultValue = "false") Boolean isConfidential,
                             @RequestParam(value = "securityLevel", required = false, defaultValue = "THUONG") String securityLevel,
                             HttpSession session,
                             RedirectAttributes redirectAttrs,
                             HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        int userLevel = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
        // Kiểm tra thẩm quyền giao việc
        if (userLevel >= 5) {
            redirectAttrs.addFlashAttribute("error", "Đồng chí là Chuyên viên/Nhân viên (Cấp 5), không có thẩm quyền giao nhiệm vụ cho người khác!");
            return "redirect:/tasks";
        }

        Optional<User> targetUserOpt = userService.findById(assignedToId);
        if (!targetUserOpt.isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Không tìm thấy cán bộ được giao việc!");
            return "redirect:/tasks";
        }

        User targetUser = targetUserOpt.get();
        int targetLevel = targetUser.getOrgLevel() != null ? targetUser.getOrgLevel() : 5;
        if (targetLevel <= userLevel) {
            redirectAttrs.addFlashAttribute("error", "Vi phạm nguyên tắc hành chính: Tuyệt đối không được giao việc cho cấp trên hoặc đồng cấp!");
            return "redirect:/tasks";
        }

        Task t = new Task();
        t.setTitle(title);
        t.setDescription(description);
        t.setAssignedBy(currentUser);
        t.setAssignedTo(targetUser);
        t.setDepartment(targetUser.getDepartment());

        if (jobCatalogId != null) {
            jobCatalogService.getById(jobCatalogId).ifPresent(catalog -> {
                t.setJobCatalog(catalog);
                t.setStandardHours(catalog.getStandardHours());
                t.setIsConfidential(catalog.getIsConfidential());
                t.setSecurityLevel(catalog.getSecurityLevel());
                if (deliverableProduct == null || deliverableProduct.trim().isEmpty()) {
                    t.setDeliverableProduct(catalog.getDeliverableProduct());
                }
            });
        }

        if (deliverableProduct != null && !deliverableProduct.trim().isEmpty()) {
            t.setDeliverableProduct(deliverableProduct);
        }
        if (isConfidential != null) {
            t.setIsConfidential(isConfidential);
        }
        if (securityLevel != null) {
            t.setSecurityLevel(securityLevel);
        }

        t.setActualHours(actualHours);
        t.setStartDate(LocalDate.now());
        t.setDueDate(dueDateStr != null && !dueDateStr.isEmpty() ? LocalDate.parse(dueDateStr) : LocalDate.now().plusDays(7));
        t.setStatus("IN_PROGRESS");
        t.setProgress(0);

        taskService.saveTask(t);
        auditLogService.log(currentUser.getId(), currentUser.getUsername(), "CREATE_TASK", "tasks", t.getId(), 
                "Giao nhiệm vụ [" + title + "] cho đồng chí: " + targetUser.getFullName() + " (Cấp " + targetLevel + ")", 
                request.getRemoteAddr());

        redirectAttrs.addFlashAttribute("success", "Đã giao nhiệm vụ thành công cho đồng chí " + targetUser.getFullName() + "!");
        return "redirect:/tasks";
    }

    @PostMapping("/update-progress")
    public String updateProgress(@RequestParam("taskId") Long taskId,
                                 @RequestParam("progress") Integer progress,
                                 @RequestParam(value = "actualHours", required = false) Double actualHours,
                                 @RequestParam(value = "deliverableProduct", required = false) String deliverableProduct,
                                 @RequestParam("evidenceText") String evidenceText,
                                 HttpSession session,
                                 RedirectAttributes redirectAttrs,
                                 HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        Optional<Task> tOpt = taskService.findById(taskId);
        if (tOpt.isPresent()) {
            Task t = tOpt.get();
            t.setProgress(progress);
            t.setEvidenceText(evidenceText);
            if (actualHours != null) {
                t.setActualHours(actualHours);
            }
            if (deliverableProduct != null && !deliverableProduct.trim().isEmpty()) {
                t.setDeliverableProduct(deliverableProduct.trim());
            }
            if (progress != null && progress >= 100) {
                t.setStatus("COMPLETED");
                t.setCompletedDate(java.time.LocalDate.now());
            } else {
                t.setStatus("IN_PROGRESS");
            }
            taskService.saveTask(t);
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "SUBMIT_TASK_REPORT", "tasks", t.getId(), 
                    "Báo cáo tiến độ " + progress + "% và nộp sản phẩm nhiệm vụ: " + t.getTitle(), request.getRemoteAddr());

            redirectAttrs.addFlashAttribute("success", "Đã nộp báo cáo kết quả và minh chứng sản phẩm cho nhiệm vụ: " + t.getTitle() + " (Tiến độ: " + progress + "%) thành công!");
        } else {
            redirectAttrs.addFlashAttribute("error", "Không tìm thấy nhiệm vụ cần báo cáo!");
        }
        return "redirect:/tasks";
    }

    @PostMapping("/review")
    public String reviewTask(@RequestParam("taskId") Long taskId,
                             @RequestParam("action") String action,
                             @RequestParam(value = "reviewNotes", required = false) String reviewNotes,
                             HttpSession session,
                             RedirectAttributes redirectAttrs,
                             HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        Optional<Task> tOpt = taskService.findById(taskId);
        if (tOpt.isPresent()) {
            Task t = tOpt.get();
            int userLevel = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
            int assigneeLevel = (t.getAssignedTo() != null && t.getAssignedTo().getOrgLevel() != null) ? t.getAssignedTo().getOrgLevel() : 5;

            boolean isAssigner = t.getAssignedBy() != null && t.getAssignedBy().getId().equals(currentUser.getId());
            boolean isSuperior = userLevel < assigneeLevel;
            boolean isAdmin = "ADMIN".equals(currentUser.getRole());

            if (!isAssigner && !isSuperior && !isAdmin) {
                redirectAttrs.addFlashAttribute("error", "Đồng chí không có thẩm quyền nghiệm thu nhiệm vụ này!");
                return "redirect:/tasks";
            }

            String assigneeName = t.getAssignedTo() != null ? t.getAssignedTo().getFullName() : "cán bộ";

            if ("ACCEPT".equalsIgnoreCase(action)) {
                t.setStatus("COMPLETED");
                t.setProgress(100);
                if (t.getCompletedDate() == null) {
                    t.setCompletedDate(LocalDate.now());
                }
                taskService.saveTask(t);
                auditLogService.log(currentUser.getId(), currentUser.getUsername(), "ACCEPT_TASK", "tasks", t.getId(),
                        "Nghiệm thu ĐẠT nhiệm vụ [" + t.getTitle() + "] của đồng chí " + assigneeName, request.getRemoteAddr());

                redirectAttrs.addFlashAttribute("success", "Đã nghiệm thu đánh giá ĐẠT cho sản phẩm của đồng chí " + assigneeName + "!");
            } else {
                t.setStatus("IN_PROGRESS");
                if (t.getProgress() != null && t.getProgress() >= 100) {
                    t.setProgress(90);
                }
                taskService.saveTask(t);
                auditLogService.log(currentUser.getId(), currentUser.getUsername(), "REJECT_TASK", "tasks", t.getId(),
                        "Yêu cầu hoàn thiện lại nhiệm vụ [" + t.getTitle() + "] của đồng chí " + assigneeName + ". Lý do: " + reviewNotes, request.getRemoteAddr());

                redirectAttrs.addFlashAttribute("error", "Đã yêu cầu đồng chí " + assigneeName + " chỉnh sửa, hoàn thiện lại sản phẩm theo ý kiến chỉ đạo.");
            }
        } else {
            redirectAttrs.addFlashAttribute("error", "Không tìm thấy nhiệm vụ cần nghiệm thu!");
        }
        return "redirect:/tasks";
    }

}

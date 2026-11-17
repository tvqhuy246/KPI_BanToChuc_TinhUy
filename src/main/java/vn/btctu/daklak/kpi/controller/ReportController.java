package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.btctu.daklak.kpi.model.Department;
import vn.btctu.daklak.kpi.model.DepartmentEvaluation;
import vn.btctu.daklak.kpi.model.Evaluation;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.DepartmentEvaluationRepository;
import vn.btctu.daklak.kpi.repository.DepartmentRepository;
import vn.btctu.daklak.kpi.repository.EvaluationRepository;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.KpiCalculationService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private EvaluationRepository evalRepo;

    @Autowired
    private DepartmentRepository deptRepo;

    @Autowired
    private DepartmentEvaluationRepository deptEvalRepo;

    @Autowired
    private KpiCalculationService kpiService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping
    public String reportPage(@RequestParam(value = "periodType", defaultValue = "MONTH") String periodType,
                             @RequestParam(value = "periodValue", required = false) String periodValue,
                             @RequestParam(value = "deptId", required = false) Long deptId,
                             @RequestParam(value = "activeTab", defaultValue = "staff") String activeTab,
                             HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        model.addAttribute("user", currentUser);

        if (periodValue == null || periodValue.trim().isEmpty()) {
            if ("QUARTER".equalsIgnoreCase(periodType)) {
                periodValue = "Quý IV/2026";
            } else if ("YEAR".equalsIgnoreCase(periodType)) {
                periodValue = "Năm 2026";
            } else {
                periodValue = "Tháng 09/2026";
            }
        }

        model.addAttribute("currentPeriodType", periodType);
        model.addAttribute("currentPeriodValue", periodValue);
        model.addAttribute("currentDeptId", deptId);
        model.addAttribute("activeTab", activeTab);

        boolean canAutoCalculate = currentUser != null && (
                "ADMIN".equals(currentUser.getRole()) ||
                "LANH_DAO_BAN".equals(currentUser.getRole()) ||
                (currentUser.getOrgLevel() != null && currentUser.getOrgLevel() <= 2)
        );
        model.addAttribute("canAutoCalculate", canAutoCalculate);

        List<Department> departments = deptRepo.findAll();
        model.addAttribute("departments", departments);

        // 1. Dữ liệu xếp loại CÁ NHÂN
        List<Evaluation> evals;
        if (deptId != null && deptId > 0) {
            evals = new java.util.ArrayList<>(evalRepo.findByDepartmentIdAndPeriodValue(deptId, periodValue));
            // Luôn đính kèm hồ sơ của Trần Võ Quang Huy 1..5 khi lọc theo bất kỳ phòng ban nào để đối chứng điểm thấp
            for (Evaluation e : evalRepo.findByPeriodValue(periodValue)) {
                if (e.getUser() != null && KpiCalculationService.isHuyTestAccount(e.getUser())) {
                    if (evals.stream().noneMatch(ex -> ex.getId().equals(e.getId()))) {
                        evals.add(e);
                    }
                }
            }
        } else {
            evals = evalRepo.findByPeriodValue(periodValue);
        }
        model.addAttribute("evaluations", evals);

        long total = evals.size();
        long excellent = evals.stream().filter(e -> "XUAT_SAC".equalsIgnoreCase(e.getRanking())).count();
        double rate = total > 0 ? (double) excellent * 100.0 / total : 0.0;
        model.addAttribute("totalEvaluations", total);
        model.addAttribute("excellentCount", excellent);
        model.addAttribute("excellentRate", Math.round(rate * 10.0) / 10.0);

        // 2. Dữ liệu xếp loại TẬP THỂ PHÒNG BAN (YÊU CẦU 2.5)
        List<DepartmentEvaluation> deptEvals = deptEvalRepo.findByPeriodValue(periodValue);
        if (deptEvals.isEmpty()) {
            deptEvals = kpiService.autoSummarizeDepartmentKpi(periodValue, periodType);
        }
        model.addAttribute("deptEvaluations", deptEvals);

        return "reports";
    }

    @PostMapping("/auto-calculate")
    public String autoCalculate(@RequestParam("periodValue") String periodValue,
                                @RequestParam(value = "periodType", defaultValue = "MONTH") String periodType,
                                HttpSession session,
                                RedirectAttributes redirectAttrs,
                                HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        boolean canAutoCalculate = "ADMIN".equals(currentUser.getRole()) ||
                "LANH_DAO_BAN".equals(currentUser.getRole()) ||
                (currentUser.getOrgLevel() != null && currentUser.getOrgLevel() <= 2);

        if (!canAutoCalculate) {
            redirectAttrs.addFlashAttribute("error", "Đồng chí không có thẩm quyền chốt sổ điểm toàn Ban! Chỉ Lãnh đạo Ban mới có quyền này.");
            return "redirect:/reports?periodValue=" + periodValue;
        }

        Map<String, Object> calcResult = kpiService.autoCalculateAllStaff(periodValue, periodType);
        auditLogService.log(
                currentUser.getId(),
                currentUser.getUsername(),
                "AUTO_CALCULATE_KPI",
                "evaluations",
                null,
                "Chốt sổ và tính điểm tự động toàn cơ quan kỳ " + periodValue,
                request.getRemoteAddr()
        );

        redirectAttrs.addFlashAttribute("success", (String) calcResult.get("message"));
        return "redirect:/reports?periodType=" + periodType + "&periodValue=" + periodValue;
    }

    @GetMapping("/draft-decision")
    public String draftDecision(@RequestParam(value = "periodValue", defaultValue = "Tháng 09/2026") String periodValue,
                                HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        int level = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
        if (!"ADMIN".equals(currentUser.getRole()) && !"LANH_DAO_BAN".equals(currentUser.getRole()) && level > 3) {
            return "redirect:/reports?error=no_permission_draft";
        }

        model.addAttribute("user", currentUser);

        List<Evaluation> evals = evalRepo.findByPeriodValue(periodValue);
        model.addAttribute("evaluations", evals);
        model.addAttribute("periodValue", periodValue);

        return "decision_preview";
    }

    @GetMapping({"/export-excel", "/export-csv"})
    public void exportCsv(@RequestParam(value = "periodValue", defaultValue = "Tháng 09/2026") String periodValue,
                          @RequestParam(value = "deptId", required = false) Long deptId,
                          HttpSession session,
                          HttpServletResponse response) throws IOException {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            response.sendError(401, "Chưa đăng nhập");
            return;
        }

        int level = currentUser.getOrgLevel() != null ? currentUser.getOrgLevel() : 5;
        if (level >= 5 && !"ADMIN".equals(currentUser.getRole())) {
            response.sendError(403, "Từ chối truy cập: Chuyên viên không có quyền trích xuất báo cáo toàn cơ quan");
            return;
        }

        // Trưởng phòng / Phó phòng: Bắt buộc chỉ được xuất báo cáo trong phạm vi phòng mình
        if ((level == 3 || level == 4) && !"ADMIN".equals(currentUser.getRole()) && !"LANH_DAO_BAN".equals(currentUser.getRole())) {
            if (currentUser.getDepartment() != null) {
                deptId = currentUser.getDepartment().getId();
            }
        }
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"Bao_Cao_KPI_" + periodValue.replace(" ", "_").replace("/", "-") + ".csv\"");

        List<Evaluation> evals;
        if (deptId != null && deptId > 0) {
            evals = evalRepo.findByDepartmentIdAndPeriodValue(deptId, periodValue);
        } else {
            evals = evalRepo.findByPeriodValue(periodValue);
        }

        PrintWriter writer = response.getWriter();
        writer.write('\ufeff');
        writer.println("STT,Họ và tên cán bộ,Chức vụ / Vị trí,Phòng chuyên môn,Kỳ đánh giá,Điểm tự chấm,Điểm chốt cuối,Xếp loại thi đua,Trạng thái");

        int stt = 1;
        for (Evaluation e : evals) {
            String name = e.getUser() != null ? e.getUser().getFullName() : "";
            String pos = (e.getUser() != null && e.getUser().getPosition() != null) ? e.getUser().getPosition().getTitle() : "";
            String dept = e.getDepartment() != null ? e.getDepartment().getName() : "";
            Double self = e.getSelfScore() != null ? e.getSelfScore() : 0.0;
            Double fin = e.getFinalScore() != null ? e.getFinalScore() : 0.0;
            String rank = e.getRankingDisplayName() != null ? e.getRankingDisplayName() : "";
            String status = e.getStatusDisplayName() != null ? e.getStatusDisplayName() : "";

            writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",%.1f,%.1f,\"%s\",\"%s\"%n",
                    stt++, name, pos, dept, periodValue, self, fin, rank, status);
        }
        writer.flush();
    }
}
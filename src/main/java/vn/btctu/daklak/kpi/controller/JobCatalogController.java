package vn.btctu.daklak.kpi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.btctu.daklak.kpi.model.JobCatalog;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.service.JobCatalogService;
import vn.btctu.daklak.kpi.service.UserService;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class JobCatalogController {

    private final JobCatalogService jobCatalogService;
    private final UserService userService;

    public JobCatalogController(JobCatalogService jobCatalogService, UserService userService) {
        this.jobCatalogService = jobCatalogService;
        this.userService = userService;
    }

    /**
     * Màn hình tra cứu 49 danh mục công việc chuẩn BTCTU
     */
    @GetMapping("/job-catalog")
    public String showJobCatalogPage(Model model, HttpSession session,
                                     @RequestParam(required = false) String group,
                                     @RequestParam(required = false) String keyword) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            currentUser = (User) session.getAttribute("currentUser");
        }
        model.addAttribute("user", currentUser);
        model.addAttribute("currentUser", currentUser);

        List<JobCatalog> list;
        if (keyword != null && !keyword.trim().isEmpty()) {
            list = jobCatalogService.search(keyword.trim());
        } else if (group != null && !group.trim().isEmpty()) {
            list = jobCatalogService.getByGroup(group.trim().toUpperCase());
        } else {
            list = jobCatalogService.getAllActive();
        }

        model.addAttribute("jobList", list);
        model.addAttribute("selectedGroup", group != null ? group : "ALL");
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("totalJobs", list.size());
        return "job_catalog";
    }

    /**
     * REST API tìm kiếm và lấy chi tiết phục vụ autocomplete/modal
     */
    @GetMapping("/api/job-catalog")
    @ResponseBody
    public ResponseEntity<List<JobCatalog>> getJobCatalogApi(@RequestParam(required = false) String group,
                                                             @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ResponseEntity.ok(jobCatalogService.search(keyword.trim()));
        }
        if (group != null && !group.trim().isEmpty() && !"ALL".equalsIgnoreCase(group)) {
            return ResponseEntity.ok(jobCatalogService.getByGroup(group.trim().toUpperCase()));
        }
        return ResponseEntity.ok(jobCatalogService.getAllActive());
    }

    @GetMapping("/api/job-catalog/{id}")
    @ResponseBody
    public ResponseEntity<JobCatalog> getJobCatalogDetailApi(@PathVariable Long id) {
        return jobCatalogService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * API kiểm tra cảnh báo vượt quá giờ định mức
     */
    @PostMapping("/api/job-catalog/check-hours")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkOverHours(@RequestBody Map<String, Object> payload) {
        Map<String, Object> res = new HashMap<>();
        try {
            Long catalogId = Long.parseLong(payload.get("jobCatalogId").toString());
            Double actualHours = Double.parseDouble(payload.get("actualHours").toString());
            
            JobCatalog catalog = jobCatalogService.getById(catalogId).orElse(null);
            if (catalog != null) {
                boolean isOver = actualHours > catalog.getStandardHours();
                res.put("isOverStandard", isOver);
                res.put("standardHours", catalog.getStandardHours());
                res.put("actualHours", actualHours);
                res.put("benchmarkPoint", catalog.getBenchmarkPoint());
                res.put("securityLevel", catalog.getSecurityLevel());
                res.put("isConfidential", catalog.getIsConfidential());
                res.put("warningMessage", isOver ? "Cảnh báo: Thời gian thực hiện (" + actualHours + "h) vượt định mức chuẩn (" + catalog.getStandardHours() + "h)!" : null);
                return ResponseEntity.ok(res);
            }
        } catch (Exception ignored) {}
        
        res.put("isOverStandard", false);
        return ResponseEntity.ok(res);
    }
}

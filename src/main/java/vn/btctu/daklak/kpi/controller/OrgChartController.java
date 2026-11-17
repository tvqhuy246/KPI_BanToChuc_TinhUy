package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.btctu.daklak.kpi.model.Department;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.*;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.UserService;

import java.util.*;

@Controller
public class OrgChartController {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private DepartmentRepository deptRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    public static class DeptBranch {
        private Department department;
        private User deputyLeader;
        private User deptHead;
        private List<User> deputyHeads = new ArrayList<>();
        private List<User> officers = new ArrayList<>();

        public Department getDepartment() { return department; }
        public void setDepartment(Department department) { this.department = department; }
        public User getDeputyLeader() { return deputyLeader; }
        public void setDeputyLeader(User deputyLeader) { this.deputyLeader = deputyLeader; }
        public User getDeptHead() { return deptHead; }
        public void setDeptHead(User deptHead) { this.deptHead = deptHead; }
        public List<User> getDeputyHeads() { return deputyHeads; }
        public void setDeputyHeads(List<User> deputyHeads) { this.deputyHeads = deputyHeads; }
        public List<User> getOfficers() { return officers; }
        public void setOfficers(List<User> officers) { this.officers = officers; }
        public int getTotalStaff() {
            return (deptHead != null ? 1 : 0) + deputyHeads.size() + officers.size();
        }
    }

    @GetMapping("/org-chart")
    public String orgChartPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        model.addAttribute("user", currentUser);

        // Kiểm tra quyền bổ nhiệm/phân cấp: Chỉ Admin hoặc Trưởng ban (Cấp 1)
        boolean canManageRoles = currentUser != null && (
                "ADMIN".equals(currentUser.getRole()) ||
                "LANH_DAO_BAN".equals(currentUser.getRole()) ||
                (currentUser.getOrgLevel() != null && currentUser.getOrgLevel() == 1)
        );
        model.addAttribute("canManageRoles", canManageRoles);

        List<User> allStaff = userRepo.findAllForOrgChart();
        model.addAttribute("allStaff", allStaff);

        List<Department> departments = deptRepo.findAll();
        model.addAttribute("departments", departments);

        List<User> level1 = new ArrayList<>();
        List<User> level2 = new ArrayList<>();
        List<User> level3 = new ArrayList<>();
        List<User> level4 = new ArrayList<>();
        List<User> level5 = new ArrayList<>();

        for (User u : allStaff) {
            if (u.getId() != null && u.getId() == 40) continue;
            int lvl = u.getOrgLevel() != null ? u.getOrgLevel() : 5;
            if (lvl == 1) level1.add(u);
            else if (lvl == 2) level2.add(u);
            else if (lvl == 3) level3.add(u);
            else if (lvl == 4) level4.add(u);
            else level5.add(u);
        }

        model.addAttribute("level1", level1);
        model.addAttribute("level2", level2);
        model.addAttribute("level3", level3);
        model.addAttribute("level4", level4);
        model.addAttribute("level5", level5);

        // Xây dựng 4 nhánh phòng ban chuyên môn chuẩn theo Cây Sơ Đồ BTCTU
        List<DeptBranch> deptBranches = new ArrayList<>();
        for (Department d : departments) {
            if ("LANH_DAO_BAN".equals(d.getCode()) || (d.getId() != null && d.getId() == 1L)) {
                continue;
            }
            DeptBranch branch = new DeptBranch();
            branch.setDepartment(d);

            // Tìm Phó Trưởng ban phụ trách phòng này
            for (User pb : level2) {
                if (pb.getDepartment() != null && pb.getDepartment().getId().equals(d.getId())) {
                    branch.setDeputyLeader(pb);
                    break;
                }
            }
            if (branch.getDeputyLeader() == null && d.getId() != null && d.getId() == 3L) {
                for (User pb : level2) {
                    if (pb.getId() != null && (pb.getId() == 4L || pb.getId() == 2L)) {
                        branch.setDeputyLeader(pb);
                        break;
                    }
                }
            }

            for (User u : allStaff) {
                if (u.getId() != null && u.getId() == 40) continue;
                if (u.getDepartment() != null && u.getDepartment().getId().equals(d.getId())) {
                    int lvl = u.getOrgLevel() != null ? u.getOrgLevel() : 5;
                    if (lvl == 3) {
                        branch.setDeptHead(u);
                    } else if (lvl == 4) {
                        branch.getDeputyHeads().add(u);
                    } else if (lvl == 5) {
                        branch.getOfficers().add(u);
                    }
                }
            }
            deptBranches.add(branch);
        }
        model.addAttribute("deptBranches", deptBranches);

        return "org_chart";
    }

    /**
     * PHÂN CẤP QUYỀN & BỔ NHIỆM CHỨC VỤ CÁN BỘ (CHỈ DÀNH RIÊNG CHO ADMIN HOẶC TRƯỞNG BAN CẤP CAO NHẤT)
     */
    @PostMapping("/org-chart/assign-role")
    public String assignRole(@RequestParam("targetUserId") Long targetUserId,
                             @RequestParam("newRole") String newRole,
                             @RequestParam("newOrgLevel") Integer newOrgLevel,
                             @RequestParam(value = "newManagerId", required = false) Long newManagerId,
                             HttpSession session,
                             RedirectAttributes redirectAttrs,
                             HttpServletRequest request) {

        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra thẩm quyền nghiêm ngặt
        boolean isAuthorized = "ADMIN".equals(currentUser.getRole()) || 
                "LANH_DAO_BAN".equals(currentUser.getRole()) || 
                (currentUser.getOrgLevel() != null && currentUser.getOrgLevel() == 1);

        if (!isAuthorized) {
            redirectAttrs.addFlashAttribute("error", "Đồng chí không có thẩm quyền phân cấp chức vụ! Quyền này chỉ dành cho Trưởng ban hoặc Quản trị viên.");
            return "redirect:/org-chart";
        }

        boolean success = userService.updateUserRoleAndLevel(targetUserId, newRole, newOrgLevel, newManagerId, currentUser);
        if (success) {
            auditLogService.log(
                    currentUser.getId(),
                    currentUser.getUsername(),
                    "ASSIGN_ROLE",
                    "users",
                    targetUserId,
                    "Phân cấp chức vụ cán bộ ID " + targetUserId + " lên vai trò: " + newRole + " (Cấp " + newOrgLevel + ")",
                    request.getRemoteAddr()
            );
            redirectAttrs.addFlashAttribute("success", "Đã phân cấp chức vụ và cập nhật Cây sơ đồ thành công!");
        } else {
            redirectAttrs.addFlashAttribute("error", "Không tìm thấy cán bộ để cập nhật!");
        }

        return "redirect:/org-chart";
    }

    @GetMapping("/api/org-chart/data")
    @ResponseBody
    public Map<String, Object> getOrgChartData() {
        List<User> allStaff = userRepo.findAllForOrgChart();
        Map<String, Object> response = new HashMap<>();

        List<Map<String, Object>> nodes = new ArrayList<>();
        for (User u : allStaff) {
            if (u.getId() != null && u.getId() == 40) continue;
            Map<String, Object> node = new HashMap<>();
            node.put("id", u.getId());
            node.put("name", u.getFullName());
            node.put("title", u.getPosition() != null ? u.getPosition().getTitle() : u.getRoleBadge());
            node.put("role", u.getRole());
            node.put("department", u.getDepartment() != null ? u.getDepartment().getName() : "");
            node.put("deptCode", u.getDepartment() != null ? u.getDepartment().getCode() : "");
            node.put("orgLevel", u.getOrgLevel());
            node.put("scope", u.getAssignedScope());
            node.put("email", u.getEmail());
            node.put("phone", u.getPhone());
            node.put("parentId", u.getManager() != null ? u.getManager().getId() : null);
            node.put("managerName", u.getManager() != null ? u.getManager().getFullName() : null);
            nodes.add(node);
        }

        response.put("total", nodes.size());
        response.put("staff", nodes);
        return response;
    }
}

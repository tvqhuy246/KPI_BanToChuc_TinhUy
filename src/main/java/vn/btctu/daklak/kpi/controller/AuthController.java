package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.btctu.daklak.kpi.model.Department;
import vn.btctu.daklak.kpi.model.Position;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.repository.DepartmentRepository;
import vn.btctu.daklak.kpi.repository.PositionRepository;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private DepartmentRepository deptRepo;

    @Autowired
    private PositionRepository posRepo;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "success", required = false) String success,
                            Model model) {
        if (error != null) {
            model.addAttribute("error", "Tên đăng nhập hoặc mật khẩu không chính xác!");
        }
        if (success != null) {
            model.addAttribute("success", "Đăng ký tài khoản thành công! Đồng chí có thể đăng nhập ngay.");
        }
        return "login";
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               HttpServletRequest request,
                               HttpSession session) {
        Optional<User> uOpt = userService.authenticate(username, password);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            session.setAttribute("CURRENT_USER", user);
            auditLogService.log(user.getId(), user.getUsername(), "LOGIN", "users", user.getId(), "Đăng nhập thành công", request.getRemoteAddr());
            return "redirect:/";
        }
        return "redirect:/login?error=true";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        List<Department> allDepts = deptRepo.findAll();
        List<Department> filteredDepts = new ArrayList<>();
        for (Department d : allDepts) {
            if (!"LANH_DAO_BAN".equals(d.getCode()) && (d.getId() == null || d.getId() != 1L)) {
                filteredDepts.add(d);
            }
        }
        model.addAttribute("departments", filteredDepts);

        List<Position> allPositions = posRepo.findAll();
        List<Position> lowestPositions = new ArrayList<>();
        for (Position p : allPositions) {
            if ("CHUYEN_VIEN".equals(p.getCode()) || "VAN_THU".equals(p.getCode())) {
                lowestPositions.add(p);
            }
        }
        model.addAttribute("positions", lowestPositions);
        return "register";
    }

    @PostMapping("/register")
    public String processRegister(@RequestParam("username") String username,
                                  @RequestParam("password") String password,
                                  @RequestParam("confirmPassword") String confirmPassword,
                                  @RequestParam("fullName") String fullName,
                                  @RequestParam("departmentId") Long departmentId,
                                  @RequestParam(value = "positionId", required = false) Long positionId,
                                  @RequestParam(value = "email", required = false) String email,
                                  @RequestParam(value = "phone", required = false) String phone,
                                  @RequestParam(value = "partyMemberCardId", required = false) String partyMemberCardId,
                                  @RequestParam(value = "citizenId", required = false) String citizenId,
                                  RedirectAttributes redirectAttrs,
                                  HttpServletRequest request) {

        if (!password.equals(confirmPassword)) {
            redirectAttrs.addFlashAttribute("error", "Mật khẩu xác nhận không khớp!");
            return "redirect:/register";
        }

        if (userService.findByUsername(username).isPresent()) {
            redirectAttrs.addFlashAttribute("error", "Tên đăng nhập đã tồn tại trong hệ thống!");
            return "redirect:/register";
        }

        try {
            User newUser = userService.registerUser(username, password, fullName, departmentId, positionId, email, phone, partyMemberCardId, citizenId);
            auditLogService.log(newUser.getId(), newUser.getUsername(), "REGISTER", "users", newUser.getId(), "Đăng ký tài khoản Chuyên viên mới (Cấp 5)", request.getRemoteAddr());
            return "redirect:/login?success=true";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký: " + e.getMessage());
            return "redirect:/register";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam("fullName") String fullName,
                                @RequestParam(value = "email", required = false) String email,
                                @RequestParam(value = "phone", required = false) String phone,
                                @RequestParam(value = "partyMemberCardId", required = false) String partyMemberCardId,
                                @RequestParam(value = "citizenId", required = false) String citizenId,
                                HttpSession session,
                                RedirectAttributes redirectAttrs,
                                HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        boolean updated = userService.updateProfile(currentUser.getId(), fullName, email, phone, partyMemberCardId, citizenId);
        if (updated) {
            userService.findById(currentUser.getId()).ifPresent(u -> session.setAttribute("CURRENT_USER", u));
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "UPDATE_PROFILE", "users", currentUser.getId(), "Cập nhật thông tin lý lịch cán bộ", request.getRemoteAddr());
            redirectAttrs.addFlashAttribute("success", "Cập nhật thông tin lý lịch cán bộ thành công!");
        } else {
            redirectAttrs.addFlashAttribute("error", "Không thể cập nhật hồ sơ cá nhân!");
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 @RequestParam("confirmNewPassword") String confirmNewPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttrs,
                                 HttpServletRequest request) {
        User currentUser = (User) session.getAttribute("CURRENT_USER");
        if (currentUser == null) return "redirect:/login";

        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttrs.addFlashAttribute("error", "Mật khẩu mới xác nhận không khớp!");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/");
        }

        if (newPassword.length() < 6) {
            redirectAttrs.addFlashAttribute("error", "Mật khẩu mới phải có tối thiểu 6 ký tự!");
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer != null ? referer : "/");
        }

        boolean success = userService.changePassword(currentUser.getId(), oldPassword, newPassword);
        if (success) {
            auditLogService.log(currentUser.getId(), currentUser.getUsername(), "CHANGE_PASSWORD", "users", currentUser.getId(), "Đổi mật khẩu tài khoản thành công", request.getRemoteAddr());
            redirectAttrs.addFlashAttribute("success", "Đổi mật khẩu tài khoản thành công!");
        } else {
            redirectAttrs.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác!");
        }
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/");
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        User user = (User) session.getAttribute("CURRENT_USER");
        if (user != null) {
            auditLogService.log(user.getId(), user.getUsername(), "LOGOUT", "users", user.getId(), "Đăng xuất khỏi hệ thống", request.getRemoteAddr());
        }
        session.invalidate();
        return "redirect:/login";
    }
}
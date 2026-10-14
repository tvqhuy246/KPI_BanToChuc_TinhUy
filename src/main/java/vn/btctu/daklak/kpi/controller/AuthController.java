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

    /**
     * Màn hình đăng ký: Chỉ hiển thị các phòng chuyên môn và vị trí Cấp 5 thấp nhất (Chuyên viên, Văn thư)
     */
    @GetMapping("/register")
    public String registerPage(Model model) {
        // Chỉ lấy 4 phòng chuyên môn, loại trừ phòng Lãnh đạo Ban (ID=1)
        List<Department> allDepts = deptRepo.findAll();
        List<Department> filteredDepts = new ArrayList<>();
        for (Department d : allDepts) {
            if (!"LANH_DAO_BAN".equals(d.getCode()) && (d.getId() == null || d.getId() != 1L)) {
                filteredDepts.add(d);
            }
        }
        model.addAttribute("departments", filteredDepts);

        // Chỉ hiển thị các vị trí việc làm Cấp 5 (Chuyên viên, Văn thư), tuyệt đối không hiển thị Trưởng ban, Phó ban, Trưởng phòng, Phó phòng
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
            // UserService khóa cứng việc tạo tài khoản ở Cấp 5 (Chuyên viên / Văn thư)
            User newUser = userService.registerUser(username, password, fullName, departmentId, positionId, email, phone, partyMemberCardId, citizenId);
            auditLogService.log(newUser.getId(), newUser.getUsername(), "REGISTER", "users", newUser.getId(), "Đăng ký tài khoản Chuyên viên mới (Cấp 5)", request.getRemoteAddr());
            return "redirect:/login?success=true";
        } catch (Exception e) {
            redirectAttrs.addFlashAttribute("error", "Có lỗi xảy ra trong quá trình đăng ký: " + e.getMessage());
            return "redirect:/register";
        }
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

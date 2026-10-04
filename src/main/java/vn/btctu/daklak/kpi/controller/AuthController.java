package vn.btctu.daklak.kpi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.btctu.daklak.kpi.model.User;
import vn.btctu.daklak.kpi.service.AuditLogService;
import vn.btctu.daklak.kpi.service.UserService;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @Autowired
    private AuditLogService auditLogService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = error, required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute(error, Tên đăng nhập hoặc mật khẩu không chính xác!);
        }
        return login;
    }

    @PostMapping("/login")
    public String processLogin(@RequestParam(username) String username,
                               @RequestParam(password) String password,
                               HttpServletRequest request,
                               HttpSession session) {
        Optional<User> uOpt = userService.authenticate(username, password);
        if (uOpt.isPresent()) {
            User user = uOpt.get();
            session.setAttribute(CURRENT_USER, user);
            auditLogService.log(user.getId(), user.getUsername(), LOGIN, users, user.getId(), Đăng nhập thành công, request.getRemoteAddr());
            return redirect:/;
        }
        return redirect:/login?error=true;
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, HttpServletRequest request) {
        User user = (User) session.getAttribute(CURRENT_USER);
        if (user != null) {
            auditLogService.log(user.getId(), user.getUsername(), LOGOUT, users, user.getId(), Đăng xuất khỏi hệ thống, request.getRemoteAddr());
        }
        session.invalidate();
        return redirect:/login;
    }
}

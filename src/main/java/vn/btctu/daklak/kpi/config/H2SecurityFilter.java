package vn.btctu.daklak.kpi.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import vn.btctu.daklak.kpi.model.User;

import java.io.IOException;

/**
 * BỘ LỌC BẢO MẬT CẤP SERVLET CONTAINER CHO H2 CONSOLE
 * Chặn đứng mọi truy cập vào /h2-console/** từ cấp Servlet trước khi đến H2 WebServlet.
 * Tuyệt đối chỉ cho phép tài khoản có vai trò ADMIN truy cập.
 */
@Component
@Order(1)
public class H2SecurityFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String uri = httpRequest.getRequestURI();
        if (uri.startsWith("/h2-console")) {
            HttpSession session = httpRequest.getSession(false);
            User user = session != null ? (User) session.getAttribute("CURRENT_USER") : null;
            if (user == null || !"ADMIN".equals(user.getRole())) {
                httpResponse.sendRedirect("/?error=no_permission_admin");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}

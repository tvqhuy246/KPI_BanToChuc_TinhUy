package vn.btctu.daklak.kpi.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (uri.startsWith(/login) || uri.startsWith(/css) || uri.startsWith(/js) || uri.startsWith(/h2-console)) {
            return true;
        }

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(CURRENT_USER) == null) {
            response.sendRedirect(/login);
            return false;
        }
        return true;
    }
}

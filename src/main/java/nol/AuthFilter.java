package nol;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;  
import java.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class AuthFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse res,
                         FilterChain chain) throws IOException, ServletException {

        HttpServletRequest  r  = (HttpServletRequest) req;
        HttpServletResponse resp = (HttpServletResponse) res;
        HttpSession s = r.getSession(false);

        boolean logged = (s != null && s.getAttribute("dni") != null);
        boolean atLogin = r.getRequestURI().endsWith("/login") ||
                          r.getRequestURI().endsWith("/login.html");

        if (logged || atLogin) {
            chain.doFilter(req, res);          // sigue la cadena
        } else {
            resp.sendRedirect(r.getContextPath() + "/login.html");
        }
    }
}


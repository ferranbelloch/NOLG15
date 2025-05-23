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
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  r   = (HttpServletRequest) req;
        HttpServletResponse resp = (HttpServletResponse) res;
        String uri = r.getRequestURI();          //  /NOL_G15  /asignaturas.html
        HttpSession s = r.getSession(false);

        boolean logged  = s != null && s.getAttribute("key") != null;
        boolean publico = uri.endsWith(".html")        // páginas públicas
                       || uri.startsWith(r.getContextPath() + "/css")
                       || uri.startsWith(r.getContextPath() + "/js")
                       || uri.startsWith(r.getContextPath() + "/login");

        if (logged || publico) {
            chain.doFilter(req, res);            // sigue
        } else {
            resp.sendRedirect(r.getContextPath() + "/login.html");
        }
    }
}

package nol;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private Client client;
    private static final String API_BASE_URL = "http://localhost:9090/CentroEducativo";

    @Override
    public void init() {
        client = ClientBuilder.newClient();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String dni  = req.getParameter("dni");
        String pass = req.getParameter("password");
        // Leemos el parámetro oculto "role"
        String role = req.getParameter("role"); // "alumno" o "profesor"
        System.out.println(">>> LoginServlet: Parámetros → dni=" + dni 
                           + ", pass=" + pass + ", role=" + role);

        try {
            // Llamamos al API de login para obtener el token (text/plain)
            Response r = client.target(API_BASE_URL + "/login")
                               .request(MediaType.TEXT_PLAIN)
                               .post(Entity.json("{\"dni\":\""+dni+"\",\"password\":\""+pass+"\"}"));

            System.out.println(">>> LoginServlet: status del API = " + r.getStatus());
            String key = null;
            if (r.getStatus() == 200) {
                key = r.readEntity(String.class);
                System.out.println(">>> LoginServlet: clave recibida = '" + key + "'");
            }

            if (r.getStatus() == 200 && key != null && !key.trim().isEmpty()) {
                // Autenticación correcta
                HttpSession session = req.getSession(true);
                session.setAttribute("key", key);
                session.setAttribute("dni", dni);
                session.setAttribute("role", role); // Guardamos el rol en sesión

                // Redireccionamos según el rol:
                if ("profesor".equals(role)) {
                    resp.sendRedirect(req.getContextPath() + "/asignaturasProfesor.jsp");
                } else {
                    // Si role es "alumno" (o cualquier otro valor), llevamos a asignaturasAlumno.jsp
                    resp.sendRedirect(req.getContextPath() + "/asignaturasAlumno.jsp");
                }
            } else {
                // Credenciales inválidas
                r.close();
                req.setAttribute("errorMsg", "DNI o contraseña incorrectos");
                req.getRequestDispatcher("/login.html").forward(req, resp);
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "Error en el servidor: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}


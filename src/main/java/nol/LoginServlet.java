package nol;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
        // Inicializa Jersey
        client = ClientBuilder.newClient();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String dni = req.getParameter("dni");
        String pass = req.getParameter("password");
        
        if (dni == null || dni.trim().isEmpty() || pass == null || pass.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "DNI y contraseña son requeridos");
            return;
        }

        try {
            // Llamada a la API de login
            Response r = client.target(API_BASE_URL + "/login")
                             .request(MediaType.APPLICATION_JSON)
                             .post(Entity.json("{\"dni\":\""+dni+"\",\"password\":\""+pass+"\"}"));

            if (r.getStatus() == 200) {
                String key = r.readEntity(String.class);
                
                // Verificar que la key no está vacía
                if (key == null || key.trim().isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error en la autenticación");
                    return;
                }

                // Crear sesión y guardar datos
                HttpSession session = req.getSession(true);
                session.setAttribute("key", key);
                session.setAttribute("dni", dni);
                
                // Redirigir directamente a asignaturas
                resp.sendRedirect(req.getContextPath() + "/asignaturas");
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Credenciales inválidas");
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


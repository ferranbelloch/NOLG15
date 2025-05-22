package nol;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;

@WebServlet("/asignaturas")
public class AsignaturasServlet extends HttpServlet {
    private Client client;

    @Override
    public void init() {
        client = ClientBuilder.newClient();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) 
            throws ServletException, IOException {
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("key") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        String key = (String) session.getAttribute("key");
        
        try {
            // Obtener las asignaturas del alumno desde CentroEducativo
            Response response = client.target("http://localhost:9090/CentroEducativo/alumnosyasignaturas")
                                    .queryParam("key", key)
                                    .request(MediaType.APPLICATION_JSON)
                                    .get();

            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                // Procesar el JSON y preparar los datos para la vista
                req.setAttribute("asignaturas", jsonResponse);
                req.getRequestDispatcher("asignaturas.jsp").forward(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error al obtener las asignaturas");
            }
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor");
        }
    }

    @Override
    public void destroy() {
        client.close();
    }
}

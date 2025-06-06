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
        String dni  = req.getParameter("dni");
        String pass = req.getParameter("password");
        System.out.println(">>> LoginServlet: Parámetros → dni=" + dni + ", pass=" + pass);

        try {
            // Cambiamos el Accept a TEXT_PLAIN porque el backend devuelve token en text/plain
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
                HttpSession session = req.getSession(true);
                session.setAttribute("key", key);
                session.setAttribute("dni", dni);
                resp.sendRedirect(req.getContextPath() + "/asignaturas.jsp");
            } else {
                // Si el status no es 200 o la key está vacía, reenvía al login con mensaje de error
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
}


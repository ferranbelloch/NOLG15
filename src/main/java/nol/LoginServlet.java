package nol;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;


@WebServlet("/login")
public class LoginServlet extends HttpServlet {
    private Client client;

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

        // Llamada a la API
        Response r = client.target("http://localhost:9090/CentroEducativo/login")
                           .request()
                           .post(Entity.json(
                                "{\"dni\":\""+dni+"\",\"password\":\""+pass+"\"}"));

        if (r.getStatus() == 200) {
            String key = r.readEntity(String.class);
            req.getSession().setAttribute("key", key);
            resp.sendRedirect("asignaturas.html");
        } else {
            resp.sendError(401, "Credenciales inválidas");
        }
    }

    @Override
    public void destroy() {
        client.close();
    }
}


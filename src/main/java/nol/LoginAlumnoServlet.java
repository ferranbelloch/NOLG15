package nol;

import com.google.gson.JsonObject;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@WebServlet(name = "LoginAlumnoServlet", urlPatterns = "/loginAlumno")
public class LoginAlumnoServlet extends HttpServlet {
    private static final String API_LOGIN_URL = "http://localhost:9090/CentroEducativo/login";
    private Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String dni = req.getParameter("dni");
        String password = req.getParameter("password");
        if (dni == null || password == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Faltan credenciales");
            return;
        }

        // Preparar JSON de login
        JsonObject loginJson = new JsonObject();
        loginJson.addProperty("dni", dni);
        loginJson.addProperty("password", password);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(API_LOGIN_URL);
            post.setHeader("Content-Type", "application/json");
            post.setEntity(new StringEntity(gson.toJson(loginJson)));

            try (CloseableHttpResponse apiResp = client.execute(post)) {
                int status = apiResp.getStatusLine().getStatusCode();
                if (status == 200) {
                    // Leer cuerpo con la key
                    BufferedReader reader = new BufferedReader(
                        new InputStreamReader(apiResp.getEntity().getContent()));
                    String key = reader.readLine().trim();
                    // Guardar en sesión
                    HttpSession session = req.getSession(true);
                    session.setAttribute("dni", dni);
                    session.setAttribute("password", password);
                    session.setAttribute("key", key);
                    // Redirigir a la lista de asignaturas del alumno
                    resp.sendRedirect(req.getContextPath() + "/alumno/asignaturas");
                } else {
                    // Login fallido
                    req.setAttribute("error", "Credenciales incorrectas");
                    req.getRequestDispatcher("/index.html").forward(req, resp);
                }
            }
        }
    }
}
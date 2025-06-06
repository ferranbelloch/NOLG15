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
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/asignaturas")
public class AsignaturasServlet extends HttpServlet {
    private Client client;
    private Gson gson;
    private static final String API_BASE_URL = "http://localhost:9090/CentroEducativo";

    @Override
    public void init() {
        client = ClientBuilder.newClient();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        // 1. Validar sesión y token
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("key") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        String key = (String) session.getAttribute("key");
        String dni = (String) session.getAttribute("dni");

        // 1.1. Si el token es "-1" (login inválido), invalidamos la sesión y redirigimos
        if ("-1".equals(key)) {
            session.invalidate();
            req.setAttribute("errorMsg", "Credenciales inválidas. Vuelve a intentarlo.");
            req.getRequestDispatcher("/login.html").forward(req, resp);
            return;
        }

        try {
            // 2. Obtener información del alumno
            WebTarget alumnoTarget = client.target(API_BASE_URL)
                                           .path("/alumnos/" + dni);
            Invocation.Builder alumnoInvocation = alumnoTarget.request(MediaType.APPLICATION_JSON)
                                                              .header("Authorization", "Bearer " + key);
            Response alumnoResponse = alumnoInvocation.get();

            if (alumnoResponse.getStatus() == 401) {
                // Token no autorizado o expirado
                alumnoResponse.close();
                session.invalidate();
                req.setAttribute("errorMsg", "Sesión inválida o caducada. Por favor, inicia sesión de nuevo.");
                req.getRequestDispatcher("/login.html").forward(req, resp);
                return;
            }

            if (alumnoResponse.getStatus() != 200) {
                int code = alumnoResponse.getStatus();
                alumnoResponse.close();
                throw new ServletException("Error al obtener información del alumno (código " + code + ")");
            }

            JsonObject alumnoData = gson.fromJson(
                    alumnoResponse.readEntity(String.class),
                    JsonObject.class
            );
            alumnoResponse.close();

            // 3. Obtener lista de acrónimos de asignaturas del alumno
            WebTarget asigListTarget = client.target(API_BASE_URL)
                                             .path("/alumnos/" + dni + "/asignaturas");
            Invocation.Builder asigListInvocation = asigListTarget.request(MediaType.APPLICATION_JSON)
                                                                  .header("Authorization", "Bearer " + key);
            Response asigListResponse = asigListInvocation.get();

            if (asigListResponse.getStatus() == 401) {
                // Token inválido al pedir lista de asignaturas
                asigListResponse.close();
                session.invalidate();
                req.setAttribute("errorMsg", "Sesión inválida o caducada. Por favor, inicia sesión de nuevo.");
                req.getRequestDispatcher("/login.html").forward(req, resp);
                return;
            }

            if (asigListResponse.getStatus() != 200) {
                int code = asigListResponse.getStatus();
                asigListResponse.close();
                throw new ServletException("Error al obtener lista de asignaturas (código " + code + ")");
            }

            JsonArray asignaturasArray = gson.fromJson(
                    asigListResponse.readEntity(String.class),
                    JsonArray.class
            );
            asigListResponse.close();

            // 4. Para cada acrónimo, solicitar detalle + grupo + miembros
            List<Map<String, Object>> asignaturasInfo = new ArrayList<>();
            for (JsonElement asigElement : asignaturasArray) {
                String acronimo = asigElement.getAsString();

                // 4.1 Detalle de la asignatura
                WebTarget detalleTarget = client.target(API_BASE_URL)
                                                .path("/asignaturas/" + acronimo);
                Invocation.Builder detalleInvocation = detalleTarget.request(MediaType.APPLICATION_JSON)
                                                                    .header("Authorization", "Bearer " + key);
                Response detalleResponse = detalleInvocation.get();

                if (detalleResponse.getStatus() != 200) {
                    detalleResponse.close();
                    // Si falla el detalle, seguimos con la siguiente asignatura
                    continue;
                }

                JsonObject asigDetalle = gson.fromJson(
                        detalleResponse.readEntity(String.class),
                        JsonObject.class
                );
                detalleResponse.close();

                Map<String, Object> asignaturaMap = new HashMap<>();
                asignaturaMap.put("codigo",       asigDetalle.get("acronimo").getAsString());
                asignaturaMap.put("nombre",       asigDetalle.get("nombre").getAsString());
                asignaturaMap.put("curso",        asigDetalle.get("curso").getAsInt());
                asignaturaMap.put("cuatrimestre", asigDetalle.get("cuatrimestre").getAsString());
                asignaturaMap.put("creditos",     asigDetalle.get("creditos").getAsDouble());

                // 4.2 Consultar grupos de esa asignatura
                WebTarget grupoTarget = client.target(API_BASE_URL)
                                              .path("/asignaturas/" + acronimo + "/grupos");
                Invocation.Builder grupoInvocation = grupoTarget.request(MediaType.APPLICATION_JSON)
                                                                .header("Authorization", "Bearer " + key);
                Response grupoResponse = grupoInvocation.get();

                if (grupoResponse.getStatus() == 200) {
                    JsonArray grupos = gson.fromJson(
                            grupoResponse.readEntity(String.class),
                            JsonArray.class
                    );
                    grupoResponse.close();

                    if (grupos.size() > 0) {
                        // Tomamos el primer grupo
                        JsonObject primerGrupo = grupos.get(0).getAsJsonObject();
                        String nombreGrupo = primerGrupo.get("nombre").getAsString();
                        asignaturaMap.put("grupoNombre", "Grupo " + nombreGrupo);

                        // 4.3 Consultar miembros del grupo
                        WebTarget miembrosTarget = client.target(API_BASE_URL)
                                                         .path("/asignaturas/" + acronimo
                                                               + "/grupos/" + nombreGrupo + "/alumnos");
                        Invocation.Builder miembrosInvocation = miembrosTarget.request(MediaType.APPLICATION_JSON)
                                                                            .header("Authorization", "Bearer " + key);
                        Response miembrosResponse = miembrosInvocation.get();

                        List<String> miembrosList = new ArrayList<>();
                        if (miembrosResponse.getStatus() == 200) {
                            JsonArray miembrosArray = gson.fromJson(
                                    miembrosResponse.readEntity(String.class),
                                    JsonArray.class
                            );
                            miembrosResponse.close();

                            for (JsonElement mElem : miembrosArray) {
                                JsonObject miembro = mElem.getAsJsonObject();
                                String nombreCompleto = miembro.get("nombre").getAsString()
                                                       + " " +
                                                       miembro.get("apellidos").getAsString();
                                miembrosList.add(nombreCompleto);
                            }
                        } else {
                            miembrosResponse.close();
                        }
                        asignaturaMap.put("miembros", miembrosList);
                    }
                } else {
                    grupoResponse.close();
                }

                asignaturasInfo.add(asignaturaMap);
            }

            // 5. Preparar datos para la JSP
            String asignaturasJson = gson.toJson(asignaturasInfo);
            req.setAttribute("asignaturasData", asignaturasJson);
            String nombreAlumno = alumnoData.get("nombre").getAsString()
                                 + " " +
                                 alumnoData.get("apellidos").getAsString();
            req.setAttribute("nombreAlumno", nombreAlumno);
            req.setAttribute("dniAlumno", dni);

            // 6. Forward a la JSP
            req.getRequestDispatcher("/asignaturas.jsp").forward(req, resp);

        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                           "Error al procesar la solicitud: " + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}
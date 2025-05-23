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
        
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("key") == null) {
            resp.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }
        
        String key = (String) session.getAttribute("key");
        String dni = (String) session.getAttribute("dni");
        
        try {
            // Obtener información del alumno
            Response alumnoResponse = client.target(API_BASE_URL + "/alumnos/" + dni)
                                    .queryParam("key", key)
                                    .request(MediaType.APPLICATION_JSON)
                                    .get();
            
            if (alumnoResponse.getStatus() != 200) {
                throw new ServletException("Error al obtener información del alumno");
            }
            
            JsonObject alumnoData = gson.fromJson(alumnoResponse.readEntity(String.class), JsonObject.class);
            
            // Obtener asignaturas del alumno
            Response asignaturasResponse = client.target(API_BASE_URL + "/alumnos/" + dni + "/asignaturas")
                                               .queryParam("key", key)
                                               .request(MediaType.APPLICATION_JSON)
                                               .get();
            
            if (asignaturasResponse.getStatus() != 200) {
                throw new ServletException("Error al obtener asignaturas del alumno");
            }
            
            JsonArray asignaturasArray = gson.fromJson(asignaturasResponse.readEntity(String.class), JsonArray.class);
            List<Map<String, Object>> asignaturasInfo = new ArrayList<>();
            
            // Obtener detalles de cada asignatura
            for (JsonElement asigElement : asignaturasArray) {
                String acronimo = asigElement.getAsString();
                Response asigDetalleResponse = client.target(API_BASE_URL + "/asignaturas/" + acronimo)
                                                   .queryParam("key", key)
                                                   .request(MediaType.APPLICATION_JSON)
                                                   .get();
                
                if (asigDetalleResponse.getStatus() == 200) {
                    JsonObject asigDetalle = gson.fromJson(asigDetalleResponse.readEntity(String.class), JsonObject.class);
                    Map<String, Object> asignaturaMap = new HashMap<>();
                    asignaturaMap.put("codigo", asigDetalle.get("acronimo").getAsString());
                    asignaturaMap.put("nombre", asigDetalle.get("nombre").getAsString());
                    asignaturaMap.put("curso", asigDetalle.get("curso").getAsInt());
                    asignaturaMap.put("cuatrimestre", asigDetalle.get("cuatrimestre").getAsString());
                    asignaturaMap.put("creditos", asigDetalle.get("creditos").getAsDouble());
                    
                    // Obtener información del grupo
                    Response grupoResponse = client.target(API_BASE_URL + "/asignaturas/" + acronimo + "/grupos")
                                                 .queryParam("key", key)
                                                 .request(MediaType.APPLICATION_JSON)
                                                 .get();
                    
                    if (grupoResponse.getStatus() == 200) {
                        JsonArray grupos = gson.fromJson(grupoResponse.readEntity(String.class), JsonArray.class);
                        if (grupos.size() > 0) {
                            JsonObject primerGrupo = grupos.get(0).getAsJsonObject();
                            asignaturaMap.put("grupoNombre", "Grupo " + primerGrupo.get("nombre").getAsString());
                            
                            // Obtener miembros del grupo
                            List<String> miembros = new ArrayList<>();
                            Response miembrosResponse = client.target(API_BASE_URL + "/asignaturas/" + acronimo + 
                                                                    "/grupos/" + primerGrupo.get("nombre").getAsString() + "/alumnos")
                                                            .queryParam("key", key)
                                                            .request(MediaType.APPLICATION_JSON)
                                                            .get();
                            
                            if (miembrosResponse.getStatus() == 200) {
                                JsonArray miembrosArray = gson.fromJson(miembrosResponse.readEntity(String.class), JsonArray.class);
                                for (JsonElement miembroElement : miembrosArray) {
                                    JsonObject miembro = miembroElement.getAsJsonObject();
                                    miembros.add(miembro.get("nombre").getAsString() + " " + 
                                               miembro.get("apellidos").getAsString());
                                }
                            }
                            asignaturaMap.put("miembros", miembros);
                        }
                    }
                    
                    asignaturasInfo.add(asignaturaMap);
                }
            }
            
            // Preparar datos para la JSP
            req.setAttribute("asignaturasData", gson.toJson(asignaturasInfo));
            req.setAttribute("nombreAlumno", alumnoData.get("nombre").getAsString() + " " + 
                                          alumnoData.get("apellidos").getAsString());
            req.setAttribute("dniAlumno", dni);
            
            req.getRequestDispatcher("asignaturas.jsp").forward(req, resp);
            
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

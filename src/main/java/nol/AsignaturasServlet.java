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
            // Obtener información del alumno y sus asignaturas
            Response response = client.target("http://localhost:9090/CentroEducativo/alumnosyasignaturas")
                                    .queryParam("key", key)
                                    .request(MediaType.APPLICATION_JSON)
                                    .get();
            
            if (response.getStatus() == 200) {
                String jsonResponse = response.readEntity(String.class);
                
                // Procesar el JSON para extraer las asignaturas del alumno actual
                List<Map<String, Object>> asignaturasAlumno = procesarAsignaturasAlumno(jsonResponse, dni);
                
                // Obtener el nombre del alumno
                String nombreAlumno = obtenerNombreAlumno(jsonResponse, dni);
                
                // Preparar datos para la JSP
                req.setAttribute("asignaturasData", gson.toJson(asignaturasAlumno));
                req.setAttribute("nombreAlumno", nombreAlumno);
                req.setAttribute("dniAlumno", dni);
                
                req.getRequestDispatcher("asignaturas.jsp").forward(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Error al obtener las asignaturas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error interno del servidor: " + e.getMessage());
        }
    }
    
    private List<Map<String, Object>> procesarAsignaturasAlumno(String jsonResponse, String dniAlumno) {
        List<Map<String, Object>> asignaturasAlumno = new ArrayList<>();
        
        try {
            JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray alumnos = root.getAsJsonArray("alumnos");
            JsonArray asignaturas = root.getAsJsonArray("asignaturas");
            
            // Buscar el alumno por DNI
            JsonObject alumnoActual = null;
            for (JsonElement alumnoElement : alumnos) {
                JsonObject alumno = alumnoElement.getAsJsonObject();
                if (dniAlumno.equals(alumno.get("dni").getAsString())) {
                    alumnoActual = alumno;
                    break;
                }
            }
            
            if (alumnoActual != null && alumnoActual.has("asignaturas")) {
                JsonArray asignaturasArray = alumnoActual.getAsJsonArray("asignaturas");
                
                // Para cada asignatura del alumno, buscar los detalles completos
                for (JsonElement asigElement : asignaturasArray) {
                    String acronimoAsignatura = asigElement.getAsString();
                    
                    // Buscar detalles de la asignatura
                    for (JsonElement asigDetalleElement : asignaturas) {
                        JsonObject asigDetalle = asigDetalleElement.getAsJsonObject();
                        if (acronimoAsignatura.equals(asigDetalle.get("acronimo").getAsString())) {
                            Map<String, Object> asignatura = new HashMap<>();
                            asignatura.put("codigo", asigDetalle.get("acronimo").getAsString());
                            asignatura.put("nombre", asigDetalle.get("nombre").getAsString());
                            asignatura.put("curso", asigDetalle.get("curso").getAsInt());
                            asignatura.put("cuatrimestre", asigDetalle.get("cuatrimestre").getAsString());
                            asignatura.put("creditos", asigDetalle.get("creditos").getAsDouble());
                            
                            // Generar información adicional para visualización
                            asignatura.put("grupoNombre", "Grupo " + generarGrupo(acronimoAsignatura));
                            asignatura.put("miembros", generarMiembros(acronimoAsignatura));
                            
                            asignaturasAlumno.add(asignatura);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return asignaturasAlumno;
    }
    
    private String obtenerNombreAlumno(String jsonResponse, String dniAlumno) {
        try {
            JsonObject root = gson.fromJson(jsonResponse, JsonObject.class);
            JsonArray alumnos = root.getAsJsonArray("alumnos");
            
            for (JsonElement alumnoElement : alumnos) {
                JsonObject alumno = alumnoElement.getAsJsonObject();
                if (dniAlumno.equals(alumno.get("dni").getAsString())) {
                    return alumno.get("nombre").getAsString() + " " + alumno.get("apellidos").getAsString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Usuario";
    }
    
    private String generarGrupo(String acronimo) {
        // Generar grupo basado en el acrónimo de la asignatura
        switch (acronimo) {
            case "DEW": return "A";
            case "DCU": return "B";
            case "IAP": return "C";
            default: return "A";
        }
    }
    
    private List<String> generarMiembros(String acronimo) {
        // Generar lista de miembros ficticios para cada grupo
        List<String> miembros = new ArrayList<>();
        switch (acronimo) {
            case "DEW":
                miembros.add("Ana Martínez López");
                miembros.add("Carlos Rodríguez García");
                miembros.add("María Sánchez Pérez");
                miembros.add("Juan Pérez Martín");
                break;
            case "DCU":
                miembros.add("Pedro González Ruiz");
                miembros.add("Laura Fernández Torres");
                miembros.add("Diego Martín Castro");
                miembros.add("Carmen López Vega");
                break;
            case "IAP":
                miembros.add("Isabel Ruiz Díaz");
                miembros.add("Roberto Díaz Moreno");
                miembros.add("Elena Castro Jiménez");
                miembros.add("Miguel Ángel Vega Ruiz");
                break;
            default:
                miembros.add("Estudiante 1");
                miembros.add("Estudiante 2");
                miembros.add("Estudiante 3");
                break;
        }
        return miembros;
    }
    
    @Override
    public void destroy() {
        if (client != null) {
            client.close();
        }
    }
}

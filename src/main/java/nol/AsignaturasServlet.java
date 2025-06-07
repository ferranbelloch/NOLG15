
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
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.JsonReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
                
                // Crear JSON para la JSP usando JSON-P
                JsonArray asignaturasJson = convertirListaAJsonArray(asignaturasAlumno);
                
                // Preparar datos para la JSP
                req.setAttribute("asignaturasData", asignaturasJson.toString());
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
            JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
            JsonObject root = jsonReader.readObject();
            jsonReader.close();
            
            JsonArray alumnos = root.getJsonArray("alumnos");
            JsonArray asignaturas = root.getJsonArray("asignaturas");
            
            // Buscar el alumno por DNI
            JsonObject alumnoActual = null;
            for (JsonValue alumnoValue : alumnos) {
                JsonObject alumno = alumnoValue.asJsonObject();
                if (dniAlumno.equals(alumno.getString("dni"))) {
                    alumnoActual = alumno;
                    break;
                }
            }
            
            if (alumnoActual != null && alumnoActual.containsKey("asignaturas")) {
                JsonArray asignaturasArray = alumnoActual.getJsonArray("asignaturas");
                
                // Para cada asignatura del alumno, buscar los detalles completos
                for (JsonValue asigValue : asignaturasArray) {
                    String acronimoAsignatura = asigValue.toString().replace("\"", "");
                    
                    // Buscar detalles de la asignatura
                    for (JsonValue asigDetalleValue : asignaturas) {
                        JsonObject asigDetalle = asigDetalleValue.asJsonObject();
                        if (acronimoAsignatura.equals(asigDetalle.getString("acronimo"))) {
                            Map<String, Object> asignatura = new HashMap<>();
                            asignatura.put("codigo", asigDetalle.getString("acronimo"));
                            asignatura.put("nombre", asigDetalle.getString("nombre"));
                            asignatura.put("curso", asigDetalle.getInt("curso"));
                            asignatura.put("cuatrimestre", asigDetalle.getString("cuatrimestre"));
                            asignatura.put("creditos", asigDetalle.getJsonNumber("creditos").doubleValue());
                            
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
            JsonReader jsonReader = Json.createReader(new StringReader(jsonResponse));
            JsonObject root = jsonReader.readObject();
            jsonReader.close();
            
            JsonArray alumnos = root.getJsonArray("alumnos");
            
            for (JsonValue alumnoValue : alumnos) {
                JsonObject alumno = alumnoValue.asJsonObject();
                if (dniAlumno.equals(alumno.getString("dni"))) {
                    return alumno.getString("nombre") + " " + alumno.getString("apellidos");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Usuario";
    }

    private JsonArray convertirListaAJsonArray(List<Map<String, Object>> lista) {
        jakarta.json.JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        
        for (Map<String, Object> item : lista) {
            jakarta.json.JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            
            for (Map.Entry<String, Object> entry : item.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                
                if (value instanceof String) {
                    objectBuilder.add(key, (String) value);
                } else if (value instanceof Number) {
                    if (value instanceof Integer) {
                        objectBuilder.add(key, (Integer) value);
                    } else if (value instanceof Double) {
                        objectBuilder.add(key, (Double) value);
                    }
                } else if (value instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> stringList = (List<String>) value;
                    jakarta.json.JsonArrayBuilder listBuilder = Json.createArrayBuilder();
                    for (String item2 : stringList) {
                        listBuilder.add(item2);
                    }
                    objectBuilder.add(key, listBuilder);
                }
            }
            
            arrayBuilder.add(objectBuilder);
        }
        
        return arrayBuilder.build();
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
=======
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


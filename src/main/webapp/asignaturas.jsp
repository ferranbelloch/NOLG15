<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="com.google.gson.Gson" %>
<%@ page import="com.google.gson.reflect.TypeToken" %>
<%@ page import="java.lang.reflect.Type" %>
<!DOCTYPE html>
<html lang="es">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="./css/asignaturas.css">
    <title>Asignaturas NOL_G15</title>
</head>
<body>
    <div class="container mt-4">
        <!-- Header con información del alumno -->
        <div class="header mb-4">
            <div class="d-flex justify-content-between align-items-center">
                <h1 class="text-primary">
                    Asignaturas del alumno: 
                    <span id="nombreAlumno" class="text-secondary">
                        ${nombreAlumno != null ? nombreAlumno : "Usuario"}
                    </span>
                </h1>
                <div>
                    <a href="certificado?dni=${dniAlumno}" class="btn btn-success me-2">
                        <i class="bi bi-file-earmark-pdf"></i> Certificado
                    </a>
                    <a href="logout" class="btn btn-outline-danger">
                        <i class="bi bi-box-arrow-right"></i> Cerrar Sesión
                    </a>
                </div>
            </div>
        </div>

        <!-- Grid de asignaturas -->
        <div class="asignaturas-grid row">
            <%
                // Obtener los datos del servlet
                String asignaturasJson = (String) request.getAttribute("asignaturasData");
                String nombreAlumno = (String) request.getAttribute("nombreAlumno");
                String dniAlumno = (String) request.getAttribute("dniAlumno");
                
                if (asignaturasJson != null && !asignaturasJson.isEmpty()) {
                    // Parsear el JSON usando Gson
                    Gson gson = new Gson();
                    Type listType = new TypeToken<List<Map<String, Object>>>(){}.getType();
                    List<Map<String, Object>> asignaturas = gson.fromJson(asignaturasJson, listType);
                    
                    if (asignaturas != null && !asignaturas.isEmpty()) {
                        // Iterar sobre las asignaturas
                        for (Map<String, Object> asignatura : asignaturas) {
                            String codigo = (String) asignatura.get("codigo");
                            String nombre = (String) asignatura.get("nombre");
                            String grupoNombre = (String) asignatura.get("grupoNombre");
                            List<String> miembros = (List<String>) asignatura.get("miembros");
                            Double creditos = (Double) asignatura.get("creditos");
                            Integer curso = asignatura.get("curso") instanceof Double ? 
                                ((Double) asignatura.get("curso")).intValue() : (Integer) asignatura.get("curso");
                            String cuatrimestre = (String) asignatura.get("cuatrimestre");
            %>
                        <!-- Card de Asignatura -->
                        <div class="col-md-4 mb-4">
                            <div class="card asignatura-card h-100 shadow-sm">
                                <div class="card-header bg-primary text-white">
                                    <h5 class="card-title mb-0">
                                        <i class="bi bi-book"></i> <%= nombre %>
                                    </h5>
                                </div>
                                <div class="card-body">
                                    <div class="mb-3">
                                        <p class="card-text">
                                            <strong>Código:</strong> <%= codigo %><br>
                                            <strong>Curso:</strong> <%= curso %>º<br>
                                            <strong>Cuatrimestre:</strong> <%= cuatrimestre %><br>
                                            <strong>Créditos:</strong> <%= creditos %>
                                        </p>
                                    </div>
                                    
                                    <div class="grupo-info bg-light p-3 rounded">
                                        <h6 class="text-primary">
                                            <i class="bi bi-people"></i> <%= grupoNombre != null ? grupoNombre : "Grupo" %>
                                        </h6>
                                        <ul class="list-unstyled mb-0">
                                            <%
                                                if (miembros != null && !miembros.isEmpty()) {
                                                    for (String miembro : miembros) {
                                            %>
                                                        <li class="mb-1">
                                                            <i class="bi bi-person-fill text-muted"></i> <%= miembro %>
                                                        </li>
                                            <%
                                                    }
                                                } else {
                                            %>
                                                    <li class="text-muted">
                                                        <i class="bi bi-exclamation-circle"></i> No hay miembros asignados
                                                    </li>
                                            <%
                                                }
                                            %>
                                        </ul>
                                    </div>
                                </div>
                                <div class="card-footer bg-transparent">
                                    <a href="alumno-detalle?asignatura=<%= codigo %>" 
                                       class="btn btn-primary w-100">
                                        <i class="bi bi-eye"></i> Ver Calificaciones
                                    </a>
                                </div>
                            </div>
                        </div>
            <%
                        }
                    } else {
            %>
                        <div class="col-12">
                            <div class="alert alert-warning text-center">
                                <i class="bi bi-exclamation-triangle"></i>
                                <h4>No hay asignaturas disponibles</h4>
                                <p>No se encontraron asignaturas para este alumno.</p>
                            </div>
                        </div>
            <%
                    }
                } else {
            %>
                    <div class="col-12">
                        <div class="alert alert-danger text-center">
                            <i class="bi bi-x-circle"></i>
                            <h4>Error al cargar las asignaturas</h4>
                            <p>No se pudieron obtener los datos de las asignaturas.</p>
                            <a href="<%= request.getContextPath() %>/asignaturas" class="btn btn-primary">
                                <i class="bi bi-arrow-clockwise"></i> Reintentar
                            </a>
                        </div>
                    </div>
            <%
                }
            %>
        </div>

        <!-- Footer -->
        <footer class="bg-light text-center py-3 mt-5 border-top">
            <div class="container">
                <p class="mb-0 text-muted">
                    <strong>NOL_G15:</strong> Arnau Vila, Ferran Belloch, Álvaro Gómez, Mario Pérez
                </p>
                <small class="text-muted">Desarrollo de Aplicaciones Web - 2024/2025</small>
            </div>
        </footer>
    </div>

    <!-- Bootstrap JS y dependencias -->
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
    <!-- Bootstrap Icons -->
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</body>
</html>

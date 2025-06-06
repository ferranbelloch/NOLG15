# ACTA DE REUNIÓN - PROYECTO NOL_G15

**Fecha:** Domingo, 26 de mayo de 2025  
**Lugar:** Reunión telemática por Discord  
**Asistentes:** Arnau Vila, Nacho Pinazo, Álvaro Gómez, Ferran Belloch, Mario Pérez

---

## Orden del día:

- Evaluación de la entrega anterior y análisis de correcciones necesarias  
- Establecimiento de objetivos para la entrega final del domingo 8 de junio  
- Organización del trabajo sin reparto formal de tareas  
- Planificación de sesiones conjuntas (presenciales o en llamada)  
- Revisión del estado actual del repositorio y de los problemas técnicos aún abiertos

---

## Desarrollo de la reunión:

En esta octava sesión, el grupo se reunió con el objetivo de **reforzar la estrategia de trabajo conjunto** para abordar la entrega final del proyecto NOL_G15. Tras evaluar el feedback y resultados del primer hito, se concluyó que era necesario mejorar varios aspectos críticos del sistema, en especial aquellos relacionados con la integración del backend y la experiencia del rol del profesor.

Se acordó que **no se repartirán tareas de forma individual**, sino que todo el equipo trabajará en conjunto, aprovechando sesiones presenciales y videollamadas para facilitar el desarrollo colaborativo y la resolución rápida de incidencias.

Durante la reunión se analizaron los siguientes puntos clave:

- **Revisión de la entrega anterior**, destacando fortalezas (estructura frontend) y debilidades (problemas de conexión JSP-servlets, parcial integración con CentroEducativo, falta de AJAX en zonas requeridas).
- Se establecieron los **objetivos concretos para la entrega final**, entre los que se incluyen:
  - Implementación completa del **rol de profesor**, con listados de asignaturas y alumnos usando AJAX.
  - Visualización y modificación de **notas** de alumnos, con persistencia local (mientras dure la sesión).
  - Generación de páginas dinámicas con datos obtenidos vía **servlets y llamadas REST**.
  - **Estilo visual uniforme** con Bootstrap 5 y estructura de navegación consistente.
  - **Revisión del filtro de logs** para asegurar su correcta activación en producción.
- Se realizó una **revisión técnica del repositorio**, comprobando:
  - Correcta incorporación de las librerías necesarias (`jersey-client`, `jakarta.ws.rs`, etc.).
  - Que todos los miembros pueden ejecutar Tomcat sin conflictos de puertos o rutas.
  - La estructura de carpetas está unificada y los JSP están siendo migrados correctamente desde HTML.

---

## Acuerdos:

1. **El grupo trabajará de forma conjunta**, evitando la fragmentación del desarrollo.  
2. Se realizarán sesiones **presenciales o por Discord** durante la semana para avanzar de forma coordinada.  
3. Se marcan como **prioritarios** los módulos de profesor y la integración dinámica con CentroEducativo.  
4. Todos deben tener los entornos operativos, con los **logs funcionando**, el **login activo**, y conexión correcta con el backend REST.  


---

## Observaciones adicionales:

- Se hace énfasis en dejar preparado el **script de carga de datos** de CentroEducativo para pruebas automáticas.   
- Se recuerda que las **actas deben estar completas** para la entrega final

---

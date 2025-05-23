# ACTA DE REUNIÓN - PROYECTO NOL_G15

**Fecha:** Martes, 20 de mayo de 2025  
**Lugar:** Reunión telemática por Discord  
**Asistentes:** Arnau Vila, Nacho Pinazo, Álvaro Gómez, Ferran Belloch, Mario Pérez

---

## Orden del día:

- Investigación sobre la conexión entre HTML y servlets  
- Pruebas de funcionamiento del servidor Tomcat  
- Resolución de errores de despliegue y mapeo

---

## Desarrollo de la reunión:

La sexta sesión del proyecto NOL_G15 se realizó de forma telemática a través de Discord, con la asistencia de todos los miembros del grupo. El objetivo principal fue **comprender e implementar la conexión entre los archivos HTML y los servlets Java**, mediante el uso de formularios y peticiones HTTP gestionadas por el servidor Tomcat.

Durante la reunión se compartieron distintos recursos y ejemplos de cómo configurar correctamente los servlets, los archivos `web.xml` y las rutas necesarias para permitir una comunicación fluida entre la interfaz de usuario y el backend.

Sin embargo, surgieron diversas **dificultades técnicas**:

- En la **máquina de Arnau**, Tomcat no logró ejecutarse correctamente a pesar de varios intentos de reconfiguración.
- En el resto de máquinas, el servidor **sí se lanzó con éxito**, pero se presentaron errores al intentar acceder a los servlets desde los formularios HTML. En concreto, **los nombres de los endpoints no se resolvían adecuadamente**, provocando respuestas de error 404.
- Se detectaron posibles errores en el archivo `web.xml` relacionados con el mapeo de URLs y la ubicación de los archivos `.class`.

Durante la sesión se propusieron distintas soluciones y se realizaron pruebas en directo para identificar el origen del problema. Se planteó revisar:

- Las rutas relativas y absolutas utilizadas en los formularios HTML.  
- El nombre del contexto (`context path`) configurado en Tomcat.  
- La estructura de carpetas del proyecto (`/WEB-INF`, `/classes`, etc.).

---

## Acuerdos:

1. Cada subgrupo revisará de forma interna sus configuraciones de despliegue y estructura de carpetas.  
2. Arnau intentará reinstalar o reconfigurar su instancia de Tomcat, con apoyo del resto del grupo.  
3. Se centralizará la documentación y ejemplos funcionales en un documento compartido para futuras pruebas.  
4. Se acuerda realizar una nueva reunión de seguimiento tras la resolución del problema de conexión entre frontend y backend.

---

## Observaciones adicionales:

Además durante diversos arraques del servidor Tomcat hubo errores con los puertos. Ya se ha identificado y resuelto el problema. 
# ACTA DE REUNIÓN - PROYECTO NOL_G15

**Fecha:** Viernes, 30 de mayo de 2025  
**Lugar:** Reunión presencial en la universidad 
**Asistentes:** Arnau Vila, Nacho Pinazo, Álvaro Gómez, Ferran Belloch, Mario Pérez

---

## Orden del día:

- Revisión y solución de problemas con librerías y dependencias externas  
- Configuración de Tomcat y localización de recursos estáticos/dinámicos  
- Verificación del entorno de desarrollo en todas las máquinas  
- Análisis y actualización del script de inicialización del servidor de datos

---

## Desarrollo de la reunión:

Esta novena sesión tuvo lugar de forma presencial en el laboratorio de la universidad, lo que permitió un trabajo más directo y efectivo sobre las configuraciones locales de cada integrante.

En primer lugar, se abordaron los **problemas relacionados con las librerías del proyecto**, especialmente las necesarias para realizar peticiones REST a CentroEducativo. Se revisaron y corrigieron:

- Versiones inconsistentes de `jersey-client`, `jersey-common`, `hk2`, y `jakarta.ws.rs-api`  
- Ubicación incorrecta de los `.jar` en el proyecto Eclipse  
- Configuración de rutas de clase (`build path`) para asegurar que las dependencias sean accesibles durante la compilación y ejecución

Una vez resueltas estas cuestiones, se detectó un **nuevo problema crítico**: Tomcat no lograba encontrar ciertos recursos (JSP, imágenes, servlets mapeados). Se trató de forma prioritaria este inconveniente:

- Se revisó la estructura del proyecto (`WEB-INF`, `web.xml`, carpetas `/pages` y `/resources`)  
- Se actualizaron las rutas relativas y absolutas en formularios, scripts y controladores  
- Se validó el archivo `web.xml`, asegurando que los servlets estén correctamente definidos
- Se realizaron varias pruebas de despliegue en las máquinas locales para verificar que los recursos estuviesen disponibles en tiempo de ejecución

Finalmente, se revisó el **script de inicialización de datos** usado para poblar el servidor de `CentroEducativo`. Se detectaron rutas y claves incorrectas o desactualizadas:

- Se actualizaron los `dni`, contraseñas, asignaturas y formato de las peticiones JSON  
- Se aseguró que el script pudiese lanzarse en cualquier máquina del grupo sin necesidad de ajustes adicionales

---

## Acuerdos:

1. Todos los miembros deben tener el proyecto con las **librerías correctamente configuradas y funcionando**.  
2. Se ha establecido una **estructura de carpetas y rutas común** para evitar errores en producción.  
3. Se solucionó parcialmente el problema de recursos no encontrados por Tomcat; se continuará probando.  
4. El script de carga de datos ha sido **revisado y validado**, con las credenciales y formatos actuales.  
5. Se hará un nuevo test completo de despliegue durante la próxima sesión para asegurar la estabilidad.

---

## Observaciones adicionales:

- Se recuerda la importancia de comprobar las rutas al desplegar JSP o servlets desde Tomcat (especialmente al trabajar desde Eclipse).  
- A partir de este punto, se prioriza el avance en las vistas JSP del profesor y las funcionalidades  pendientes.

---

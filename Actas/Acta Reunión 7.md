# ACTA DE REUNIÓN - PROYECTO NOL_G15

**Fecha:** Miércoles, 21 de mayo de 2025  
**Lugar:** Reunión telemática por Discord  
**Asistentes:** Arnau Vila, Nacho Pinazo, Álvaro Gómez, Ferran Belloch, Mario Pérez

---

## Orden del día:

- Resolución de errores con los `import` en el código backend  
- Instalación de librerías necesarias para las peticiones REST  
- Conversión de HTML a JSP para integrar datos desde el backend  
- Coordinación del trabajo individual

---

## Desarrollo de la reunión:

Durante esta sesión, el grupo se centró en **resolver errores de compilación relacionados con los `import`**, necesarios para implementar la lógica backend y realizar peticiones a servicios REST.

Tras analizar el problema, se identificó la necesidad de **añadir varias librerías esenciales al proyecto**, entre ellas:

- `jersey-client`  
- `jersey-common`  
- `jakarta.ws.rs-api`  
- `hk2` (para la inyección de dependencias en Jersey)

Estas librerías fueron **descargadas y añadidas al repositorio Git** del proyecto, de modo que todos los integrantes puedan integrarlas fácilmente en sus entornos de desarrollo y evitar inconsistencias entre máquinas.

Otro punto abordado fue la **conversión de los archivos HTML a JSP**, con el objetivo de poder insertar dinámicamente datos desde los servlets. Sin embargo, surgieron dificultades:

- Problemas de sintaxis al adaptar los formularios y estructuras HTML a código JSP.  
- Dudas sobre cómo recuperar datos desde el backend y mostrarlos en la vista.  
- Necesidad de estructurar el flujo entre servlets y JSP correctamente (`requestDispatcher`, `setAttribute`, etc.).

---

## Acuerdos:

1. Las **librerías necesarias** ya están añadidas al proyecto en Git. Todos deben asegurarse de tenerlas correctamente integradas.  
2. Cada miembro **trabajará de forma individual a lo largo del día** para investigar y tratar de resolver la conversión de HTML a JSP.  
3. Se propone compartir soluciones viables y ejemplos en el grupo de Discord para avanzar más rápido.  
4. Se realizará una nueva reunión si alguno logra una integración funcional para explicarla al resto.

---

## Observaciones adicionales:
 
- El objetivo inmediato es tener toda la primera entrega completada antes del viernes por la mañana. 

---

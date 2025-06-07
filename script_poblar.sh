#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:9090/CentroEducativo"
COOKIE="cucu"
ADMIN_DNI="111111111"
ADMIN_PASS="654321"

# LOGIN
KEY=$(curl -s --data "{\"dni\":\"$ADMIN_DNI\",\"password\":\"$ADMIN_PASS\"}" \
        -H "Content-Type: application/json" \
        "$BASE/login" -c "$COOKIE" -b "$COOKIE")
echo "KEY: $KEY"
[[ -n $KEY ]] || { echo "Login fallido"; exit 1; }

# ASIGNATURAS
ASIGNATURAS=(
'{"acronimo":"DEW","nombre":"Desarrollo Web","curso":3,"cuatrimestre":"B","creditos":4.5}'
'{"acronimo":"IAP","nombre":"Integración de Aplicaciones","curso":4,"cuatrimestre":"A","creditos":4.5}'
'{"acronimo":"DCU","nombre":"Desarrollo Centrado en el Usuario","curso":4,"cuatrimestre":"A","creditos":4.5}'
)

echo "-> Asignaturas"
for A in "${ASIGNATURAS[@]}"; do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
           -X POST -H "Content-Type: application/json" \
           --data "$A" "$BASE/asignaturas?key=$KEY" -b "$COOKIE")
  echo "   $(jq -r .acronimo <<<"$A")  -> HTTP $code"
done

# PROFES
PROFES=(
'{"dni":"23456733H","nombre":"Ramón","apellidos":"Garcia"}'
'{"dni":"10293756L","nombre":"Pedro","apellidos":"Valderas"}'
'{"dni":"06374291A","nombre":"Manoli","apellidos":"Albert"}'
'{"dni":"65748923M","nombre":"Joan","apellidos":"Fons"}'
)

echo "-> Profesores"
for P in "${PROFES[@]}"; do
  code=$(curl -s -o /dev/null -w "%{http_code}" \
           -X POST -H "Content-Type: application/json" \
           --data "$P" "$BASE/profesores?key=$KEY" -b "$COOKIE")
  echo "   $(jq -r .dni <<<"$P") -> HTTP $code"
done

# ALUMNOS + ALTAS
ALUMNOS=(
'{"dni":"12345678W","nombre":"Pepe","apellidos":"Garcia Sanchez","password":"123456","asignaturas":["DCU","DEW","IAP"]}'
'{"dni":"23456387R","nombre":"Maria","apellidos":"Fernandez Gómez","password":"123456","asignaturas":["DCU","DEW"]}'
'{"dni":"34567891F","nombre":"Miguel","apellidos":"Hernandez Llopis","password":"123456","asignaturas":["DCU","IAP"]}'
'{"dni":"93847525G","nombre":"Laura","apellidos":"Benitez Torres","password":"123456","asignaturas":["IAP","DEW"]}'
'{"dni":"37264096W","nombre":"Minerva","apellidos":"Alonso Pérez","password":"123456","asignaturas":[]}'
)

echo "-> Alumnos y matrículas"
for alumno in "${ALUMNOS[@]}"; do
  DNI=$(jq -r .dni <<<"$alumno")

  code=$(curl -s -o /dev/null -w "%{http_code}" \
           -X POST -H "Content-Type: application/json" \
           --data "$alumno" "$BASE/alumnos?key=$KEY" -b "$COOKIE")
  echo "   $DNI -> alta HTTP $code"

  mapfile -t ASIGS < <(jq -r '.asignaturas[]' <<<"$alumno")
  for ASIG in "${ASIGS[@]}"; do
    code2=$(curl -s -o /dev/null -w "%{http_code}" \
              -X POST "$BASE/asignaturas/$ASIG/alumnos/$DNI?key=$KEY" -b "$COOKIE")
    echo "      -> $ASIG -> matricula HTTP $code2"
  done
done

echo "Script finalizado"


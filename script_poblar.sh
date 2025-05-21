#!/usr/bin/env bash
set -euo pipefail

BASE="http://localhost:9090/CentroEducativo"
COOKIE="cucu"                 # almacena JSESSIONID
ADMIN_DNI="111111111"         # usuario administrador
ADMIN_PASS="654321"

# 1. LOGIN
KEY=$(curl -s --data "{\"dni\":\"$ADMIN_DNI\",\"password\":\"$ADMIN_PASS\"}" \
          -X POST -H "Content-Type: application/json" \
          "$BASE/login" -c "$COOKIE" -b "$COOKIE" \
      | jq -r .key)

echo "Sesión abierta (key=$KEY)"

# 2. CARGA DE ASIGNATURAS
declare -a ASIGNATURAS=(
'{"acronimo":"DEW","nombre":"Desarrollo Web","curso":3,"cuatrimestre":"B","creditos":4.5}'
'{"acronimo":"IAP","nombre":"Integración de Aplicaciones","curso":4,"cuatrimestre":"A","creditos":4.5}'
'{"acronimo":"DCU","nombre":"Desarrollo Centrado en el Usuario","curso":4,"cuatrimestre":"A","creditos":4.5}'
)

for A in "${ASIGNATURAS[@]}"; do
  curl -s -X POST -H "Content-Type: application/json" \
       --data "$A" "$BASE/asignaturas?key=$KEY" -b "$COOKIE" > /dev/null
  echo "   ➜ asignatura añadida: $(echo "$A" | jq -r .acronimo)"
done
echo "Asignaturas cargadas"

# 3. CARGA DE PROFESORES
declare -a PROFES=(
'{"dni":"23456733H","nombre":"Ramón","apellidos":"Garcia"}'
'{"dni":"10293756L","nombre":"Pedro","apellidos":"Valderas"}'
'{"dni":"06374291A","nombre":"Manoli","apellidos":"Albert"}'
'{"dni":"65748923M","nombre":"Joan","apellidos":"Fons"}'
)

for P in "${PROFES[@]}"; do
  curl -s -X POST -H "Content-Type: application/json" \
       --data "$P" "$BASE/profesores?key=$KEY" -b "$COOKIE" > /dev/null
  echo "   ➜ profesor añadido: $(echo "$P" | jq -r .dni)"
done
echo "Profesores cargados"

# 4. CARGA DE ALUMNOS + MATRÍCULAS
read -r -d '' ALUMNOS_JSON <<"EOF"
[
  {"dni":"12345678W","nombre":"Pepe","apellidos":"Garcia Sanchez","password":"123456","asignaturas":["DCU","DEW","IAP"]},
  {"dni":"23456387R","nombre":"Maria","apellidos":"Fernandez Gómez","password":"123456","asignaturas":["DCU","DEW"]},
  {"dni":"34567891F","nombre":"Miguel","apellidos":"Hernandez Llopis","password":"123456","asignaturas":["DCU","IAP"]},
  {"dni":"93847525G","nombre":"Laura","apellidos":"Benitez Torres","password":"123456","asignaturas":["IAP","DEW"]},
  {"dni":"37264096W","nombre":"Minerva","apellidos":"Alonso Pérez","password":"123456","asignaturas":[]}
]
EOF

echo "$ALUMNOS_JSON" | jq -c '.[]' |
while read -r alumno; do
  # Alta del alumno
  curl -s -X POST -H "Content-Type: application/json" \
       --data "$alumno" "$BASE/alumnos?key=$KEY" -b "$COOKIE" > /dev/null
  DNI=$(echo "$alumno" | jq -r .dni)
  echo "   ➜ alumno añadido: $DNI"

  # Matrícula en sus asignaturas
  for ASIG in $(echo "$alumno" | jq -r '.asignaturas[]'); do
    curl -s -X POST "$BASE/asignaturas/$ASIG/alumnos/$DNI?key=$KEY" \
         -b "$COOKIE" > /dev/null
    echo "      ↳ matriculado en $ASIG"
  done
done
echo "Alumnos y matrículas cargadas"

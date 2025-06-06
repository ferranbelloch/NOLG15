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
  ACR=$(jq -r .acronimo <<<"$A")
  code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/asignaturas/$ACR?key=$KEY" -b "$COOKIE")
  if [[ $code == "200" ]]; then
    echo "   $ACR ya existe -> HTTP $code"
  else
    code=$(curl -s -o /dev/null -w "%{http_code}" \
             -X POST -H "Content-Type: application/json" \
             --data "$A" "$BASE/asignaturas?key=$KEY" -b "$COOKIE")
    echo "   $ACR -> creada HTTP $code"
  fi
done

# PROFESORES
PROFES=(
'{"dni":"23456733H","nombre":"Ramón","apellidos":"Garcia"}'
'{"dni":"10293756L","nombre":"Pedro","apellidos":"Valderas"}'
'{"dni":"06374291A","nombre":"Manoli","apellidos":"Albert"}'
'{"dni":"65748923M","nombre":"Joan","apellidos":"Fons"}'
)

echo "-> Profesores"
for P in "${PROFES[@]}"; do
  DNI=$(jq -r .dni <<<"$P")
  code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/profesores/$DNI?key=$KEY" -b "$COOKIE")
  if [[ $code == "200" ]]; then
    echo "   $DNI ya existe -> HTTP $code"
  else
    code=$(curl -s -o /dev/null -w "%{http_code}" \
             -X POST -H "Content-Type: application/json" \
             --data "$P" "$BASE/profesores?key=$KEY" -b "$COOKIE")
    echo "   $DNI -> creado HTTP $code"
  fi
done

# ALUMNOS (10 en total)
ALUMNOS=(
'{"dni":"12345678W","nombre":"Pepe","apellidos":"Garcia Sanchez","password":"123456","asignaturas":["DCU","DEW","IAP"]}'
'{"dni":"23456387R","nombre":"Maria","apellidos":"Fernandez Gómez","password":"123456","asignaturas":["DCU","DEW"]}'
'{"dni":"34567891F","nombre":"Miguel","apellidos":"Hernandez Llopis","password":"123456","asignaturas":["DCU","IAP"]}'
'{"dni":"93847525G","nombre":"Laura","apellidos":"Benitez Torres","password":"123456","asignaturas":["IAP","DEW"]}'
'{"dni":"37264096W","nombre":"Minerva","apellidos":"Alonso Pérez","password":"123456","asignaturas":[]}'
'{"dni":"00000011K","nombre":"Ana","apellidos":"Valverde","password":"123456","asignaturas":["DEW","DCU"]}'
'{"dni":"00000012L","nombre":"Roberto","apellidos":"Cuevas","password":"123456","asignaturas":["IAP"]}'
'{"dni":"00000013M","nombre":"Marina","apellidos":"Delgado","password":"123456","asignaturas":["DEW","IAP"]}'
'{"dni":"00000014N","nombre":"Dario","apellidos":"Vega","password":"123456","asignaturas":["DCU"]}'
'{"dni":"00000015O","nombre":"Isabel","apellidos":"Campos","password":"123456","asignaturas":["DEW","IAP","DCU"]}'
)

echo "-> Alumnos y matrículas con notas"
for alumno in "${ALUMNOS[@]}"; do
  DNI=$(jq -r .dni <<<"$alumno")

  code=$(curl -s -o /dev/null -w "%{http_code}" "$BASE/alumnos/$DNI?key=$KEY" -b "$COOKIE")
  if [[ $code == "200" ]]; then
    echo "   $DNI ya existe -> HTTP $code"
  else
    code=$(curl -s -o /dev/null -w "%{http_code}" \
             -X POST -H "Content-Type: application/json" \
             --data "$alumno" "$BASE/alumnos?key=$KEY" -b "$COOKIE")
    echo "   $DNI -> creado HTTP $code"
  fi

  mapfile -t ASIGS < <(jq -r '.asignaturas[]' <<<"$alumno")
  for ASIG in "${ASIGS[@]}"; do
    matricula_data="{\"acronimo\":\"$ASIG\"}"
    code2=$(curl -s -o /dev/null -w "%{http_code}" \
              -X POST -H "Content-Type: application/json" \
              --data "$matricula_data" "$BASE/alumnos/$DNI/asignaturas?key=$KEY" -b "$COOKIE")
    echo "      -> $ASIG -> matricula HTTP $code2"

    nota=$((RANDOM % 11))
    code3=$(curl -s -o /dev/null -w "%{http_code}" \
    -X PUT -H "Content-Type: application/json" \
    --data "$nota" "$BASE/alumnos/$DNI/asignaturas/$ASIG?key=$KEY" -b "$COOKIE")
    echo "         -> nota $nota -> HTTP $code3"
  done
done

echo "Script finalizado"

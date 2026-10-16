---
name: errores-para-negocio
description: Explica cada error como lo vive el usuario o el negocio, qué hacer ante él y deja el nombre técnico solo como referencia.
---
# Errores explicados para el negocio

Documenta cada error con este formato:

**Qué le pasa al usuario, en una frase**
- **Cuándo ocurre:** la condición de negocio que lo provoca, tomada del mensaje de error o del
  contexto de código de la evidencia.
- **Impacto:** qué operación o proceso se detiene, se rechaza o queda pendiente.
- **Qué hacer:** la acción concreta para el usuario o para el área de soporte.
- **Referencia técnica:** `NombreTecnicoDelError`, solo para que soporte pueda ubicarlo.

Reglas:
- Agrupa los errores por el proceso de negocio afectado, no por su tipo técnico.
- Cuando la evidencia incluya un mensaje del catálogo de errores, úsalo para explicar el error.
- Separa los errores que el usuario puede corregir (datos, estados, aprobaciones) de los que
  requieren a soporte (fallas de servicios o de comunicación).
- No describas trazas de error, clases internas ni configuraciones.
- Si falta evidencia para un campo, escribe el mensaje de datos faltantes en ese campo.

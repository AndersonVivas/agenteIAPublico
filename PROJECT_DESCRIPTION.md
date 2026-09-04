# Descripción del Proyecto

Este proyecto es un microservicio Spring Boot 4 que automatiza la recepción de repositorios y el análisis de código fuente.

## Qué hace

- Expone un endpoint REST en `POST /api/v1/repository-agent/clone`.
- Recibe una solicitud de clonación y clona el repositorio Git objetivo en un espacio de trabajo local.
- Ejecuta un escaneo del repositorio para identificar señales de código como APIs, acceso a base de datos, excepciones, uso de configuración y dependencias externas.
- Devuelve una respuesta estructurada con el resultado de la clonación y el resumen del escaneo.
- Soporta dos modos de ejecución:
  - Procesamiento directo, donde la clonación y el escaneo ocurren en el mismo flujo de la petición.
  - Orquestación con Temporal, donde el flujo se delega a Temporal cuando `app.temporal.enabled=true`.

## Capacidades principales

- Clonación de repositorios Git con URL base, credenciales y directorio local configurables.
- Escaneo de código fuente en múltiples lenguajes y tipos de archivo.
- Generación de evidencia agregada sobre la estructura del repositorio y el uso de tecnologías.
- Manejo de errores REST para fallos relacionados con la clonación.
- Soporte de health y readiness mediante Spring Boot Actuator.

## Aspectos de configuración

- La configuración de Git se define mediante propiedades `app.git.*`.
- La configuración del workflow de Temporal se define mediante propiedades `app.temporal.*`.
- Los endpoints de observabilidad y salud se exponen mediante Spring Boot Actuator.

## Flujo típico

1. Un cliente envía una solicitud de clonación de repositorio.
2. El servicio clona el repositorio de forma local.
3. El repositorio se analiza para detectar indicadores arquitectónicos y técnicos.
4. El servicio devuelve el resultado de la clonación y el resumen del análisis.

## Uso previsto

Este servicio está pensado para apoyar el análisis automatizado de repositorios, la generación de documentación y los flujos de modernización, produciendo una copia local y un escaneo reproducible del código base.
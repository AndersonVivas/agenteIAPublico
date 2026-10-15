# Error Manual

## Alcance y Calidad de Evidencia

**1. Alcance documental.** Este manual cubre la operación del microservicio de remesas y su superficie pública bajo `/business/cash-flow-management/v1/remittances`: consulta, creación/actualización, notificación, archivos digitales y ejecución de servicios centrales. También cubre operaciones de denominaciones, totales, balances, órdenes, guías de transporte, transferencias y estados.

**2. Cobertura de evidencia.** La evidencia proviene de OpenAPI, código de clientes Feign, manejo global de excepciones, configuraciones Spring/Bootstrap, perfiles, Helm, Kafka, Log4j2 y propiedades de build. Se identifican dependencias HTTP internas, mensajería Kafka, chequeos de salud mediante Actuator y rutas de despliegue por ambiente.

**3. Nivel de confianza.** Alto para la existencia de rutas, clientes síncronos, excepciones, variables de configuración y dependencias declaradas. Medio para el impacto operativo exacto por operación, porque el escaneo no aporta telemetría productiva, contratos efectivos de dependencias ni historial de incidentes.

**4. Brechas de conocimiento.** No evidenciado en el escaneo: valores efectivos de timeouts, códigos HTTP específicos por excepción de dominio, umbrales de alertamiento, configuración de alta disponibilidad de Kafka, propietarios operativos, SLO/SLA, mecanismos de reintento, circuit breaking, procedimientos aprobados de reverso o resultados de pruebas de recuperación.

**5. Conflictos y cautelas.** Se observan configuraciones de Kafka activas y un toggle `kafka: OFF` en distintos recursos; debe verificarse el perfil efectivo antes de asumir publicación o auditoría por eventos. Las rutas IFI y CENTRAL_BANK aparecen explícitamente no implementadas; no deben tratarse como capacidades operativas disponibles. Referencias de hosts, paths y clientes se resuelven por ambiente, por lo que la configuración desplegada prevalece sobre los valores observados en el repositorio.

**Trazabilidad:** EV-159709dd1bd6, EV-003f2d681207, EV-1297a45d9a30, EV-196b1cd54897, EV-4de1a50ca58a, EV-49498d9c99eb.

## Triage de los Primeros 15 Minutos

**1. Delimitar impacto.** Registrar hora de inicio, ambiente, operación afectada, `remittanceId` u otro identificador de negocio, volumen estimado y si el fallo afecta consulta, modificación, ejecución, transferencia, notificación o descarga. Separar fallos de una remesa de degradación transversal.

**2. Validar disponibilidad local.** Consultar `/actuator/health` en la instancia afectada. Si el estado no es `UP`, tratar el evento como indisponibilidad de servicio o rollout fallido y escalar a plataforma/despliegue antes de reintentar operaciones de negocio.

**3. Clasificar la respuesta.** Si la respuesta corresponde a una excepción de negocio, validar datos de la operación: monto total, saldo, estado, aprobaciones, destino, canal, cabeceras y orden de remesa. Si corresponde a `TechnicalException`, timeout, error Feign o error genérico, continuar con la verificación de dependencia.

**4. Identificar dependencia común.** Correlacionar los fallos por cliente: remesas, denominaciones/totales, balance, moneda, canal, estado, órdenes, guía de transporte o transferencia de efectivo. Un incremento simultáneo en varias operaciones que usan la misma dependencia indica probable causa externa.

**5. Contener.** No reprocesar ni repetir automáticamente operaciones de cambio de estado, ejecución, transferencia, actualización de denominaciones o totales mientras el resultado sea incierto. Mantener trazabilidad por identificador y confirmar el estado actual antes de cualquier acción correctiva.

**6. Priorizar escalamiento.** Escalar inmediatamente si hay indisponibilidad de health, fallos repetidos de dependencia, errores de timeout, sospecha de duplicidad, impacto en transferencias, o selección de IFI/CENTRAL_BANK. Adjuntar GUID, hora, ruta, código HTTP, respuesta sanitizada y dependencia involucrada.

**Trazabilidad:** EV-4de1a50ca58a, EV-1cdb432a9726, EV-20f3878e31b5, EV-003f2d681207, EV-49498d9c99eb.

## Catalogo de Errores

**1. Validación y reglas de negocio.**

| Patrón | Señal observable | Tratamiento inicial |
|---|---|---|
| Monto inconsistente o insuficiente | `AmountTotalMismatchException` o `RemittanceInadequateAmountException` | Validar monto total, denominaciones y saldo; no reintentar sin corregir datos. |
| Estado no permitido o cambio rechazado | `RemittanceCantChangeStatusException`, `InconsistentStatusDetailCatalogException` o catálogo ilegal | Consultar estado vigente y catálogo; confirmar transición autorizada. |
| Remesa sin aprobación u orden inexistente | `RemittancesNotApprovedException` o error de orden no encontrada | Validar aprobación y existencia de la orden antes de reprocesar. |
| Cabecera, canal o destino inválido | `bad_request_header`, `NotValidChannelException` o `NotValidDestinationException` | Corregir solicitud, canal y destino; conservar evidencia del request sanitizado. |
| Código de remesa no generado o no actualizable | `NewRemittanceCodeException` o `UpdateRemittanceCodeException` | Confirmar unicidad, estado de la remesa y resultado previo antes de reintentar. |

**2. Fallos técnicos e integración.**

| Patrón | Señal observable | Tratamiento inicial |
|---|---|---|
| Falla HTTP de dependencia | `TechnicalException` originada por Feign | Identificar cliente/dependencia, código HTTP y latencia; no asumir que la operación no se ejecutó. |
| Error inesperado | `TechnicalException` con error inesperado o handler genérico | Preservar GUID, stack trace y contexto; escalar si es recurrente. |
| Generación de adjunto fallida | `AttachmentGenerationException` | Verificar insumos y servicio asociado; evitar volver a emitir archivos sin comprobar resultado. |
| Método HTTP no soportado o datos inválidos | `HttpRequestMethodNotSupportedException` o `MethodArgumentNotValidException` | Corregir método, contrato y payload del consumidor. |

**3. Funcionalidad no soportada.** La selección de proveedores IFI o CENTRAL_BANK puede lanzar `NotImplementedException`; tratarla como limitación conocida y derivar por el canal operativo aprobado.

**Trazabilidad:** EV-18611297ec58, EV-1cdb432a9726, EV-20f3878e31b5, EV-49498d9c99eb, EV-32998e3feb1f.

## Matriz de Fallos de Dependencias Externas

**1. Dependencias HTTP síncronas.** El servicio usa clientes Feign configurables para soporte de remesas, denominaciones, totales, balance, canales, estados, moneda, órdenes, guías de transporte y transferencia de efectivo. Una indisponibilidad, DNS inválido, error HTTP o latencia en cualquiera de ellas puede bloquear o degradar las operaciones de extremo a extremo.

| Dependencia o dominio | Síntoma esperado | Códigos/timeout | Guía de recuperación |
|---|---|---|---|
| Remesas, órdenes, denominaciones y totales | Consulta o actualización fallida; estado incierto en operaciones mutables | Los códigos específicos no están evidenciados; las fallas Feign se normalizan como `TechnicalException` | Verificar estado remoto por identificador antes de reintentar; escalar con cliente, ruta, código y latencia. |
| Balance y moneda | Rechazo de validación de fondos, consulta de balance o cálculo asociado | No evidenciado en el escaneo | Validar disponibilidad y respuesta del servicio de balance/moneda; no forzar la operación si el saldo no puede confirmarse. |
| Canal, estado y sucursal/institución | Rechazo por canal/destino o imposibilidad de resolver catálogos y estados | No evidenciado en el escaneo | Confirmar datos maestros y disponibilidad de la dependencia; corregir solicitud si aplica. |
| Guía de transporte y transferencia de efectivo | Error en ejecución logística o transferencia BCE | No evidenciado en el escaneo | Detener reprocesos automáticos; reconciliar estado de guía/transferencia con el sistema propietario. |

**2. Timeout y propagación.** Existen claves de timeout sin valores explícitos o con referencias ambiguas. No se puede afirmar el límite efectivo ni el código de respuesta final; una latencia prolongada debe investigarse como posible timeout o saturación de dependencia. El manejo observado envuelve errores Feign en `TechnicalException`, sin evidencia de retry, fallback o circuit breaker.

**3. Kafka.** Kafka soporta flujos de eventos y auditoría mediante tópicos configurados por entorno. La indisponibilidad del broker, credenciales ausentes o configuración SASL incorrecta puede afectar publicación, procesamiento y auditoría. El comportamiento de reintento, retención, HA y timeout no está evidenciado en el escaneo.

**4. Dependencias de despliegue.** Helm utiliza URLs `*.svc.cluster.local` y condiciona el arranque a `/actuator/health`. Problemas de DNS, red de clúster o un health no `UP` pueden impedir rollout o provocar reinicios.

**Trazabilidad:** EV-003f2d681207, EV-20f3878e31b5, EV-1297a45d9a30, EV-196b1cd54897, EV-0cecd5a79d19, EV-4de1a50ca58a.

## Riesgos de Configuracion y Timeouts

**1. Verificar resolución de ambiente.** Hosts, paths, puerto, identidad y versión se inyectan mediante placeholders y perfiles, incluido `include: ${ENVIRONMENT}`. Una variable ausente, mal nombrada o apuntando al ambiente incorrecto puede impedir el arranque o dirigir tráfico a una dependencia equivocada. Validar configuración efectiva del despliegue antes de intervenir el código.

**2. Tratar los timeouts como riesgo alto.** Se identificaron claves de timeout sin valores explícitos o ambiguos para clientes/configuración. No asumir un límite conocido: confirmar valores efectivos, relación con el timeout del gateway y coherencia entre cliente, servidor y proxy. El error documentado como `gateway response time limit` debe correlacionarse con la latencia de la dependencia.

**3. Validar URLs generadas.** Bootstrap genera claves de cliente y URLs base mediante plantillas como `client-${openapicliente?index}` y `http://localhost:808${openapicliente?index}/v1/`. Una renderización incorrecta puede causar fallas de conexión o desvío de tráfico; verificar el resultado final por ambiente.

**4. Revisar configuración local.** El perfil local deshabilita el pool de conexiones. Esta condición puede ocultar problemas de rendimiento o saturación y no debe promoverse sin revisión a ambientes superiores.

**5. Proteger configuración Kafka.** Las credenciales se resuelven por variables de entorno en producción; comprobar que estén disponibles sin exponerlas en logs, tickets o capturas. Las credenciales de prueba en texto plano deben considerarse un riesgo y no reutilizarse.

**6. Confirmar salud de arranque.** Los manifiestos usan `curl` contra `http://localhost:8080/actuator/health` y exigen `UP`. Revisar puerto real, exposición de Actuator y tiempo de inicialización cuando existan reinicios o rollouts fallidos.

**Trazabilidad:** EV-20fec3c86ef1, EV-1297a45d9a30, EV-0bedf2c63cee, EV-b6b3c03fb5f8, EV-170b36fb4ede, EV-4de1a50ca58a.

## Limitaciones Conocidas y Rutas No Soportadas

**1. Rutas de proveedor no implementadas.** Los flujos IFI y CENTRAL_BANK lanzan explícitamente `NotImplementedException` en varios proveedores de transferencia y balance. No habilitar ni reprocesar transacciones por estas rutas; derivar el caso por el proceso operativo aprobado.

**2. Incertidumbre del resultado de operaciones mutables.** Las rutas de actualización, cambio de estado, ejecución, transferencias, denominaciones y totales dependen de integraciones síncronas. Ante timeout o fallo Feign, el escaneo no demuestra idempotencia, compensación ni consulta automática de estado; no asumir que la operación falló por completo ni que puede repetirse sin reconciliación.

**3. Diagnóstico HTTP limitado.** El manejador global centraliza excepciones, pero no se evidencian mapeos HTTP específicos por cada excepción de dominio. El código HTTP, el detalle expuesto al consumidor y la acción exacta deben validarse contra la respuesta real y los logs.

**4. Observabilidad incompleta.** Los logs dependen de `host`, `user-agent`, `x-forwarded-for` y `guid` en el contexto. No está evidenciada su inicialización uniforme; la ausencia de estos campos puede impedir correlación completa entre consumidor, gateway y dependencias.

**5. Mensajería y recuperación no verificadas.** Kafka está configurado, incluso con un toggle desactivado en algunos recursos, pero no se evidencian semánticas de entrega, reintentos, DLQ, HA ni procedimientos de recuperación. No concluir pérdida, duplicidad o entrega garantizada de eventos sin evidencia adicional.

**6. Datos operativos faltantes.** No evidenciado en el escaneo: responsables por dependencia, contactos de escalamiento, umbrales de capacidad, SLO, valores productivos de timeout, runbooks aprobados y resultados de pruebas de contingencia.

**Trazabilidad:** EV-49498d9c99eb, EV-20f3878e31b5, EV-1cdb432a9726, EV-3d6054cdca95, EV-196b1cd54897.

## Recomendaciones de Monitoreo y Alertas

**1. Disponibilidad del servicio.** Alertar ante estado distinto de `UP` en `/actuator/health`, fallas de startup o reinicios asociados al chequeo Helm. Segmentar por ambiente, versión e instancia para diferenciar un fallo de rollout de una degradación general.

**2. Errores por tipo.** Medir y alertar por tasa de `BusinessException`, `TechnicalException`, errores de validación y método HTTP no soportado. Separar reglas de negocio de fallos técnicos para evitar escalar como infraestructura los rechazos corregibles por datos.

**3. Salud de dependencias.** Instrumentar tasa de errores, latencia, timeouts y códigos HTTP por cliente Feign: remesas, órdenes, denominaciones/totales, balance, moneda, canal, estado, guía de transporte y transferencia. Alertar por aumento sostenido, no por eventos aislados, y correlacionar con la ruta de negocio afectada.

**4. Operaciones sensibles.** Vigilar específicamente fallos o latencias anómalas en cambio de estado, ejecución de core services, transferencias, actualizaciones de denominaciones/totales y notificaciones. Estas rutas pueden producir estado incierto cuando falla una dependencia síncrona.

**5. Kafka y auditoría.** Monitorizar conectividad al broker, autenticación, errores de serialización, publicación/consumo por tópico transaccional y auditor, y retraso de consumidores si la plataforma expone esas métricas. Umbrales específicos: No evidenciado en el escaneo.

**6. Trazabilidad de solicitudes.** Incluir y consultar `guid`, host, IP reenviada y user-agent cuando estén disponibles. Alertar sobre reducción significativa de solicitudes sin GUID o contexto, porque limita la investigación de incidentes.

**Trazabilidad:** EV-4de1a50ca58a, EV-1cdb432a9726, EV-003f2d681207, EV-196b1cd54897, EV-3d6054cdca95.

## Playbooks de Recuperacion

**1. Timeout o error técnico de una dependencia.**
1. Registrar GUID, ruta, identificador de remesa, hora, cliente Feign, código HTTP y latencia.
2. Confirmar que el servicio local permanece `UP`.
3. Identificar la dependencia común y verificar su disponibilidad con el equipo propietario.
4. Consultar el estado actual de la remesa, orden, transferencia o actualización antes de reintentar.
5. Reintentar solo si el estado remoto confirma que no hubo efecto o si el procedimiento del dominio lo autoriza.
6. Escalar si persiste, afecta varias operaciones o deja transacciones en estado incierto.

**2. Rechazo de regla de negocio.**
1. Clasificar la excepción: monto, saldo, estado, aprobación, destino, canal, cabecera u orden.
2. Validar los datos de entrada y el estado vigente en los sistemas propietarios.
3. Corregir la solicitud o completar la precondición faltante.
4. Ejecutar una sola vez la operación corregida y conservar su correlación.
5. Escalar al responsable funcional si la regla contradice el comportamiento esperado del negocio.

**3. Rollout fallido o instancia no saludable.**
1. Verificar `/actuator/health`, puerto efectivo y logs de arranque.
2. Validar variables de ambiente, hosts, paths, versión y configuración del perfil desplegado.
3. Confirmar resolución de DNS y disponibilidad de dependencias críticas del clúster.
4. Revisar el chequeo Helm que exige estado `UP`.
5. Revertir o pausar el rollout conforme al proceso de plataforma si la salud no se recupera; no mezclar la intervención con reprocesos de negocio.

**4. Fallo de Kafka o auditoría.**
1. Confirmar si Kafka está habilitado en el perfil efectivo.
2. Verificar conectividad, autenticación y errores de serialización sin exponer credenciales.
3. Registrar tópico, GUID, hora y resultado de publicación/consumo si están disponibles.
4. Escalar a la plataforma de mensajería; no declarar pérdida o duplicidad de eventos sin evidencia de entrega.

**5. Solicitud IFI o CENTRAL_BANK.**
1. Detener el reproceso.
2. Confirmar que el proveedor seleccionado corresponde a una ruta no implementada.
3. Informar la limitación al canal solicitante y derivar al flujo soportado o al responsable funcional.
4. Escalar para priorización de producto si la capacidad es requerida.

**Trazabilidad:** EV-20f3878e31b5, EV-1cdb432a9726, EV-4de1a50ca58a, EV-196b1cd54897, EV-49498d9c99eb.

## Escalamiento y Datos a Recolectar

**1. Escalar de inmediato** cuando ocurra cualquiera de estas condiciones: servicio no saludable; fallo repetido de una dependencia Feign; timeout o error de gateway; impacto en transferencia, ejecución o cambio de estado; posible duplicidad o estado incierto; error Kafka que afecte auditoría/procesamiento; selección de IFI o CENTRAL_BANK; o fallo de rollout.

**2. Escalar al equipo correcto.**

| Condición | Destino de escalamiento |
|---|---|
| Health no `UP`, reinicios, DNS, red, Helm o despliegue | Plataforma/SRE o equipo de despliegue. |
| Error Feign, timeout, código HTTP externo o inconsistencia remota | Equipo propietario de la dependencia y equipo de remesas. |
| Monto, saldo, aprobación, estado, canal, destino u orden | Soporte funcional/propietario del dominio y equipo de remesas. |
| Kafka, autenticación, tópico, serialización o retraso de consumo | Plataforma de mensajería y equipo de remesas. |
| IFI o CENTRAL_BANK | Responsable funcional/producto; la ruta no está implementada. |

**3. Recolectar antes del handoff.** Incluir ambiente, versión/instancia, hora con zona, ruta y método HTTP, `remittanceId`, orden, guía o transferencia relacionada, GUID, código HTTP, payload y respuesta sanitizados, stack trace o mensaje técnico, cliente/dependencia, latencia, estado de `/actuator/health`, volumen afectado y resultado de la consulta de estado posterior.

**4. Para eventos de configuración.** Adjuntar valores efectivos no sensibles de perfil, host/path resueltos, puerto, estado de Kafka y resultado del health check. Nunca adjuntar contraseñas, tokens, cabeceras de autorización ni contenido sensible de remesas.

**5. Criterio de cierre.** Cerrar únicamente cuando el servicio esté saludable, la dependencia se encuentre estable y cada operación afectada haya sido reconciliada o derivada al responsable correspondiente. La ausencia de nuevos errores no confirma por sí sola el resultado de operaciones mutables.

**Trazabilidad:** EV-003f2d681207, EV-4de1a50ca58a, EV-1297a45d9a30, EV-196b1cd54897, EV-49498d9c99eb, EV-3d6054cdca95.

## Indice de Evidencia

| Guía operativa | Evidencia consolidada |
|---|---|
| Superficie pública de remesas: consulta, actualización, notificación, archivos y ejecución | `api-public-remittance-openapi-surface` — EV-159709dd1bd6, EV-38a4bc3fa9f9, EV-5392c31eb186, EV-68e311ccf741 |
| Dependencias HTTP síncronas mediante Feign | `dependency-synchronous-feign-integrations` — EV-003f2d681207, EV-0286e3c26d2b, EV-0a93b33887f2, EV-fd5f9dd12568 |
| Denominaciones, totales, balance y transferencia | `api-denomination-client-endpoints`, `api-balance-and-cash-transfer-client-endpoints` — EV-69714137cf9b, EV-410ffb2616df, EV-bc92fb7c58a0 |
| Hosts externalizados, perfiles y URLs generadas | `configuration-environment-externalized-service-endpoints`, `configuration-templated-client-base-urls` — EV-20fec3c86ef1, EV-2f6c432ff055, EV-0bedf2c63cee |
| Riesgo de timeout sin valores explícitos | `configuration-unspecified-timeouts` — EV-1297a45d9a30, EV-25e20b9a63cd, EV-2c4b5035d4e0 |
| Kafka, credenciales y mensajería | `dependency-kafka-runtime-messaging`, `configuration-kafka-environment-credentials` — EV-196b1cd54897, EV-170b36fb4ede, EV-2cbc2ec63194 |
| Salud Actuator y despliegue Helm | `dependency-helm-actuator-startup-health-check` — EV-4de1a50ca58a, EV-73a00b2f0707, EV-b0d902507295 |
| Taxonomía, manejador y normalización de errores | `exception-business-and-technical-taxonomy`, `exception-global-handler-coarse-mappings`, `exception-feign-failures-wrapped-technical` — EV-18611297ec58, EV-1cdb432a9726, EV-20f3878e31b5 |
| Rutas IFI/CENTRAL_BANK no implementadas | `exception-unimplemented-ifi-central-bank-paths` — EV-49498d9c99eb, EV-5011a8b6a4a5, EV-730b735728bd |
| Contexto de logs para correlación | `configuration-runtime-log-context-dependency` — EV-3d6054cdca95, EV-78769a95acbb, EV-e312a862ba4d |

No evidenciado en el escaneo: métricas productivas, valores efectivos de timeout, umbrales de alerta, propietarios operativos, SLO/SLA y procedimientos de recuperación aprobados.

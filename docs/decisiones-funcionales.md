# 📑 Decisiones Funcionales y de Diseño

Este documento describe en detalle las decisiones funcionales y de arquitectura tomadas durante el desarrollo de la API para **Cocos Backend Challenge**, justificando el impacto operativo y la consistencia del sistema financiero.

---

### 1. Flujo Automático de Fondos mediante Eventos de Dominio (`OrderFilledEvent`)

* **Decisión:** Cada vez que una orden de compra (`BUY`) o venta (`SELL`) transiciona al estado `FILLED`, se emite un evento de aplicación/dominio `OrderFilledEvent`. El componente listener `CashFlowOrderListener` captura este evento de forma asíncrona/transaccional y genera automáticamente una orden de tesorería:
  * **Orden de Venta (`SELL FILLED`):** Genera automáticamente una orden de tipo `CASH_IN` en el instrumento de tipo `MONEDA` (ARS) por el monto total de la venta (`size * price`).
  * **Orden de Compra (`BUY FILLED`):** Genera automáticamente una orden de tipo `CASH_OUT` en el instrumento `MONEDA` (ARS) por el importe debitado (`size * price`).
* **Justificación & Beneficio:**
  * **Desacoplamiento y Responsabilidad Única:** La lógica central de ejecución de la orden bursátil se mantiene desacoplada de la mecánica de acreditación/débito de saldo en cuenta.
  * **Trazabilidad y Auditoría:** Toda entrada y salida de efectivo queda registrada de manera uniforme en la tabla `orders` con su correspondiente `side` (`CASH_IN` / `CASH_OUT`), cumpliendo con la premisa de que los movimientos de fondos se modelan como órdenes.

---

### 2. Procesamiento Periódico de Órdenes LIMIT mediante Scheduler

* **Decisión:** Se implementó una tarea programada (`LimitOrderExecutionScheduler`) que se ejecuta cada minuto (`@Scheduled(fixedRate = 60000)`).
* **Mecanismo:**
  * Consulta periódicamente todas las órdenes de tipo `LIMIT` que se encuentran en estado `NEW`.
  * Ejecuta la estrategia de re-evaluación (`ExecuteLimitOrderExecutionStrategy`) verificando las condiciones de mercado y la disponibilidad de saldo/tenencias.
  * Si la orden cumple las condiciones de ejecución, pasa al estado `FILLED` y a su vez dispara el `OrderFilledEvent` correspondiente para el movimiento de fondos.
* **Justificación & Beneficio:**
  * Permite que las órdenes límite no queden estancadas y puedan liquidarse automáticamente en cuanto las condiciones del mercado y del usuario lo permitan, sin necesidad de intervención manual.

---

### 3. Idempotencia en la Creación de Órdenes (`Idempotency-Key`)

* **Decisión:** Para la creación de órdenes vía `POST /orders`, se exige un encabezado obligatorio `Idempotency-Key` (UUID).
* **Mecanismo:**
  * Se almacena el registro en la tabla `idempotency_keys` asociando la clave única con el hash SHA-256 de la petición (`request_hash`).
  * Si un cliente reintenta una petición idéntica ante un corte de red o timeout, el sistema devuelve inmediatamente la respuesta previa cacheada sin reprocesar la orden ni duplicar transacciones.
  * Si se intenta reutilizar la misma clave para una petición distinta, se rechaza con un conflicto de idempotencia (`409 Conflict`).
* **Justificación & Beneficio:**
  * En sistemas financieros es crítico prevenir la duplicación accidental de operaciones y débitos indeseados ante reintentos automáticos de clientes o microservicios.

---

### 4. Patrón Strategy y Template Method para el Ciclo de Vida de Órdenes

* **Decisión:** La ejecución y validación de órdenes se estructuró aplicando los patrones **Template Method** y **Strategy**:
  * `AbstractOrderExecutionStrategy`: Define el esqueleto de ejecución común (resolución de contexto de cuenta, cotizaciones actuales y cadena de validaciones).
  * `MarketOrderExecutionStrategy`: Ejecuta de inmediato la orden al último precio de mercado (`close`), pasando la orden a `FILLED`.
  * `CreateLimitOrderExecutionStrategy`: Registra la orden límite en el libro con estado `NEW`.
  * `ExecuteLimitOrderExecutionStrategy`: Responsable de evaluar y transicionar órdenes límites pendientes a `FILLED`.
* **Justificación & Beneficio:**
  * Facilita la extensibilidad (por ejemplo, agregar en el futuro órdenes Stop-Loss, Trailing Stop, etc.) sin modificar el flujo base de validación y persistencia.

---

### 5. Flexibilidad de Entrada: Compra por Cantidad (`size`) o por Monto (`amount`)

* **Decisión:** El endpoint de envío de órdenes permite especificar tanto el número exacto de títulos a operar (`size`) como un presupuesto monetario disponible (`amount`).
* **Regla de Negocio:**
  * En caso de recibir `amount`, el sistema resuelve el precio efectivo y calcula la cantidad máxima de acciones enteras mediante redondeo hacia abajo (`RoundingMode.DOWN`), garantizando que no se generen fracciones de acciones.
  * Si el monto monetario especificado es inferior al costo de una acción individual, o si se envían tanto `size` como `amount` en la misma solicitud, la orden se rechaza con un error de validación claro (`BadDataException`).
* **Justificación & Beneficio:**
  * Maximiza la usabilidad para el usuario final (permitiendo invertir montos fijos de dinero) manteniendo la integridad del inventario de acciones del mercado.

---

### 6. Máquina de Estados Estricta y Validación de Transiciones (`OrderStatus`)

* **Decisión:** Los estados de las órdenes (`NEW`, `FILLED`, `REJECTED`, `CANCELLED`) implementan una máquina de estados determinística:
  * Solo una orden en estado `NEW` puede transicionar a `FILLED`, `REJECTED` o `CANCELLED`.
  * Los estados `FILLED`, `REJECTED` y `CANCELLED` son terminales; ninguna orden en estos estados puede ser modificada o re-ejecutada.
  * La cancelación de órdenes (`POST /orders/{id}/cancel`) valida explícitamente que la orden se encuentre en `NEW`.
* **Justificación & Beneficio:**
  * Evita inconsistencias de concurrencia y estados corruptos en el ciclo de vida de las operaciones.

---

### 7. Trazabilidad de Rechazos (`reject_reason`)

* **Decisión:** Se extendió el esquema de la tabla `orders` incorporando la columna `reject_reason`.
* **Mecanismo:**
  * Cuando una orden falla alguna de las reglas de la cadena de validadores (`FundsValidator`, `HoldingsValidator`, `PriceValidator`, `SizeValidator`), la orden se persiste en estado `REJECTED` junto con la explicación del motivo (ej.: fondos insuficientes, acciones insuficientes para la venta, precio de límite inválido).
* **Justificación & Beneficio:**
  * Cumple con la regla de registrar toda orden rechazada en el historial para auditoría y brinda al usuario retroalimentación comprensible sobre el motivo por el cual no se ejecutó su operación.

---

### 8. Cálculo de Posiciones y Portfolio en Tiempo Real (Event Sourcing Light)

* **Decisión:** En lugar de mantener columnas mutables con el saldo acumulado en la tabla de usuarios, el estado del portfolio (pesos disponibles, tenencias por instrumento y valuaciones) se calcula dinámicamente agregando los movimientos históricos en estado `FILLED`.
* **Fórmulas Aplicadas:**
  * **Efectivo disponible:** $\sum \text{CASH\_IN} + \sum \text{SELL} - \sum \text{CASH\_OUT} - \sum \text{BUY}$.
  * **Cantidad de acciones por activo:** $\sum \text{BUY (size)} - \sum \text{SELL (size)}$.
  * **Rendimiento Diario (%):** $\frac{\text{close} - \text{previousClose}}{\text{previousClose}} \times 100$.
  * **Rendimiento Total (%):** Basado en el precio promedio ponderado de compra ($\text{avgBuyPrice}$) contrastado contra el valor de mercado actual ($\text{netShares} \times \text{close}$).
  * **Exclusión de Moneda:** Los instrumentos de tipo `MONEDA` (ej. ARS) se excluyen de la lista de posiciones de activos bursátiles para evitar duplicar el cómputo del efectivo en el valor total de la cuenta.
* **Justificación & Beneficio:**
  * Garantiza consistencia matemática total (principio de partida doble) sin riesgos de desincronización entre balances cacheados y transacciones ejecutadas.

---

### 9. Búsqueda de Instrumentos Paginada y Enriquecida con Market Data

* **Decisión:** El endpoint de búsqueda (`GET /instruments/search`) soporta consultas por ticker o nombre del instrumento con paginación (`page`, `limit`).
* **Enriquecimiento:**
  * Cada instrumento retornado se complementa en tiempo de consulta con su último precio de mercado (`close`), el cierre previo (`previousClose`) y la variación porcentual calculada.
* **Justificación & Beneficio:**
  * Optimiza la transferencia de datos y ofrece a la interfaz de usuario toda la información financiera relevante para la toma de decisiones en una sola llamada.

---

### 10. Optimización de Base de Datos e Índices

* **Decisión:** Mediante migraciones versionadas con Flyway se crearon índices estratégicos sobre las tablas de lectura intensiva:
  * `idx_orders_user_status`: Optimiza el cálculo de saldos y tenencias filtrando por usuario y órdenes `FILLED`.
  * `idx_orders_user_instrument`: Acelera la agregación de órdenes por activo.
  * `idx_marketdata_instrument_date`: Agiliza la búsqueda del último registro de mercado por instrumento ordenado por fecha.
  * `idx_instruments_ticker` y `idx_instruments_name`: Aseguran búsquedas rápidas e insensibles a mayúsculas/minúsculas.
* **Justificación & Beneficio:**
  * Mantiene tiempos de respuesta submilisegundos incluso a medida que el volumen de órdenes y cotizaciones crece en el tiempo.

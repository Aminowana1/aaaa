# Auditoría de MDVEconomy

Fecha: 26 de septiembre de 2026. Entrada: `MDVEconomy-main.zip`, recuperado del adjunto de la conversación indicada. Se inspeccionaron los 30 archivos Java, los tres YAML, los POM, el workflow, el helper Python, la licencia, la configuración del editor y los metadatos/binarios de `ct_extract`. `INVENTARIO.md` registra todos los archivos originales y los métodos declarados.

## Resultado y alcance

La compilación real con Temurin JDK 21.0.12.1 y Maven 3.9.9 terminó con **BUILD SUCCESS**. El JAR incorpora CommandTree y SQLite; las APIs del servidor quedan fuera. Pasaron el control del JAR y 12 comprobaciones reales sobre SQLite en memoria. Actionlint 1.7.12 aceptó el workflow sin errores.

El defecto confirmado del workflow era intentar copiar `MDVPublic-<versión>.jar`, aunque Maven produce `MDVEconomy-<versión>.jar`. Se corrigió haciendo que el workflow consulte el nombre final a Maven. No se encontraron errores de javac en los 30 fuentes actuales. No se cambiaron las reglas de venta, compra, cobro, menús ni persistencia: los fallos funcionales siguientes quedan documentados, **no corregidos**, para conservar el comportamiento solicitado.

“Confirmado por código” significa que existe una rama o secuencia defectuosa verificable en los fuentes. No significa que se haya ejecutado un exploit en un servidor. No se arrancó Paper ni se ejecutó esta entrega en GitHub; la ejecución real del workflow queda para cuando se suba el proyecto. Las pruebas locales no certifican compatibilidad de MMOItems/MythicLib con el servidor ni recuperación ante caídas.

## Funciones disponibles

| Función | Comportamiento real |
|---|---|
| Inicio y cierre | Crea configuración y mensajes por defecto, inicializa SQLite, busca un proveedor Vault, registra comandos/listeners, procesa vencimientos y programa revisiones. Cierra la conexión al desactivar. |
| `/mdveconomy` | Muestra los argumentos del árbol de comandos. |
| `/mdveconomy reload` | Recarga config.yml y messages.yml. Requiere OP o `mdveconomy.reload`; rechaza argumentos extra. |
| `/ah` | Abre el menú de subastas para jugadores, con carga asíncrona desde SQLite. |
| `/ah sell <precio>` y `/ah vender <precio>` | Publica la pila completa de la mano principal. Precio entero entre 1 y el máximo configurado (9999). Rechaza aire y denares de MMOItems. El precio es por toda la pila. |
| Límites por rango | Por defecto: 5 publicaciones, 12 horas según config.yml. VIP: 15 publicaciones, 36 horas con `mdveconomy.ah.rank.vip`. Combina el mayor límite y la mayor duración de los rangos permitidos. |
| Listado paginado | Consulta ACTIVE no vencidas, ordenadas por fecha descendente. 21 posiciones de venta en el menú por defecto; flechas anterior/siguiente y número de página. |
| Información del artículo | Muestra precio, vendedor y tiempo restante, con placeholders y lore configurable. Conserva el ítem serializado para entrega, separado del ítem de presentación. |
| Compra normal | Un clic abre las opciones de pago: banco o denares de inventario. Impide comprar publicaciones propias y comprueba estado/vencimiento. |
| Compra rápida | Shift-clic usa la preferencia INVENTORY/BANK almacenada por jugador. Un botón alterna la preferencia; el valor inicial es INVENTORY. |
| Pago por banco | Comprueba y retira saldo mediante Vault. Marca la publicación SOLD si continúa disponible. |
| Pago con efectivo | Cuenta y retira ítems MMOItems `MISCELLANEOUS:DENAR` del almacenamiento del inventario. Cada unidad equivale a un denar; redondea hacia arriba si el precio llega con decimales desde otro consumidor. |
| Cobro al vendedor | Siempre deposita en Vault, cualquiera sea el método del comprador. La preferencia guardada se usa para comprar, no para elegir dónde cobrar, pese al comentario de PaymentMethod. |
| Competencia entre compradores | El UPDATE condicional permite una sola transición ACTIVE→SOLD. Al perder esa carrera intenta devolver el pago. No garantiza recuperación frente a todos los errores. |
| Entrega | Entrega inmediatamente si el destinatario está conectado y hay espacio; si no, guarda un pendiente. Intenta entregar pendientes al conectarse. |
| Vencimiento | Marca vencidas y procura devolver el artículo al vendedor o almacenarlo como pendiente. Revisión inicial y cada 60 segundos por defecto. |
| Mensajes y sonidos | Archivo messages.yml con valores por defecto, colores y placeholders. Sonidos configurables al abrir, pulsar, comprar, vender y en determinados errores. |
| Menús configurables | Títulos, filas, materiales, rellenos, slots, botones, lore y acciones. Valida filas 1–6, materiales y ciertos conflictos de slots. |
| Persistencia | SQLite en `plugins/MDVEconomy/database.db`; tablas players, auctions y pending_items, claves foráneas, modo WAL, índices y migración de default_payment_method. Un ejecutor serializa el acceso SQL. |
| Datos de artículos | Serialización binaria de Paper convertida a Base64, preservando metadatos según esa API. |
| Comandos y autocompletado | Delegados en CommandTree v1.0.2. Se inspeccionó también el bytecode adjunto; admite los constructores protegidos `(CommandSender, JavaPlugin)` mediante reflexión. |

### Funciones anunciadas pero no implementadas

- **Mis artículos en venta, Artículos pendientes, Banco y Ordenar:** los botones existen en config.yml, pero sus acciones `OPEN_PLAYER_LISTINGS_MENU`, `OPEN_PENDING_ITEMS_MENU`, `OPEN_BANK_MENU` y `OPEN_SORT_MENU` no tienen implementación. El clic puede sonar sin abrir nada.
- **Trueque/ofertas con ítems:** `offerPurchaseWithItems` y `completePurchaseWithItems` retornan `null`. No hay interfaz funcional conectada; un consumidor que espere un CompletableFuture fallará.
- **Cancelar una publicación:** existe el enum CANCELLED, pero no un flujo de cancelación.
- **Alias raíz `/subastas`:** aparece en `CAuctionHouse.getAliases()`, pero no se registra en plugin.yml. Bukkit no lo registra a partir del alias interno del árbol.
- No hay banco propio, comandos de saldo/transferencia, panel administrativo, filtros/ordenación elegibles, menú Bedrock específico ni recuperación manual de pendientes implementados.

## Cambios entregados para el build

| Archivo | Cambio |
|---|---|
| `.github/workflows/build.yml` | Nombre correcto; push a todas las ramas/tags, pull_request y manual; Java 21 con caché Maven; `clean verify`; nombre leído de `project.build.finalName`; verificación del JAR y SQLite; ZIP del JAR final; fallo si falta el artefacto; permisos de lectura, sin credenciales persistidas, concurrencia y timeout de 20 minutos. |
| `pom.xml` | Mantiene versión 1.0.0 y dependencias originales. Añade Maven Central primero para evitar consultas innecesarias a repositorios externos; fija versiones de plugins de clean/resources/surefire/jar; filtra únicamente plugin.yml; Shade fusiona descriptores de servicios, excluye firmas de dependencias y deja de generar un POM reducido. Son mejoras de empaquetado/previsión, no errores de Java demostrados. |
| `build_replace.py` | Corrige el rechazo de una primera compilación sin target; elimina el JDK absoluto del autor; usa Maven del PATH y JAVA_HOME heredado; acepta MDV_PLUGINS_DIR; aplica timeout a Maven. No se ejecutó la copia a ningún servidor. |
| `.gitignore` | Excluye target, dist, POM reducido y cachés Python. |
| `scripts/verify_jar.py` | Comprueba recursos, versión sustituida, clases incluidas/excluidas, bytecode Java 21, servicio JDBC, biblioteca Linux, firmas y CRC del JAR. |
| `scripts/RepositorySmokeTest.java` | 12 comprobaciones sobre los repositorios reales y el SQLite incluido en el JAR, con esquema de prueba en memoria. |
| Documentación | README, este informe, inventario y evidencias de verificación. |

El ZIP fuente excluye target, dist, `ct_extract` y `dependency-reduced-pom.xml`: estos dos últimos eran material generado, no fuentes de compilación. Se inspeccionaron antes de excluirlos y quedan identificados en el inventario. Se conservan la licencia, los fuentes de juego, los recursos y la configuración del editor. Ninguno de los 30 Java de producción ni de los tres YAML se modificó.

Actualizar `<version>` en pom.xml actualiza plugin.yml, JAR y ZIP automáticamente. No es necesario editar el workflow para cada actualización normal. Los repositorios SNAPSHOT, los cambios mayores de Java y las futuras políticas de Actions siguen requiriendo mantenimiento eventual.

## Bugs confirmados por inspección, pendientes de corrección

### B01 — Crítico: publicación sin retirar el artículo

**Evidencia:** `commands/auction_house/CSell.java`, handleSell; `auction/AuctionService.java`, createListing.

Se clona el artículo y se crea la fila en SQLite. Después, en otra tarea, se retira de la mano solo si todavía es similar y conserva suficiente cantidad. Si el jugador mueve, deja caer o cambia la pila durante esa ventana, no se retira nada y la publicación permanece ACTIVE. Varias solicitudes antes de la retirada también pueden producir publicaciones duplicadas, hasta el límite configurado.

**Prueba pendiente en Paper:** publicar y cambiar inmediatamente de slot, o repetir el comando con latencia SQL. **Corrección propuesta:** reservar/retirar la pila en el hilo principal antes de publicar, bloquear operaciones concurrentes por jugador y diseñar devolución persistente en caso de error. Una simple comparación posterior no basta.

### B02 — Alto: cobro sin compensación ante error SQL

**Evidencia:** AuctionService.purchaseBank, purchaseInventoryCash y markSoldOrRefund.

El débito ocurre antes del UPDATE SQL. Solo hay reembolso cuando el UPDATE termina normalmente con `false`. Una excepción SQL tras cobrar termina el futuro con error y no ejecuta refund. El comprador puede perder el pago sin compra. Hace falta una transacción de negocio persistida con estados y compensaciones, además de la transacción SQL.

### B03 — Alto: falla el depósito al vendedor pero se informa venta exitosa

**Evidencia:** AuctionService.completePurchase y notifySellerItemSold.

Si Vault devuelve `false`, se escribe una advertencia, se notifica que el dinero fue depositado, se entrega el artículo y se retorna éxito. No hay deuda pendiente ni reintento. Si Vault lanza una excepción, la venta ya puede estar SOLD sin completar entrega/cobro.

### B04 — Alto: pérdida de artículos pendientes por carrera y marcado anticipado

**Evidencia:** PendingItemService.processQueue.

Se comprueba espacio en una tarea, se marca CLAIMED en la base y luego se añade el artículo en otra tarea. En el intervalo el inventario puede llenarse o el jugador desconectarse. No se revalida conexión/espacio antes de añadir ni se procesan los sobrantes de addItem. Un cierre después del claim también deja el artículo reclamado sin entrega recuperable. La prueba de idempotencia SQL aprobada no elimina este problema.

### B05 — Alto: vencimiento y devolución no son atómicos

**Evidencia:** AuctionService.processExpiredAuctions.

Primero se confirma EXPIRED en una transacción; después se solicita entrega o almacenamiento en un futuro separado que no se encadena ni se espera. Si esa segunda operación falla, la siguiente revisión no vuelve a seleccionar la fila EXPIRED. Puede perderse la devolución. Persistir EXPIRED y su obligación de devolución en una misma transacción evita esa ventana SQL.

### B06 — Alto: venta SOLD sin entrega durable

**Evidencia:** AuctionService.markSoldOrRefund, completePurchase y deliverPurchasedItem; PendingItemService.storeOrDeliverForUuid.

SOLD, depósito y entrega/guardado son pasos independientes. Un fallo de deserialización, almacenamiento, apagado o caída entre esos pasos no tiene recuperación registrada. El esquema de auctions tampoco guarda comprador, método de pago ni estado del abono, lo que dificulta reconstruir operaciones incompletas.

### B07 — Alto: reembolso no verificado

**Evidencia:** AuctionService.markSoldOrRefund y DenaresCash.give.

El booleano devuelto por el reembolso bancario se ignora, aunque el mensaje asegura que se devolvió. En efectivo, la creación del DENAR puede fallar; si falta espacio se arrojan monedas al suelo en vez de conservar una devolución pendiente. El resultado puede ser pérdida o recogida por otro jugador.

### B08 — Alto: dependencia de ejecución MMOItems no declarada

**Evidencia:** `src/main/resources/plugin.yml` solo declara Vault; DenaresCash enlaza directamente MMOItems.

La compilación usa MMOItems/MythicLib como provided. No se garantiza su presencia/orden desde MDVEconomy. Sin MMOItems, una venta o pago que invoque DenaresCash puede fallar al resolver clases; compilar no instala esas dependencias. Debe declararse dependencia obligatoria o implementarse una integración opcional real. No se cambió la política de carga en esta entrega.

### B09 — Medio: faltan manejadores de menús y contratos devuelven null

**Evidencia:** MenuClickListener.handleOption, AuctionHouseMenu.handleConfiguredAction y métodos de trueque de AuctionService. Los cuatro botones mencionados no realizan su acción y los dos métodos de trueque no retornan futuros válidos.

### B10 — Medio: no se reintentan pendientes al liberar espacio

**Evidencia:** PlayerConnectionListener solo llama deliverPending en PlayerJoinEvent. Si el inventario está lleno al entrar, vaciarlo después no activa otra entrega. El botón de pendientes tampoco funciona. Es necesario reconectarse o que otro código invoque el servicio.

### B11 — Medio: arrastres del inventario no controlados

**Evidencia:** MenuClickListener escucha InventoryClickEvent, no InventoryDragEvent. Arrastrar ítems sobre slots vacíos del menú puede introducir ítems del jugador en una interfaz temporal; luego pueden sobrescribirse o quedar inaccesibles. La ausencia del manejador está confirmada; cuantificar pérdida/otras vías requiere Paper. No se afirma que este caso permita extraer los ítems de muestra.

### B12 — Medio: la recarga no cambia el período de vencimiento

**Evidencia:** MDVEconomyPlugin.onEnable y reloadPluginConfig. El período se captura al iniciar y no se reprograma al recargar. Cambiar expire-check-interval-seconds exige reiniciar para que se aplique. Los menús ya abiertos también conservan parte de su configuración anterior.

### B13 — Bajo: alias raíz sin registrar y errores de comando poco claros

**Evidencia:** CAuctionHouse.getAliases, plugin.yml y bytecode de CommandTreeManager. `/subastas` no queda registrado por el descriptor. Argumentos desconocidos de `/ah` pueden terminar sin respuesta útil porque el manager ignora el booleano de execute. El autocompletado de la biblioteca no filtra todos los subcomandos por permiso; reload sí valida permiso al ejecutarse.

### B14 — Bajo: límite ocupado por publicaciones ya vencidas

**Evidencia:** AuctionRepository.countActiveByPlayer filtra estado ACTIVE, pero no expires_at. Hasta que pase la revisión periódica, una publicación vencida sigue consumiendo cupo, aunque ya no se vea en el listado.

## Posibles problemas y riesgos adicionales

| Riesgo | Evidencia e impacto potencial |
|---|---|
| Acceso a API de Paper fuera del hilo principal | processExpiredAuctions deserializa/modifica ItemStack en continuaciones SQL; processQueue lee isOnline y deserializa desde continuaciones asíncronas. Revisar la seguridad de cada API en la versión exacta y mover las operaciones de juego al hilo principal. No se reprodujo una excepción por hilo. |
| Cierre y tareas pendientes | DatabaseService.close espera hasta 5 s y cierra mientras continuaciones pueden solicitar tareas o SQL nuevo; submit no convierte RejectedExecutionException en un futuro fallido. Los schedulers pueden rechazar tareas al desactivarse. No hay un drenaje durable de operaciones completas. |
| Datos corruptos | Base64, UUID y enums se convierten sin aislamiento por registro. Un artículo inválido puede interrumpir la página, cola o lote de vencimientos. No hay recuperación/cuarentena individual. |
| Configuración incompleta o incoherente | ignored-slots vacío causa pageSize=0 y error; duplicados no se rechazan; duración, precio máximo e intervalo no reciben validación global. Algunas configuraciones se aceptan al recargar y fallan recién al abrir un menú. |
| Colores del lore | applyPlaceholders traduce & a §, luego parseText usa un serializador de &. Esa mezcla puede dejar colores sin interpretar correctamente. Verificar visualmente y unificar el serializador. |
| Restricciones de materiales | requireMaterial descarta aire/material desconocido, pero no comprueba isItem; un material no representable como ítem puede fallar al construir ItemStack. |
| API monetaria pública | createListing admite double sin validar precio finito/positivo, cantidad ni duración. El comando actual restringe a enteros válidos; otros consumidores o datos editados podrían saltar esas restricciones. INVENTORY redondea, Vault cobra el double. |
| Integraciones antiguas | Se conservaron MMOItems 6.9.5-SNAPSHOT y MythicLib 1.6.2-SNAPSHOT para no cambiar comportamiento. Poder compilar sus APIs no demuestra que esas versiones funcionen en Paper 1.21.6. |
| Dependencias SNAPSHOT y servicios externos | Paper/MMOItems/MythicLib pueden cambiar bajo las mismas coordenadas; JitPack/Phoenix/Paper pueden estar caídos. El build es automático, pero no reproducible bit a bit ni independiente de la red. |
| Crecimiento y consultas | No hay purga de SOLD/EXPIRED/CLAIMED. findExpirable y findPendingForPlayer cargan listas completas. countActiveByPlayer carece de índice específico por vendedor/estado. Puede aumentar latencia y memoria con uso prolongado. |
| Paginación | Ordena solo por created_at, sin desempate por id; altas/bajas entre páginas pueden mover resultados. No es por sí solo un fallo monetario: el UPDATE vuelve a validar la venta. |
| Reembolso de denares | Se recrea un DENAR desde la plantilla actual, no necesariamente el mismo ítem retirado. Cambios de plantilla o ítems falsificados por otras herramientas pueden alterar equivalencias. Solo se verifica tipo/ID de MMOItems, no procedencia. |
| Helper local | RESET_CONFIG=True elimina toda la carpeta del plugin, incluyendo la base. El selector antiguo de JAR usa coincidencias/fallback; con varios candidatos podría elegir uno no deseado. No ejecutar contra un servidor activo. Se conservaron esas funciones y no se realizó despliegue. |
| APIs obsoletas | javac advirtió sobre Sound.valueOf, marcado para eliminación, y MessageService. No fallan en el build probado; pueden romperse al actualizar Paper. |
| Auditoría de terceros limitada | Se inspeccionó el bytecode CommandTree adjunto y los POM resueltos. No se auditó íntegramente el código de SQLite/Paper/MMOItems/MythicLib ni se hizo un escaneo CVE. No se afirma ausencia de vulnerabilidades de terceros. |

## Verificaciones ejecutadas

- Compilación limpia real de 30 fuentes con `mvn --batch-mode --no-transfer-progress clean verify`: aprobada. Segunda ejecución con el POM final y evaluación de project.build.finalName: aprobada. Evidencia: `verification/maven-build.log`.
- Resolución real de las dependencias desde repositorios públicos, sin usar ct_extract como sustituto de CommandTree.
- JAR final: recursos presentes, plugin.yml con versión resuelta, clases propias Java 21, CommandTree/SQLite incluidos, APIs provided ausentes, driver JDBC fusionado, biblioteca nativa Linux y CRC correctos. Evidencia: `verification/artifact-check.log`.
- SQLite real cargado desde el JAR: ID de jugador estable/nombre con comilla, preferencia por defecto y cambio, listado excluye vencidas, página, única venta exitosa, rechazo de venta vencida, expiración idempotente, source_id nulo, única reclamación, exclusión de reclamados y claves foráneas. **12/12 aprobadas**; evidencia: `verification/sqlite-check.log`.
- Actionlint 1.7.12: sin errores. No se ejecutó ShellCheck, que no está instalado. No se ejecutaron jobs remotos en GitHub.
- Sintaxis de Python, estructura del ZIP final, identidad de los fuentes originales de producción y simulación del cambio de versión: ver `verification/delivery-checks.txt`.
- Maven informa **No tests to run** porque no hay suite JUnit. Las 12 comprobaciones son un programa adicional que el workflow ejecuta explícitamente. No prueban inventarios, proveedor Vault, migración con datos reales, cierres ni carreras en Paper.

Advertencias restantes del build: APIs obsoletas, detección de procesadores de anotaciones en el classpath y solapamiento de META-INF/MANIFEST.MF entre dependencias sombreadas. El JAR pasó las verificaciones, pero el funcionamiento completo del plugin sigue pendiente de pruebas de integración.

## Orden propuesto para una corrección funcional posterior

1. Evitar publicación sin retiro (B01) con reserva y recuperación durable.
2. Registrar cada compra, comprador, método y obligaciones de pago/entrega; compensar errores e implementar reintentos idempotentes (B02–B07).
3. Corregir entrega de pendientes y vencimientos con persistencia transaccional de las obligaciones.
4. Declarar/verificar MMOItems y MythicLib; probar el DENAR y el proveedor Vault.
5. Bloquear arrastres, implementar botones pendientes o identificarlos como no disponibles, validar configuración y reprogramar el timer en reload.
6. Probar en Paper con dos compradores, cambios rápidos de inventario, desconexiones, inventarios llenos, reinicio entre pasos, SQL fallido y proveedor Vault que rechace/arroje errores. Guardar copia de database.db y sus WAL/SHM mediante un backup consistente antes de migraciones.

## Referencias técnicas consultadas

- [GitHub: compilar y probar con Maven](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-maven).
- [setup-java: configuración y caché Maven](https://github.com/actions/setup-java).
- [checkout](https://github.com/actions/checkout) y [upload-artifact](https://github.com/actions/upload-artifact).
- [Paper: preparación de proyectos y repositorio Maven](https://docs.papermc.io/paper/dev/project-setup/).
- [CommandTree del autor](https://github.com/k3vndev/command-tree).

Los hallazgos del plugin proceden del ZIP inspeccionado; las referencias externas se usaron para contrastar la configuración de compilación.

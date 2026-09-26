# MDVEconomy 2.0.0: cambios y verificaciones

## Entrega

Se reemplazó el modelo de compra directa por pujas y trueques a partir del proyecto auditado. El banco nuevo queda pendiente, como se pidió. La integración existente con saldos Vault se usa para pujar, devolver pagos y cobrar ventas.

### Funciones y correspondencia con las capturas

| Pantalla / requisito | Implementación |
|---|---|
| Principal /ah | Tres botones: Subastas, Gestiones y Banco; el banco informa que todavía no está disponible. |
| Listado público | Categorías de equipamiento, armaduras, herramientas, consumibles, materiales y otros, sin filtro al abrir; selección con brillo. |
| Categorías MMOItems / vanilla | Tipos MMOItems y patrones de materiales editables por sección. |
| Orden y calidad | Tolva: recientes, precio alto/bajo, vendedor con más publicaciones, más pujas/ofertas. Ojo: tiers configurables por ID de MMOItems. |
| Pujas | Pepita retira efectivo; bloque retira Vault. Nueva puja = máximo +1. Si ya liderás, solo se retira 1 adicional. |
| Cambio de precio | Un menú desactualizado se refresca sin cobrar; exige otro clic con el nuevo importe visible. |
| Historial | Últimas tres pujas con nombre, importe y fecha. |
| Trueque | Hasta cinco pilas, incluidos denares; selección desde inventario y reserva al confirmar. |
| Vista pública de ofertas | Libro con participantes e ítems, sin botones de aceptación para compradores. |
| Gestiones propias | Cinco publicaciones por página, estado y entregas; crear y abrir participaciones desde el girasol. |
| Más espacios | Cupo base 5, rangos configurables por permiso; paginación independiente del cupo. |
| Detalle / cancelación | Dueño puede cancelar mientras está activa; se crean devoluciones. Al haber entregas, el botón pasa a esmeralda. |
| Vencimiento de puja | Ganador reclama el artículo; vendedor reclama el total por Vault. Sin ganador se devuelve el artículo. |
| Vencimiento de trueque | No acepta ninguna oferta automáticamente; devuelve todas las reservas. |
| Aceptar trueque | Cabezas/nombres y cinco ítems por fila; acepta solo el dueño, entrega a ambas partes y devuelve las demás ofertas. |
| Crear publicación | Pila completa con datos personalizados, prohibición de vender DENAR, cancelar/publicar y alternar modos. |
| Duración | Base 10 horas; 16, 24 y 48 habilitadas por sus permisos. |
| Menús editables | Once archivos en menus, slots, títulos, nombres, lore, rellenos y variantes. Cabezas Base64 y CustomModelData para botones. |
| Flechas | Se ocultan en los límites; en una única página no hay flechas. |
| Entregas | Reclamación explícita, sin arrojar objetos al suelo. Efectivo grande se entrega por partes según espacio. |
| Maven / GitHub | Java 21, pruebas en verify, empaquetado automático sin editar workflow por versión. |

Las tres capturas repetidas del menú de gestiones se interpretaron como una sola pantalla. Los botones se ubicaron siguiendo las referencias; son configurables. La selección de un artículo es una vista previa y el retiro se hace al confirmar, para que cerrar/desconectarse no pierda artículos. Las pujas superadas quedan reclamables de inmediato. Un reembolso no se reutiliza automáticamente al volver a pujar: se reclama desde el girasol.

## Correcciones respecto de la auditoría anterior

- Se eliminó la publicación antes de retirar el ítem: ahora se valida la pila completa, se registra la intención de retiro y solo se publica tras retirarla. Un inventario que cambió rechaza la operación.
- Se eliminaron los futuros de trueque que retornaban null y los botones sin implementación de la casa de subastas. Solo Banco queda deliberadamente sin funcionalidad nueva.
- Se eliminaron las carreras entre tareas SQL y tareas de inventario de los flujos anteriores. Las acciones se serializan en el hilo del servidor; los eventos del menú se cancelan y sus acciones se realizan después del evento.
- Vencimiento/cancelación/aceptación y creación de las obligaciones de entrega se confirman en una única transacción SQLite.
- Se conserva el resultado de cada retiro/entrega en un registro de operaciones. Si hay incertidumbre no se repite el efecto ni se asegura falsamente que se devolvió dinero: queda REVIEW.
- Los depósitos Vault rechazados conservan la entrega READY. El vendedor no recibe un mensaje de cobro completado cuando el proveedor rechazó el depósito.
- La reclamación valida dueño/estado, maneja falta de espacio sin perder artículos y evita la doble entrega. No descarta sobrantes de addItem: calcula primero una distribución completa y aplica el inventario resultante.
- Se bloquean clics de extracción, shift/doble clic, teclas numéricas, arrastres, intercambio de mano y drops mientras se usa el menú.
- Se declara Vault, MMOItems y MythicLib como dependencias; se registra `/subastas`; la recarga reprograma el timer y valida menús antes de sustituirlos.
- Se usa una sola representación de texto para colores; el menú nunca entrega el ítem de muestra con lore modificado.
- Se cargan explícitamente las clases JDBC desde el classloader del plugin y se incluye el driver y sus bibliotecas nativas en el JAR final.

La solución reduce los errores conocidos y hace recuperables las operaciones incompletas; no promete una transacción atómica entre Vault, SQLite y los archivos de jugador. Esos sistemas no ofrecen una transacción compartida. La conciliación manual de casos inciertos está documentada en README y no se debe realizar adivinando el resultado.

## Arquitectura

| Clase | Responsabilidad |
|---|---|
| MDVEconomyPlugin | Inicio, dependencias, copia/migración, comandos, recarga, timer y cierre. |
| MarketSettings | Límites, permisos, duración e intervalos. |
| Models | Registros de publicación, oferta, puja, entrega, operación y filtros. |
| MarketStore | Esquema SQLite, conexión, consultas y transacciones. |
| MarketEngine | Reglas de negocio, reservas, cierre, devoluciones, reclamaciones y conciliación. |
| ItemCodec | Serialización Paper/Base64, incluidos datos personalizados. |
| ItemCatalog | Denares MMOItems, categorías y tiers. |
| InventoryTransfers | Planificación/verificación de cambios de inventario y llamadas a Vault. |
| MenuFiles | Carga y validación de definiciones editables. |
| IconFactory | Colores, placeholders, materiales, brillo y cabezas Base64. |
| MarketView | Estado de una pantalla y selecciones. |
| MarketMenus | Representación y acciones de las once pantallas. |
| MenuListener | Protección de inventarios y despacho de acciones al siguiente tick. |

Las pantallas nuevas se preparan en un holder nuevo antes de mostrarse; un error al construirlas no cambia la relación entre ítems visibles y acciones del menú anterior.

## Pruebas realizadas

**51 pruebas: 0 fallos, 0 errores, 0 omitidas. BUILD SUCCESS.**

Entorno: Windows, Temurin JDK 21.0.12.1, Maven 3.9.9. Se ejecutó una compilación limpia del proyecto final con `clean verify`, además de evaluar el nombre final desde el POM. El nombre obtenido fue `MDVEconomy-2.0.0`.

### 32 pruebas de motor y SQLite

Cubren: incremento de solo 1 para el líder; cobro total al nuevo postor; devoluciones en su medio original; reofertar tras ser superado; fondos insuficientes; pila rechazada antes de publicar; límite de publicaciones; compra propia/modo incorrecto; precio máximo; corte por vencimiento; cierre idempotente sin pujas; aceptación de trueque y devolución a perdedores; trueque vencido sin autoaceptación; cancelación; autorización del dueño y pertenencia de la oferta; tamaño/límite de ofertas; orden reciente y últimas tres pujas; pertenencia y reclamación única; Vault rechazado; falta de espacio; retiro incierto y bloqueo; fallo SQL después de retirar; entrega incierta y conciliación; reintento autorizado de entrega no ejecutada; recuperación al reiniciar; rollback del vencimiento; categorías/tiers/paginación/participantes; ordenación; migración idempotente; devolución física por partes; conciliación de entrega parcial; inventario sin capacidad; copia SQLite consistente.

Se utiliza SQLite real con el esquema de producción. Las retiradas y entregas externas de esos tests son adaptadores simulados; no se conectaron a un proveedor Vault ni a inventarios de Minecraft.

### 19 pruebas de menús

Once validan los layouts por defecto (slots sin solapamiento, entradas y estructura); las restantes prueban slots duplicados, número incorrecto de entradas, Base64 inválido/válido y dominio de texturas, ausencia de filler, acción desconocida y decoración superpuesta. No son capturas ni pruebas visuales de un cliente Minecraft. La comprobación runtime de que un material sea un ítem se realiza al cargar en Paper; no se simula su Registry en los tests.

### Comprobaciones de entrega

- `scripts/verify_jar.py`: descriptor y versión resuelta, once menús, recursos, bytecode Java 21, SQLite y servicio JDBC, biblioteca nativa Linux, CRC, exclusión de firmas heredadas y APIs provided.
- Actionlint 1.7.12: workflow aceptado. No hay ShellCheck instalado; no se ejecutó un job remoto en GitHub.
- ZIP fuente sin target, caches ni archivos generados de la versión anterior; conserva `.github`, código, pruebas, licencia y documentación.
- ZIP de servidor con el JAR final y guía; nunca usa el original sin dependencias que produce Shade.

Los logs y los resúmenes JUnit se incluyen en `verification/`. Quedan advertencias no bloqueantes: algunas APIs de Paper obsoletas, detección de procesadores de anotaciones y el MANIFEST compartido al sombrear SQLite; las pruebas muestran además un logger SLF4J sin proveedor en el proceso de pruebas.

## Lo que falta verificar en tu servidor

No se dispuso de un servidor Paper con tus JAR de MMOItems/MythicLib, tu proveedor Vault y tu configuración de DENAR/tiers. Antes de usar con jugadores reales, verificar:

1. Inicio de las dependencias y lectura del DENAR y tiers reales.
2. Apariencia de los menús, cabezas Base64 y metadatos de tus ítems.
3. Dos jugadores pujando, cambiando de método de pago, superándose y reclamando devoluciones.
4. Trueques con cinco pilas, aceptación, cancelación y vencimiento.
5. Inventario lleno, cierre/desconexión durante selección y reclamación.
6. Los permisos de cupo/duración y las páginas con más publicaciones/ofertas.
7. Copia de la base antes de migrar y revisión del listado de devoluciones antiguas.

No hay banco nuevo, compatibilidad Folia, base multi-servidor ni purga automática del historial. La base local se usa en el hilo del servidor; aunque hay paginación y lotes, la carga y un disco lento pueden afectar el tiempo por tick. La clasificación de categorías se guarda al publicar. Las políticas que quedaron abiertas en la descripción se resolvieron como se explica en README; son ajustables en próximas versiones.

## Referencias de API

- [Texturas de perfiles de Paper](https://jd.papermc.io/paper/1.21.6/org/bukkit/profile/PlayerTextures.html): las URL de piel deben apuntar al servidor de texturas de Minecraft.
- [Metadatos de ítems de Paper](https://jd.papermc.io/paper/1.21.6/org/bukkit/inventory/meta/ItemMeta.html): nombres, lore y propiedades de representación.

Los resultados de compilación y tests se basan en las ejecuciones locales incluidas, no en esas referencias externas.

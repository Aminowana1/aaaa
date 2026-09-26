> Versión actual: **2.0.2**, con command-tree v1.0.2 restaurado. Ver [CAMBIOS-2.0.2.md](CAMBIOS-2.0.2.md). Las referencias a la compilación anterior son históricas.

# MDVEconomy 2.0.0 — Pujas y trueques

Casa de subastas para Paper API 1.21.6 y Java 21. Reemplaza la compra directa de la versión 1 por publicaciones de **pujas** y **trueques**, con gestión, historial y entregas reclamables.

## Compilar con Maven / GitHub

Subí el contenido de esta carpeta a la raíz del repositorio. Deben quedar `pom.xml`, `src`, `scripts` y `.github` en esa raíz.

```text
mvn --batch-mode --no-transfer-progress clean verify
python scripts/verify_jar.py target/MDVEconomy-2.0.0.jar
```

Requiere JDK 21 y Maven 3.9. El JAR para instalar es `target/MDVEconomy-2.0.0.jar`, no `original-*.jar`.

El workflow `.github/workflows/build.yml` compila, ejecuta las pruebas, valida el JAR y genera el ZIP del servidor automáticamente con cada push, pull request o ejecución manual. Obtiene el nombre final desde Maven. Al actualizar el código o `<version>` en el POM **no hace falta editar build.yml**. No necesita tokens personales ni GitHub Packages. Descargá el artefacto de la ejecución en Actions. Sigue requiriendo acceso a Maven Central, Paper, JitPack y Phoenix; cambios de Java/Actions/repositorios pueden necesitar mantenimiento futuro.

## Instalación

1. Detené el servidor y hacé una copia completa de la carpeta `plugins/MDVEconomy`.
2. Reemplazá el JAR anterior; no dejes dos versiones del mismo plugin.
3. Instalá Vault, un proveedor de economía registrado en Vault, MMOItems y MythicLib compatibles con tu versión de Paper. El plugin los declara como dependencias obligatorias. Las APIs utilizadas para compilar no incluyen ni reemplazan esos plugins.
4. Configurá en MMOItems `MISCELLANEOUS:DENAR` o cambiá `cash.type` / `cash.id`.
5. Iniciá, comprobá la consola y probá `/ah` con dos jugadores en un servidor de pruebas.

**La compilación y las pruebas automáticas están verificadas. No se ejecutó Minecraft con tus plugins reales.** Las versiones de API conservadas de la base (MMOItems 6.9.5-SNAPSHOT y MythicLib 1.6.2-SNAPSHOT) no certifican por sí solas compatibilidad de sus JAR de servidor con Paper 1.21.6.

## Flujo de uso

- `/ah` o `/subastas`: menú principal con Subastas, Gestiones y Banco. Banco muestra “próximamente”; no tiene funciones bancarias nuevas. El saldo Vault sí se usa para pujar y cobrar ventas.
- **Subastas:** abre sin filtro de categoría ni calidad. A la izquierda: equipamiento, armaduras, herramientas, consumibles, materiales y otros. Clic en una categoría la selecciona y hace brillar el icono; otro clic en la misma vuelve a todo.
- **Tolva:** alterna recientes, mayor puja, menor puja, vendedores con más publicaciones activas y mayor cantidad de pujas/ofertas. Los trueques no tienen precio monetario y valen 0 para ordenar por precio.
- **Ojo de ender:** alterna todos los tiers y los IDs configurados. La clasificación se guarda al publicar; cambios de categorías/tier en config se aplican a publicaciones nuevas.
- **Puja:** comienza en 1 denar. Cada acción ofrece 1 más que el máximo. El actual ganador solo paga la diferencia de 1; cualquier otro paga el nuevo total. Pepita = efectivo, bloque = Vault. Se puede combinar la procedencia de los incrementos. Si el precio cambió desde que abriste la pantalla, se actualiza y pide otro clic sin cobrar.
- **Devolución de una puja superada:** queda para reclamar en el girasol, en el mismo medio que se retiró: Vault o denares físicos. Las devoluciones antiguas no se gastan automáticamente en una nueva puja.
- **Historial:** muestra las últimas tres pujas, más recientes primero.
- **Trueque:** elegí hasta cinco pilas con clic en tu inventario. Admite denares físicos. Confirmar retira las pilas y registra la oferta. El libro deja ver las ofertas a cualquier comprador, pero solo el vendedor puede aceptarlas.
- **Gestiones:** cinco publicaciones por página; las activas aparecen primero y después el historial. Esmeralda para crear; girasol para tus participaciones y entregas. Los permisos amplían el cupo de publicaciones activas.
- **Detalle propio:** cancelar está disponible mientras la publicación está activa. Al terminar y haber entregas, ese botón se convierte en esmeralda para abrir las entregas reclamables. En pujas muestra historial y ganador/precio; en trueques muestra el trigo para gestionar ofertas.
- **Al aceptar un trueque:** se crean entregas para vendedor y comprador; las demás ofertas se devuelven. Si vence sin aceptar, todos recuperan sus artículos. No hay aceptación automática de trueques.
- **Al cerrar una puja con ganador:** el comprador reclama el artículo y el vendedor reclama el total en Vault. Sin pujas, se devuelve el artículo. Cancelar devuelve todas las reservas.
- **Crear:** clic en una pila del inventario, modo pujas/trueque, duración y confirmar. Se conserva la pila completa, con cantidad y datos personalizados. No se puede publicar DENAR como artículo de venta.

En los formularios, los artículos son una **vista previa**: permanecen en tu inventario hasta confirmar. Cerrarlos, volver o desconectarse antes de confirmar no mueve artículos. Al confirmar se verifica que la pila siga siendo exactamente la seleccionada. Los clics rápidos, shift, números, doble clic, arrastres e intercambio de mano no extraen ítems del menú.

Las flechas solo aparecen cuando existe la página anterior/siguiente. Una sola página no muestra flechas. El menú de ofertas muestra cuatro ofertas por página, cada una con cabeza/nombre, hasta cinco pilas y aceptar; se ordena por llegada descendente.

## Permisos

| Permiso | Efecto |
|---|---|
| Sin permisos especiales | 5 publicaciones activas, duración 10 horas. |
| `mdveconomy.slots.vip` | 10 activas por defecto. Editable en config. |
| `mdveconomy.slots.elite` | 20 activas por defecto. Editable en config. |
| `mdveconomy.duration.16` | Habilita 16 horas en el reloj. |
| `mdveconomy.duration.24` | Habilita 1 día. |
| `mdveconomy.duration.48` | Habilita 2 días. |
| `mdveconomy.reload` | Recarga configuración, mensajes, menús y período del temporizador. Por defecto OP. |
| `mdveconomy.admin` | Revisión y conciliación de operaciones inciertas. Por defecto OP. |

Cada duración adicional requiere su permiso; tener 48 h no concede automáticamente 16/24 h. Podés añadir rangos y permisos propios en `limits.ranks`. Se toma el mayor cupo permitido. Los permisos de cupo/duración tienen default false, también para OP sin concesión explícita.

## Configuración de categorías y tiers

`plugins/MDVEconomy/config.yml`:

```yaml
categories:
  equipment:
    mmoitems-types: [SWORD, STAFF, HAMMER, ACCESSORY, TU_TIPO_PERSONALIZADO]
    vanilla: ['*_SWORD', BOW, CROSSBOW, SHIELD]
tiers:
  COMMON: 'Común'
  UNCOMMON: 'Poco común'
  SPECIAL: 'Especial'
  RARE: 'Raro'
  EPIC: 'Épico'
  LEGENDARY: 'Legendario'
```

Las claves de `mmoitems-types` y `tiers` son los **IDs reales** de MMOItems, no sus nombres visibles; adaptalos a tu servidor. El tier se lee de `MMOITEMS_TIER`. Los tipos MMOItems tienen prioridad sobre el material vanilla. Después se recorren los patrones vanilla en orden; lo que no coincida cae en `other`. Los comodines `*` funcionan en materiales. Podés añadir tipos MMOItems a cualquiera de las seis secciones. Para una sección visual adicional, creá su categoría y añadí un botón `CATEGORY:su_clave` en browse.yml usando un slot libre.

`max-bid` limita el importe monetario; `max-offers-per-player` limita las ofertas de una persona por publicación (por defecto 5 ofertas, cada una con hasta 5 pilas). La revisión de vencimientos se realiza cada 5 s, hasta 100 filas por lote, ambos configurables.

## Menús editables y cabezas Base64

Los 11 archivos se generan en `plugins/MDVEconomy/menus/`:

| Archivo | Pantalla |
|---|---|
| main.yml | Principal de /ah |
| browse.yml | Categorías, ofertas, orden y tiers |
| bid.yml | Pujar por Vault/efectivo |
| trade.yml | Formulario de trueque |
| manage.yml | Tus publicaciones |
| owner.yml | Detalle propio/participación, cancelar y reclamar |
| offers.yml | Ofertas recibidas, vista pública o gestión del dueño |
| create.yml | Crear publicación |
| participations.yml | Historial de tus pujas/trueques |
| claims.yml | Devoluciones, cobros y artículos reclamables |
| history.yml | Últimas tres pujas |

Los slots son **0–53**, de izquierda a derecha y de arriba abajo. Ejemplo: primera fila 0–8, última 45–53. Cambiá `title`, `rows`, `filler`, `buttons`, `content-slots`, `input-slots`, `preview-slot`, las filas de `offer-slots`, `name` y `lore`. No superpongas botones, contenido, entradas ni decoraciones. El formulario de creación requiere un slot y el de trueque cinco. Cada fila de ofertas requiere cinco slots, una cabeza y aceptar.

Ejemplo de botón de volver:

```yaml
buttons:
  back:
    slot: 49
    material: PLAYER_HEAD
    name: '&eVolver'
    lore: ['&7Regresar al menú anterior']
    texture: 'PEGAR_AQUI_EL_BASE64_COMPLETO'
    action: BACK
```

`texture` acepta el valor Base64 de texturas de Minecraft, cuyo JSON contiene `textures.SKIN.url` apuntando a `textures.minecraft.net`. Dejalo vacío para usar el material. No es un enlace de una web de cabezas. También se admite `custom-model-data` numérico y `glow` para botones. `variants` define el icono alternativo del modo y de la reclamación. El nombre del artículo real se conserva; podés añadir `listing-name` si querés reemplazar el nombre de su representación. `listing-lore` y `selection-lore` son editables.

Placeholders de publicaciones: `{id}`, `{seller}`, `{mode}`, `{status}`, `{price}`, `{next}`, `{cost}`, `{winner}`, `{time}`, `{tier}`, `{offers}`, `{claimable}`. Generales: `{page}`, `{hours}`, `{sort}`, `{active}`, `{limit}`, `{selected}`. Ofertas: `{player}`, `{status}`, `{date}`, `{offer}`. Entregas: `{label}`, `{amount}`, `{kind}`, `{status}`, `{id}`.

Usá `/mdveconomy reload` después de editar. Valida la configuración antes de cambiar la versión cargada; los menús abiertos se cierran para no mezclar layouts. Las selecciones de formulario no se pierden del inventario, porque aún no habían sido retiradas. Los mensajes comunes están en `messages-v2.yml`; los errores de validación incluyen el motivo directamente.

## Migración desde 1.0

Se reutiliza `database.db`, añadiendo tablas `market_*`. En el primer inicio se crea una copia consistente `database-before-v2-<fecha>.db` mediante SQLite y se importa una sola vez:

- Las publicaciones antiguas ACTIVE pasan a devoluciones reclamables del vendedor. No se convierten a pujas sin su consentimiento.
- Los pendientes antiguos PENDING pasan al nuevo menú de entregas.
- Las tablas antiguas permanecen conservadas. SOLD/CLAIMED no se vuelven a entregar.
- La configuración v1 se guarda como `config-v1-backup-<fecha>.yml` y se instala la nueva estructura. Las personalizaciones antiguas deben trasladarse a los nuevos archivos.

No reinstales el JAR v1 sobre una base ya usada por v2: las tablas antiguas se conservaron como histórico. Para volver atrás hay que restaurar también una copia coherente anterior y tener en cuenta operaciones posteriores, no solo cambiar el JAR. La migración no puede reconstruir pérdidas o duplicaciones ya ocurridas por bugs de la versión anterior.

## Recuperación de pagos y entregas

La reserva, venta, aceptación, cancelación, vencimiento y creación de entregas se registran en SQLite. Los reembolsos y premios permanecen reclamables hasta entregarse. No se arrojan artículos al suelo. Las devoluciones físicas grandes se pueden reclamar por partes según el espacio, conservando el resto.

Vault, los datos del jugador y SQLite **no comparten una transacción única**. Antes de cada retiro/entrega se guarda una intención. Si una excepción o caída impide determinar el resultado, se bloquea la operación para revisión en vez de repetirla o declarar éxito. Las operaciones que quedaron a mitad al reiniciar pasan a REVIEW. La publicación involucrada no se cierra automáticamente hasta resolverla.

Administración, solo después de comprobar el saldo/inventario y los registros reales:

```text
/mdveconomy revisiones
/mdveconomy resolver <id> retirado
/mdveconomy resolver <id> no-retirado
/mdveconomy resolver <id> entregado
/mdveconomy resolver <id> no-entregado
```

Para TAKE, `retirado` crea una devolución reclamable; `no-retirado` libera el bloqueo sin devolver algo que no salió. Para GIVE, `entregado` registra la entrega (o la porción de efectivo) como cumplida; `no-entregado` permite reintentar. No adivines: una decisión incorrecta del administrador puede crear pérdida o duplicación. La conciliación queda registrada en la base y consola. Si falla el almacenamiento durante la cuarentena, el motor se pausa hasta reiniciar y revisar.

## Verificación y límites

Ver `CAMBIOS-Y-PRUEBAS.md` y `verification/`. Las pruebas usan SQLite real y transferencias externas simuladas; las de menú validan layout/configuración, no una sesión visual de Minecraft. No se incluye ni ejecutó un servidor Paper. SQLite y las operaciones de inventario se serializan en el hilo del servidor para impedir carreras; se usan páginas/lotes, pero un disco lento o demasiadas publicaciones pueden afectar ticks. Es un plugin de un único servidor, no una economía distribuida multi-servidor. Los registros no se purgan automáticamente.

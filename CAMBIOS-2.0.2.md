# MDVEconomy 2.0.2 — command-tree restaurado

Se restaura com.github.k3vndev:command-tree:v1.0.2 en pom.xml usando el repositorio JitPack existente. Maven incluye la librería dentro del JAR final; no hay que instalarla como un plugin separado.

Los comandos usan CommandNode y CommandTreeManager:
- CAuctionHouse: /ah y /subastas, abre el mercado.
- CMDVEconomy: raíz del árbol administrativo.
- CReload: recarga configuración, menús y temporizador.
- CReviews: lista operaciones pendientes de revisión.
- CResolve: conciliación administrativa de operaciones.
- MarketCommandNode: comprobaciones de permisos y manejo de errores compartidos.
- CommandRegistration: conecta las raíces declaradas en plugin.yml con los árboles.

Para agregar un subcomando administrativo, crear un nodo que extienda MarketCommandNode e incluirlo en getSubCommands() de CMDVEconomy. Los constructores de las raíces conservan la firma exacta (CommandSender, JavaPlugin) que la librería instancia mediante reflexión.

Se conservan el mercado, los trueques, pujas, menús, persistencia y permisos de la versión nueva. No se recupera el sistema de venta antiguo ni /ah sell. Se mantienen comprobaciones explícitas de permisos al ejecutar cada nodo y se filtra el autocompletado administrativo por permisos, porque la librería no lo hace automáticamente.

Verificación: Maven clean verify con Java 21 y dependencias locales. Las pruebas de comandos comprueban la construcción por reflexión, el alias /subastas, despacho a subcomandos, permisos, autocompletado y rechazo de argumentos sobrantes o faltantes antes de ejecutar operaciones. No reemplazan la prueba de arranque en un servidor real.

Actualizar el repositorio reemplazando src completa y pom.xml; incluir scripts/verify_jar.py actualizado para verificar que command-tree está empaquetado. El workflow permanente no cambia. Extraer el JAR del ZIP servidor y reemplazar el anterior con el servidor apagado.

Los informes 2.0.0 y 2.0.1 son históricos. Esta versión sustituye la decisión anterior de usar manejadores CommandExecutor propios.

# MDVEconomy 2.0.1 — comandos separados

Se revisó el ZIP avanceeconomi-main proporcionado por el usuario.

La versión 2.0.0 sí tenía la ejecución y el autocompletado de comandos dentro de MDVEconomyPlugin. La comparación real era command.getName().equals("ah"), no una comparación de cadenas con ==. Era una decisión de organización, no un error de comparación.

Cambios:
- AuctionHouseCommand controla /ah y su alias /subastas.
- EconomyAdminCommand controla reload, revisiones y resolver, incluyendo permisos y autocompletado.
- CommandRegistration registra los manejadores declarados en plugin.yml.
- MDVEconomyPlugin conserva el ciclo de vida y los servicios del plugin, sin lógica de enrutamiento de comandos.
- Se conserva el comportamiento, los mensajes, permisos y alias de 2.0.0.
- La versión del proyecto pasa a 2.0.1; el workflow permanece igual y toma el nombre del JAR desde Maven.

No se reintroduce CommandTree: los nuevos manejadores implementan la API de Bukkit/Paper, ya presente como dependencia. El código antiguo que heredaba CommandNode sí requería esa librería; mezclar esas clases con la versión nueva causaba los errores anteriores.

Instalación del código: reemplazar la carpeta src completa en el repositorio y actualizar pom.xml con los del ZIP. Confirmar también las eliminaciones de archivos antiguos. El ZIP tiene pom.xml en su raíz.

Verificación: consultar verification/command-refactor-build.log. Las pruebas existentes cubren mercado y menús; no sustituyen una prueba de los comandos en un servidor Paper con Vault, MMOItems y MythicLib. Los informes de 2.0.0 que conserva el proyecto son históricos.

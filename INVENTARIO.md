# Inventario y trazabilidad de MDVEconomy

SHA-256 del ZIP original: `0cb14ba06d1790e3238588bb9fcb226b788af86153f6b2f068d19215cf7f5ff8`.

## Archivos originales

| Archivo | Bytes | Tratamiento |
|---|---:|---|
| `.github/workflows/build.yml` | 1266 | Modificado para build |
| `.gitignore` | 6 | Modificado para build |
| `.vscode/settings.json` | 113 | Conservado sin cambios |
| `LICENSE` | 1065 | Conservado sin cambios |
| `build_replace.py` | 4456 | Modificado para build |
| `ct_extract/META-INF/MANIFEST.MF` | 124 | Inspeccionado; generado, excluido del ZIP final |
| `ct_extract/META-INF/maven/com.mdvcraft/command-tree/pom.properties` | 109 | Inspeccionado; generado, excluido del ZIP final |
| `ct_extract/META-INF/maven/com.mdvcraft/command-tree/pom.xml` | 1170 | Inspeccionado; generado, excluido del ZIP final |
| `ct_extract/com/example/commandtree/Main.class` | 568 | Inspeccionado; generado, excluido del ZIP final |
| `ct_extract/com/mdvcraft/commandtree/CommandNode.class` | 6016 | Inspeccionado; generado, excluido del ZIP final |
| `ct_extract/com/mdvcraft/commandtree/CommandTreeManager.class` | 3834 | Inspeccionado; generado, excluido del ZIP final |
| `dependency-reduced-pom.xml` | 2790 | Inspeccionado; generado, excluido del ZIP final |
| `pom.xml` | 3793 | Modificado para build |
| `src/main/java/com/mdvcraft/mdveconomy/MDVEconomyPlugin.java` | 4215 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/auction/Auction.java` | 393 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/auction/AuctionRepository.java` | 5634 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/auction/AuctionService.java` | 14118 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/auction/AuctionStatus.java` | 113 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/commands/CMDVEconomy.java` | 946 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/commands/CReload.java` | 948 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/commands/auction_house/CAuctionHouse.java` | 1663 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/commands/auction_house/CSell.java` | 4413 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/database/DatabaseService.java` | 6270 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/database/ItemSerialization.java` | 552 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/economy/DenaresCash.java` | 2219 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/economy/EconomyService.java` | 1265 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/listeners/PlayerConnectionListener.java` | 713 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/menus/AuctionHouseMenu.java` | 13436 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/menus/JavaMenuService.java` | 7203 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/menus/MenuClickListener.java` | 2599 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/menus/MenuOption.java` | 291 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/menus/MenuViewHolder.java` | 1627 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/menus/PurchaseOptionsMenu.java` | 2651 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/messages/MessageService.java` | 1860 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItem.java` | 320 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItemRepository.java` | 3274 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItemService.java` | 5500 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItemStatus.java` | 97 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/players/PaymentMethod.java` | 721 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/players/PlayerPreferencesService.java` | 1303 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/players/PlayerRepository.java` | 3013 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/sound/SoundPlayer.java` | 858 | Conservado sin cambios |
| `src/main/java/com/mdvcraft/mdveconomy/util/Formatting.java` | 457 | Conservado sin cambios |
| `src/main/resources/config.yml` | 4213 | Conservado sin cambios |
| `src/main/resources/messages.yml` | 2225 | Conservado sin cambios |
| `src/main/resources/plugin.yml` | 624 | Conservado sin cambios |

## Clases y métodos declarados

Las referencias usan rutas relativas al proyecto y líneas de esta entrega. Se listan métodos y constructores explícitos; los accesores automáticos de records/enums son generados por Java.

### Auction

Registro de datos de una publicación y su vendedor.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/auction/Auction.java`.

Modelo/enum sin métodos explícitos adicionales.

### AuctionRepository

Inserción, conteos, página, búsqueda, transición a vendido/vencido y mapeo SQL.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/auction/AuctionRepository.java`.

- Línea 19: `private AuctionRepository()`.
- Línea 22: `public static long insert(Connection connection, long sellerId, String itemData, int quantity, double price, long createdAt, long expiresAt) throws SQLException`.
- Línea 45: `public static int countActiveByPlayer(Connection connection, long sellerId) throws SQLException`.
- Línea 55: `public static int countActive(Connection connection, long now) throws SQLException`.
- Línea 65: `public static List<Auction> findActivePage(Connection connection, long now, int limit, int offset) throws SQLException`.
- Línea 84: `public static Auction findById(Connection connection, long id) throws SQLException`.
- Línea 98: `public static boolean markSold(Connection connection, long id, long now) throws SQLException`.
- Línea 108: `public static List<Auction> findExpirable(Connection connection, long now) throws SQLException`.
- Línea 128: `public static boolean markExpired(Connection connection, long id) throws SQLException`.
- Línea 136: `private static Auction mapRow(ResultSet resultSet) throws SQLException`.

### AuctionService

Límites, creación/listado, compra por banco/efectivo, reembolso, cobro, entrega y vencimientos; trueque pendiente.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/auction/AuctionService.java`.

- Línea 38: `public AuctionService(JavaPlugin plugin, DatabaseService databaseService, EconomyService economyService, PendingItemService pendingItemService)`.
- Línea 50: `public RankLimits resolveLimits(Player player)`.
- Línea 79: `public CompletableFuture<SellResult> createListing(Player seller, ItemStack item, int quantity, double price, int maxListings, long durationMillis)`.
- Línea 103: `public CompletableFuture<AuctionPage> listActivePage(int page, int pageSize)`.
- Línea 125: `public CompletableFuture<PurchaseResult> purchaseBank(Player buyer, Auction auction)`.
- Línea 156: `public CompletableFuture<PurchaseResult> purchaseInventoryCash(Player buyer, Auction auction)`.
- Línea 187: `public CompletableFuture<PurchaseResult> offerPurchaseWithItems(Player buyer, long auctionId, List<ItemStack> offeredItems)`.
- Línea 193: `public CompletableFuture<PurchaseResult> completePurchaseWithItems(Player buyer, long auctionId, List<ItemStack> offeredItems)`.
- Línea 200: `public CompletableFuture<Auction> fetchAuction(long auctionId)`.
- Línea 208: `public PurchaseFailureReason validateCommon(Player buyer, Auction auction)`.
- Línea 224: `private CompletableFuture<PurchaseResult> markSoldOrRefund(Player buyer, Auction auction, Supplier<CompletableFuture<Boolean>> refund)`.
- Línea 240: `private CompletableFuture<PurchaseResult> completePurchase(Player buyer, Auction auction)`.
- Línea 258: `private void notifySellerItemSold(Auction auction, Player buyer)`.
- Línea 278: `private CompletableFuture<Void> deliverPurchasedItem(Player buyer, Auction auction)`.
- Línea 284: `private int requiredCashAmount(double price)`.
- Línea 292: `public void processExpiredAuctions()`.
- Línea 316: `private <T> CompletableFuture<T> callSync(Callable<T> callable)`.
- Línea 339: `public static SellResult ok(long auctionId)`.
- Línea 343: `public static SellResult failure(SellFailureReason reason)`.
- Línea 357: `public static PurchaseResult ok()`.
- Línea 361: `public static PurchaseResult failure(PurchaseFailureReason reason)`.

### AuctionStatus

Estados ACTIVE, SOLD, CANCELLED y EXPIRED; CANCELLED no tiene flujo conectado.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/auction/AuctionStatus.java`.

Modelo/enum sin métodos explícitos adicionales.

### CAuctionHouse

Raíz /ah: apertura del menú, subcomandos y alias interno.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/commands/auction_house/CAuctionHouse.java`.

- Línea 22: `protected CAuctionHouse(CommandSender sender, JavaPlugin plugin)`.
- Línea 30: `public List<CommandNode<MDVEconomyPlugin>> getSubCommands()`.
- Línea 36: `public boolean execute(List<String> args)`.
- Línea 52: `public String getName()`.
- Línea 57: `public List<String> getAliases()`.

### CSell

Validación de la mano/precio/denares, límites, publicación asíncrona, retirada y mensajes.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/commands/auction_house/CSell.java`.

- Línea 21: `protected CSell(CommandSender sender, MDVEconomyPlugin plugin, AuctionService auctionService)`.
- Línea 27: `public boolean execute(List<String> args)`.
- Línea 38: `private void handleSell(Player player, List<String> args)`.
- Línea 114: `public String getName()`.
- Línea 119: `public List<String> getAliases()`.
- Línea 123: `private void playErrorSound(Player player)`.

### CMDVEconomy

Raíz administrativa: ejecución, nombre y subcomandos.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/commands/CMDVEconomy.java`.

- Línea 15: `protected CMDVEconomy(CommandSender sender, JavaPlugin plugin)`.
- Línea 20: `public boolean execute(List<String> args)`.
- Línea 30: `public String getName()`.
- Línea 35: `public List<CommandNode<MDVEconomyPlugin>> getSubCommands()`.

### CReload

Permiso y ejecución de recarga, rechazo de argumentos extra.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/commands/CReload.java`.

- Línea 12: `protected CReload(CommandSender sender, MDVEconomyPlugin plugin)`.
- Línea 17: `public String getName()`.
- Línea 22: `protected boolean hasPermission()`.
- Línea 27: `public boolean execute(List<String> args)`.

### DatabaseService

Conexión/migración SQLite, ejecutor, transacciones, cierre y contrato SqlFunction.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/database/DatabaseService.java`.

- Línea 28: `public DatabaseService(JavaPlugin plugin)`.
- Línea 37: `public void initialize()`.
- Línea 45: `private void connectAndMigrate()`.
- Línea 113: `private void migrateAddColumnIfMissing(Statement statement, String table, String column, String columnDefinition) throws SQLException`.
- Línea 128: `public <T> CompletableFuture<T> submit(SqlFunction<Connection, T> action)`.
- Línea 145: `public <T> T runInTransaction(Connection connection, SqlFunction<Connection, T> action) throws SQLException`.
- Línea 167: `public void close()`.

### ItemSerialization

Conversión de ItemStack a bytes/Base64 y operación inversa.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/database/ItemSerialization.java`.

- Línea 12: `private ItemSerialization()`.
- Línea 15: `public static String serialize(ItemStack item)`.
- Línea 19: `public static ItemStack deserialize(String data)`.

### DenaresCash

Identificación MMOItems, conteo, retirada y creación/devolución de denares físicos.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/economy/DenaresCash.java`.

- Línea 16: `private DenaresCash()`.
- Línea 19: `public static boolean isDenar(ItemStack item)`.
- Línea 31: `public static int count(PlayerInventory inventory)`.
- Línea 43: `public static boolean remove(PlayerInventory inventory, int amount)`.
- Línea 76: `public static void give(Player player, int amount)`.

### EconomyService

Obtención del proveedor Vault, saldo suficiente, retiro, depósito y formato decimal.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/economy/EconomyService.java`.

- Línea 16: `public EconomyService(JavaPlugin plugin)`.
- Línea 27: `public boolean has(OfflinePlayer player, double amount)`.
- Línea 31: `public boolean withdraw(OfflinePlayer player, double amount)`.
- Línea 35: `public boolean deposit(OfflinePlayer player, double amount)`.
- Línea 39: `public String format(double amount)`.

### PlayerConnectionListener

Entrega de pendientes al entrar el jugador.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/listeners/PlayerConnectionListener.java`.

- Línea 16: `public PlayerConnectionListener(PendingItemService pendingItemService)`.
- Línea 21: `public void onPlayerJoin(PlayerJoinEvent event)`.

### MDVEconomyPlugin

Ciclo de vida, registro de servicios/comandos/listeners, timer, recarga y accesores a servicios.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/MDVEconomyPlugin.java`.

- Línea 30: `public void onEnable()`.
- Línea 72: `public void onDisable()`.
- Línea 79: `public void reloadPluginConfig()`.
- Línea 84: `public MessageService getMessageService()`.
- Línea 88: `public PlayerPreferencesService getPlayerPreferencesService()`.
- Línea 92: `public CommandTreeManager<MDVEconomyPlugin> getBaseCommandTreeManager()`.
- Línea 96: `public EconomyService getEconomyService()`.
- Línea 100: `public AuctionHouseMenu getAuctionHouseMenu()`.
- Línea 104: `public AuctionService getAuctionService()`.

### AuctionHouseMenu

Apertura, paginación, compra, errores, preferencia y representación de botones/artículos.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/menus/AuctionHouseMenu.java`.

- Línea 37: `public AuctionHouseMenu(MDVEconomyPlugin plugin, AuctionService auctionService)`.
- Línea 48: `public void open(Player player, int page)`.
- Línea 61: `public void changePage(Player player, MenuViewHolder holder, int delta)`.
- Línea 74: `public void handlePurchaseClick(Player player, MenuViewHolder holder, long auctionId, InventoryClickEvent event)`.
- Línea 113: `private void purchase(Player player, MenuViewHolder holder, Auction auction, PaymentMethod method)`.
- Línea 133: `private void handlePurchaseError(Player player, Throwable error)`.
- Línea 161: `private void loadPage(Player player, MenuViewHolder holder, int page)`.
- Línea 179: `private void refreshDefaultPaymentMethodOption(Player player, MenuViewHolder holder)`.
- Línea 193: `public void updateDefaultPaymentMethodOption(MenuViewHolder holder, PaymentMethod method)`.
- Línea 201: `private void renderPage(Player player, MenuViewHolder holder, AuctionPage result)`.
- Línea 227: `private String messageFor(PurchaseFailureReason reason)`.
- Línea 237: `private ItemStack createAuctionDisplayItem(Auction auction)`.
- Línea 260: `private MenuViewHolder buildBaseMenu()`.
- Línea 270: `private void updatePaginationOptions(MenuViewHolder holder, int totalPages)`.
- Línea 297: `private void replacePaginationOptionsWithFiller(MenuViewHolder holder, ConfigurationSection config)`.
- Línea 306: `private ItemStack createPageNavigationItem(MenuOption option, int destinationPage, int totalPages)`.
- Línea 317: `private ItemStack createOptionItem(MenuOption option, String... placeholders)`.

### JavaMenuService

Construcción genérica de menús, rellenos, carga/validación de opciones, materiales y texto.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/menus/JavaMenuService.java`.

- Línea 29: `public JavaMenuService(MDVEconomyPlugin plugin)`.
- Línea 33: `public MenuViewHolder createMenu(ConfigurationSection config)`.
- Línea 93: `public ItemStack createDefaultFiller(ConfigurationSection config)`.
- Línea 101: `public boolean handleAction(MenuViewHolder holder, int slot, Consumer<MenuOption> actionHandler)`.
- Línea 112: `public Component parseText(String text)`.
- Línea 116: `private Map<Integer, MenuOption> loadOptions(ConfigurationSection options, int inventorySize, Set<Integer> ignoredSlots)`.
- Línea 151: `private void validateSlots(List<Integer> slots, int inventorySize, String configPath)`.
- Línea 159: `private int parseSlot(Object value, int inventorySize, String configPath)`.
- Línea 170: `private Material requireMaterial(String materialName, String configPath)`.
- Línea 178: `private ItemStack createFiller(Material material)`.
- Línea 186: `private ItemStack createItem(MenuOption option)`.

### MenuClickListener

Cancelación de clics, despacho de acciones y cambio de preferencia; no controla arrastres.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/menus/MenuClickListener.java`.

- Línea 18: `public MenuClickListener(MDVEconomyPlugin plugin, AuctionHouseMenu auctionHouseMenu)`.
- Línea 24: `public void onInventoryClick(InventoryClickEvent event)`.
- Línea 49: `private void toggleDefaultPaymentMethod(Player player, MenuViewHolder holder)`.
- Línea 60: `private void handleOption(Player player, MenuViewHolder holder, MenuOption option)`.

### MenuOption

Registro de botón y copia inmutable del lore.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/menus/MenuOption.java`.

Modelo/enum sin métodos explícitos adicionales.

### MenuViewHolder

Estado del inventario: opciones, slots, contexto, página, IDs de subastas y siguiente página.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/menus/MenuViewHolder.java`.

- Línea 18: `public MenuViewHolder(Map<Integer, MenuOption> optionsBySlot, List<Integer> ignoredSlots)`.
- Línea 23: `public Map<Integer, MenuOption> getOptionsBySlot()`.
- Línea 27: `public List<Integer> getIgnoredSlots()`.
- Línea 31: `public Object getContext()`.
- Línea 35: `public void setContext(Object context)`.
- Línea 39: `public void setInventory(Inventory inventory)`.
- Línea 44: `public Inventory getInventory()`.
- Línea 48: `public int getPage()`.
- Línea 52: `public void setPage(int page)`.
- Línea 56: `public Map<Integer, Long> getAuctionsBySlot()`.
- Línea 60: `public void setAuctionsBySlot(Map<Integer, Long> auctionsBySlot)`.
- Línea 64: `public boolean hasNextPage()`.
- Línea 68: `public void setHasNextPage(boolean hasNextPage)`.

### PurchaseOptionsMenu

Menú banco/inventario, previsualización y contexto de compra.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/menus/PurchaseOptionsMenu.java`.

- Línea 24: `public PurchaseOptionsMenu(MDVEconomyPlugin plugin, JavaMenuService javaMenuService)`.
- Línea 29: `public void open(Player player, Auction auction, ItemStack auctionItem, MenuViewHolder auctionHouseHolder)`.
- Línea 74: `public PurchaseContext getContext(MenuViewHolder holder)`.

### MessageService

Carga de mensajes con defaults, búsqueda, envío, placeholders y colores.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/messages/MessageService.java`.

- Línea 23: `public MessageService(JavaPlugin plugin)`.
- Línea 28: `public void load()`.
- Línea 43: `public String get(String path, String... placeholders)`.
- Línea 47: `public void send(CommandSender sender, String path, String... placeholders)`.
- Línea 55: `public static String applyPlaceholders(String template, String... placeholders)`.

### PendingItem

Registro de entrega pendiente con origen, estado y fechas.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItem.java`.

Modelo/enum sin métodos explícitos adicionales.

### PendingItemRepository

Inserción, consulta de pendientes, claim condicional y mapeo SQL.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItemRepository.java`.

- Línea 17: `private PendingItemRepository()`.
- Línea 20: `public static long insert(Connection connection, long playerId, String itemData, int quantity, String sourceType, Long sourceId, long createdAt) throws SQLException`.
- Línea 47: `public static List<PendingItem> findPendingForPlayer(Connection connection, long playerId) throws SQLException`.
- Línea 66: `public static boolean claim(Connection connection, long id, long claimedAt) throws SQLException`.
- Línea 75: `private static PendingItem mapRow(ResultSet resultSet) throws SQLException`.

### PendingItemService

Entrega inmediata o almacenamiento, cola por jugador, verificación de espacio y tareas síncronas.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItemService.java`.

- Línea 32: `public PendingItemService(JavaPlugin plugin, DatabaseService databaseService)`.
- Línea 41: `public CompletableFuture<Void> deliverOrStore(Player player, ItemStack item, String sourceType, Long sourceId)`.
- Línea 49: `public CompletableFuture<Void> storeOrDeliverForUuid(UUID uuid, String username, ItemStack item, String sourceType, Long sourceId)`.
- Línea 77: `public void deliverPending(Player player)`.
- Línea 96: `private CompletableFuture<Void> processQueue(Player player, List<PendingItem> items, int index)`.
- Línea 127: `private boolean hasRoom(PlayerInventory inventory, ItemStack stack)`.
- Línea 143: `private <T> CompletableFuture<T> runSync(Callable<T> callable)`.

### PendingItemStatus

Estados PENDING y CLAIMED.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/pending/PendingItemStatus.java`.

Modelo/enum sin métodos explícitos adicionales.

### PaymentMethod

INVENTORY/BANK, nombre visible, alternancia y conversión a/de base de datos.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/players/PaymentMethod.java`.

- Línea 16: `public String getDisplayName()`.
- Línea 20: `public PaymentMethod toggle()`.
- Línea 24: `public String toDatabaseValue()`.
- Línea 28: `public static PaymentMethod fromDatabaseValue(String value)`.

### PlayerPreferencesService

Lectura y alternancia asíncronas de la preferencia por UUID.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/players/PlayerPreferencesService.java`.

- Línea 15: `public PlayerPreferencesService(DatabaseService databaseService)`.
- Línea 19: `public CompletableFuture<PaymentMethod> getDefaultPaymentMethod(UUID uuid, String username)`.
- Línea 26: `public CompletableFuture<PaymentMethod> toggleDefaultPaymentMethod(UUID uuid, String username)`.

### PlayerRepository

ID por UUID, alta/nombre, búsqueda de UUID y preferencia persistida.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/players/PlayerRepository.java`.

- Línea 15: `private PlayerRepository()`.
- Línea 18: `public static long getOrCreateId(Connection connection, UUID uuid, String username) throws SQLException`.
- Línea 45: `private static void updateUsername(Connection connection, long id, String username) throws SQLException`.
- Línea 53: `public static UUID findUuidById(Connection connection, long id) throws SQLException`.
- Línea 62: `public static PaymentMethod getDefaultPaymentMethod(Connection connection, long playerId) throws SQLException`.
- Línea 73: `public static void setDefaultPaymentMethod(Connection connection, long playerId, PaymentMethod method) throws SQLException`.

### SoundPlayer

Reproducción del sonido configurado y advertencia si es inválido.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/sound/SoundPlayer.java`.

- Línea 14: `private SoundPlayer()`.
- Línea 17: `public static void play(JavaPlugin plugin, Player player, String key)`.

### Formatting

Tiempo restante en horas/minutos o mensaje de expiración.

Archivo: `src/main/java/com/mdvcraft/mdveconomy/util/Formatting.java`.

- Línea 4: `public static String formatRemaining(long expiresAt)`.

